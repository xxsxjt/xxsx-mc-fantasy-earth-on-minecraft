package com.xxsx.earthonlinemagic.entity;

import com.xxsx.earthonlinemagic.ArcanaPower;
import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.MagicJourney;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class ContractableFamiliarEntity extends TamableAnimal {
    public static final int MAX_STABILITY = 3;
    private static final EntityDataAccessor<Integer> DATA_STABILITY = SynchedEntityData.defineId(
            ContractableFamiliarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(
            ContractableFamiliarEntity.class, EntityDataSerializers.BYTE);
    private final Kind kind;

    protected ContractableFamiliarEntity(EntityType<? extends ContractableFamiliarEntity> type,
                                         Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes(double health, double speed, double attack,
                                                              double armor, double knockbackResistance) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attack)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.SAFE_FALL_DISTANCE, 8.0D);
    }

    public static boolean checkSpawnRules(EntityType<? extends ContractableFamiliarEntity> type,
                                          LevelAccessor level, EntitySpawnReason reason,
                                          BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STABILITY, 0);
        builder.define(DATA_MODE, (byte) FamiliarMode.FOLLOW.id());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ArcaneStability", getStability());
        output.putInt("FamiliarMode", getFamiliarMode().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setStability(input.getIntOr("ArcaneStability", 0));
        setFamiliarMode(FamiliarMode.byId(input.getIntOr("FamiliarMode", 0)));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.08D, true));
        this.goalSelector.addGoal(3, new ModeAwareFollowOwnerGoal(this, 1.15D, 9.0F, 2.1F));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.12D,
                stack -> stack.is(EarthOnlineMagic.ARCANE_DUST.get()), false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.92D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame()) {
            if (stack.is(EarthOnlineMagic.ARCANE_DUST.get())) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    setStability(getStability() + 1);
                    if (level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.WITCH, getX(), getY() + getBbHeight() * 0.7D,
                                getZ(), 8, 0.35D, 0.3D, 0.35D, 0.02D);
                    }
                    player.sendSystemMessage(Component.translatable(
                            "message.earth_online_magic.familiar.stability", getStability(), MAX_STABILITY));
                }
                return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            if (stack.is(EarthOnlineMagic.FAMILIAR_CONTRACT.get())) {
                if (!level().isClientSide()) {
                    if (getStability() < MAX_STABILITY) {
                        player.sendSystemMessage(Component.translatable(
                                "message.earth_online_magic.familiar.stability_required",
                                getStability(), MAX_STABILITY));
                    } else if (!net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                        stack.consume(1, player);
                        tame(player);
                        setFamiliarMode(FamiliarMode.FOLLOW);
                        setHealth(getMaxHealth());
                        navigation.stop();
                        setTarget(null);
                        level().broadcastEntityEvent(this, (byte) 7);
                        player.sendSystemMessage(Component.translatable(
                                "message.earth_online_magic.familiar.contracted", getDisplayName()));
                        if (player instanceof ServerPlayer serverPlayer) {
                            MagicJourney.complete(serverPlayer, MagicJourney.Milestone.FAMILIAR_CONTRACT);
                        }
                    }
                }
                return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
        } else if (isOwnedBy(player)) {
            if (stack.is(EarthOnlineMagic.ARCANE_DUST.get()) && getHealth() < getMaxHealth()) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    heal(5.0F);
                }
                return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            if (stack.isEmpty()) {
                if (!level().isClientSide()) {
                    setFamiliarMode(getFamiliarMode().next());
                    player.sendSystemMessage(Component.translatable(
                            "message.earth_online_magic.familiar.mode",
                            Component.translatable(getFamiliarMode().translationKey())));
                }
                return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(EarthOnlineMagic.ARCANE_DUST.get());
    }

    @Override
    public @Nullable ContractableFamiliarEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        var child = getType().create(level, EntitySpawnReason.BREEDING);
        if (child instanceof ContractableFamiliarEntity familiar) {
            if (isTame()) {
                familiar.setOwnerReference(getOwnerReference());
                familiar.setTame(true, true);
                familiar.setFamiliarMode(FamiliarMode.FOLLOW);
            }
            return familiar;
        }
        return null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() && tickCount % kind.particleInterval() == 0 && random.nextFloat() < 0.78F) {
            level().addParticle(kind.particle(), getRandomX(0.7D), getRandomY() + 0.08D,
                    getRandomZ(0.7D), 0.0D, 0.018D, 0.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 100 == 0 && isTame()
                && getFamiliarMode() != FamiliarMode.ANCHOR
                && getOwner() instanceof ServerPlayer owner && distanceToSqr(owner) <= 196.0D) {
            applyOwnerSupport(owner);
        }
    }

    private void applyOwnerSupport(ServerPlayer owner) {
        switch (kind) {
            case AETHER_FOX -> owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 140, 0, true, false, true));
            case RUNE_WOLF -> owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 0, true, false, true));
            case MANA_WISP -> ArcanaPower.setCurrentMana(owner, ArcanaPower.getCurrentMana(owner) + 2.0D);
        }
    }

    public Kind kind() {
        return kind;
    }

    public int getStability() {
        return entityData.get(DATA_STABILITY);
    }

    public void setStability(int stability) {
        entityData.set(DATA_STABILITY, Math.max(0, Math.min(MAX_STABILITY, stability)));
    }

    public FamiliarMode getFamiliarMode() {
        return FamiliarMode.byId(entityData.get(DATA_MODE));
    }

    public void setFamiliarMode(FamiliarMode mode) {
        entityData.set(DATA_MODE, (byte) mode.id());
        setOrderedToSit(mode == FamiliarMode.GUARD);
        setInSittingPose(mode == FamiliarMode.GUARD);
        if (mode != FamiliarMode.FOLLOW) {
            navigation.stop();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind) {
            case AETHER_FOX -> SoundEvents.FOX_AMBIENT;
            case RUNE_WOLF -> SoundEvents.BREEZE_IDLE_GROUND;
            case MANA_WISP -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (kind) {
            case AETHER_FOX -> SoundEvents.FOX_HURT;
            case RUNE_WOLF -> SoundEvents.BREEZE_HURT;
            case MANA_WISP -> SoundEvents.AMETHYST_BLOCK_HIT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (kind) {
            case AETHER_FOX -> SoundEvents.FOX_DEATH;
            case RUNE_WOLF -> SoundEvents.BREEZE_DEATH;
            case MANA_WISP -> SoundEvents.AMETHYST_BLOCK_BREAK;
        };
    }

    public enum Kind {
        AETHER_FOX(ParticleTypes.PORTAL, 14),
        RUNE_WOLF(ParticleTypes.ENCHANT, 13),
        MANA_WISP(ParticleTypes.WITCH, 9);

        private final ParticleOptions particle;
        private final int particleInterval;

        Kind(ParticleOptions particle, int particleInterval) {
            this.particle = particle;
            this.particleInterval = particleInterval;
        }

        public ParticleOptions particle() {
            return particle;
        }

        public int particleInterval() {
            return particleInterval;
        }
    }

    public enum FamiliarMode {
        FOLLOW(0, "message.earth_online_magic.familiar.mode.follow"),
        GUARD(1, "message.earth_online_magic.familiar.mode.guard"),
        ANCHOR(2, "message.earth_online_magic.familiar.mode.anchor");

        private final int id;
        private final String translationKey;

        FamiliarMode(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public FamiliarMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static FamiliarMode byId(int id) {
            for (FamiliarMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return FOLLOW;
        }
    }

    private static final class ModeAwareFollowOwnerGoal extends FollowOwnerGoal {
        private final ContractableFamiliarEntity familiar;

        private ModeAwareFollowOwnerGoal(ContractableFamiliarEntity familiar, double speed,
                                         float startDistance, float stopDistance) {
            super(familiar, speed, startDistance, stopDistance);
            this.familiar = familiar;
        }

        @Override
        public boolean canUse() {
            return familiar.getFamiliarMode() == FamiliarMode.FOLLOW && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return familiar.getFamiliarMode() == FamiliarMode.FOLLOW && super.canContinueToUse();
        }
    }
}
