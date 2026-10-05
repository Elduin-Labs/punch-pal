package com.elduin.punch_pal.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Set;

/**
 * A friend who looks like a player. It runs up to you and punches you, and you fly 255 blocks away.
 * You always come down on safe ground, and it never hurts. Nobody can hurt the friend either.
 */
public class PunchPalEntity extends PathfinderMob {

	/** How far a punch sends you, in blocks. */
	public static final int PUNCH_BLOCKS = 255;

	public static final String LINE = "Pow! Have a nice flight, friend!";

	/** How far away it notices you and starts running at you. */
	private static final double CHASE_RANGE = 40.0;

	/** Close enough to punch, squared. */
	private static final double PUNCH_REACH_SQR = 2.5 * 2.5;

	/** Wait this long between punches, so you can walk back. 20 ticks is one second. */
	private static final int COOLDOWN_TICKS = 60;

	private int cooldown;

	public PunchPalEntity(EntityType<? extends PunchPalEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PunchGoal(this));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.cooldown > 0) {
			this.cooldown--;
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.getEntity() instanceof Player) {
			// It is your friend. Players cannot hurt it. The void and /kill still work.
			return false;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
		return false;
	}

	/** Swings, then sends the player 255 blocks straight away from the friend. */
	private void punch(ServerLevel level, ServerPlayer player) {
		this.swing(InteractionHand.MAIN_HAND);
		level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 1.0F, 1.0F);
		this.cooldown = COOLDOWN_TICKS;

		Vec3 away = new Vec3(player.getX() - this.getX(), 0.0, player.getZ() - this.getZ());
		if (away.lengthSqr() < 1.0E-4) {
			double angle = this.random.nextDouble() * Math.PI * 2.0;
			away = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
		}
		away = away.normalize();

		BlockPos landing = findLanding(level, player, away);
		if (landing == null) {
			// Nowhere safe to land (all void, or all lava). Nobody gets thrown into that.
			return;
		}

		puff(level, player);
		player.sendSystemMessage(Component.literal("<Punch Pal> ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(LINE).withStyle(ChatFormatting.GRAY)));
		player.teleportTo(level, landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0;
		puff(level, player);
		level.playSound(null, landing, SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private static void puff(ServerLevel level, ServerPlayer player) {
		level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
	}

	/**
	 * Looks for a safe place about 255 blocks away. If that spot is bad (lava, empty sky, inside a wall)
	 * it tries a little nearer, up to eight times. Returns null if none is safe.
	 */
	private static BlockPos findLanding(ServerLevel level, ServerPlayer player, Vec3 away) {
		for (int attempt = 0; attempt < 8; attempt++) {
			int blocks = PUNCH_BLOCKS - attempt * 15;
			double x = player.getX() + away.x * blocks;
			double z = player.getZ() + away.z * blocks;
			if (!level.getWorldBorder().isWithinBounds(x, z)) {
				continue;
			}
			int bx = (int) Math.floor(x);
			int bz = (int) Math.floor(z);
			// Asking for the height loads the chunk, so you never land in empty space.
			int y = level.dimensionType().hasCeiling()
					? findFloorUnderCeiling(level, bx, (int) player.getY(), bz)
					: level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
			if (y <= level.getMinY() + 1) {
				continue;
			}
			BlockPos feet = new BlockPos(bx, y, bz);
			if (isSafeToStand(level, feet)) {
				return feet;
			}
		}
		return null;
	}

	/** The Nether has a roof, so look for the floor near where the player was, not the top of the world. */
	private static int findFloorUnderCeiling(ServerLevel level, int x, int aroundY, int z) {
		for (int y = Math.min(aroundY + 16, level.getMaxY() - 2); y > level.getMinY() + 1; y--) {
			BlockPos feet = new BlockPos(x, y, z);
			if (level.getBlockState(feet.below()).isSolid() && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()) {
				return y;
			}
		}
		return level.getMinY();
	}

	private static boolean isSafeToStand(ServerLevel level, BlockPos feet) {
		BlockState floor = level.getBlockState(feet.below());
		FluidState fluid = floor.getFluidState();
		boolean hot = fluid.is(net.minecraft.tags.FluidTags.LAVA)
				|| floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CACTUS) || floor.is(Blocks.CAMPFIRE)
				|| floor.is(Blocks.SOUL_CAMPFIRE) || floor.is(Blocks.SWEET_BERRY_BUSH) || floor.is(BlockTags.FIRE);
		boolean roomToStand = !level.getBlockState(feet).isSolid() && !level.getBlockState(feet.above()).isSolid();
		return !hot && roomToStand;
	}

	/** Runs at the nearest player and punches when it gets there. */
	private static final class PunchGoal extends Goal {

		private final PunchPalEntity pal;
		private ServerPlayer target;

		PunchGoal(PunchPalEntity pal) {
			this.pal = pal;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!(this.pal.level() instanceof ServerLevel level)) {
				return false;
			}
			// false: creative players count too. Spectators never do.
			Player nearest = level.getNearestPlayer(this.pal.getX(), this.pal.getY(), this.pal.getZ(), CHASE_RANGE, false);
			if (nearest instanceof ServerPlayer player && player.isAlive()) {
				this.target = player;
				return true;
			}
			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.target != null && this.target.isAlive() && !this.target.isSpectator()
					&& this.target.level() == this.pal.level()
					&& this.pal.distanceToSqr(this.target) <= CHASE_RANGE * CHASE_RANGE;
		}

		@Override
		public void stop() {
			this.target = null;
			this.pal.getNavigation().stop();
		}

		@Override
		public void tick() {
			this.pal.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
			if (this.pal.distanceToSqr(this.target) > PUNCH_REACH_SQR) {
				this.pal.getNavigation().moveTo(this.target, 1.15);
			} else {
				this.pal.getNavigation().stop();
				if (this.pal.cooldown == 0 && this.pal.level() instanceof ServerLevel level) {
					this.pal.punch(level, this.target);
				}
			}
		}
	}
}
