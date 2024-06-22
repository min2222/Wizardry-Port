package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Does not implement ISummonedCreature because it has different despawning rules and because EntityWolf already has an
 * owner system.
 */
public class EntitySpiritWolf extends Wolf {

	private int dispelTimer = 0;

	private static final int DISPEL_TIME = 10;

	public EntitySpiritWolf(Level world){
		this(WizardryEntities.SPIRIT_WOLF.get(), world);
		this.xpReward = 0;
	}
	
	public EntitySpiritWolf(EntityType<? extends Wolf> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	@Override
	protected void registerGoals(){

		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_,
			MobSpawnType p_146748_, SpawnGroupData p_146749_, CompoundTag p_146750_){

		// Adds Particles on spawn. Due to client/server differences this cannot be done
		// in the item.
		if(this.level.isClientSide){
			this.spawnAppearParticles();
		}

		return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
	}

	private void spawnAppearParticles(){
		for(int i=0; i<15; i++){
			double x = this.getX() - this.getBbWidth() / 2 + this.random.nextFloat() * getBbWidth();
			double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
			double z = this.getZ() - this.getBbWidth() / 2 + this.random.nextFloat() * getBbWidth();
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).spawn(level);
		}
	}

	public float getOpacity(){
		return 1 - (float)dispelTimer/DISPEL_TIME;
	}
	
	@Override
	public void tick(){

		super.tick();

		if(dispelTimer > 0){
			if(dispelTimer++ > DISPEL_TIME){
				this.discard();
			}
		}

		// Adds a dust particle effect
		if(this.level.isClientSide){
			double x = this.getX() - this.getBbWidth() / 2 + this.random.nextFloat() * getBbWidth();
			double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
			double z = this.getZ() - this.getBbWidth() / 2 + this.random.nextFloat() * getBbWidth();
			ParticleBuilder.create(Type.DUST).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).shaded(true).spawn(level);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {

		ItemStack stack = player.getItemInHand(hand);

		if(this.isTame()){

			// Allows the owner (but not other players) to dispel the spirit wolf using a
			// wand.
			if(stack.getItem() instanceof ISpellCastingItem && this.getOwner() == player && player.isShiftKeyDown()){
				// Prevents accidental double clicking.
				if(this.tickCount > 20){
					
					this.dispelTimer++;
					
					this.playSound(WizardrySounds.ENTITY_SPIRIT_WOLF_VANISH, 0.7F, random.nextFloat() * 0.4F + 1.0F);
					// This is necessary to prevent the wand's spell being cast when performing this
					// action.
					return InteractionResult.SUCCESS;
				}
			}
		}

		return super.mobInteract(player, hand);

	}

	@Override
	public boolean isBaby() {
		return false;
	}
	
	@Override
	public void setBaby(boolean pBaby) {
		
	}

	@Override
	public int getExperienceReward(){
		return 0;
	}

	@Override
	protected boolean shouldDropLoot(){
		return false;
	}

	@Override
	protected ResourceLocation getDefaultLootTable(){
		return null;
	}

	@Override
	public Component getDisplayName(){
		if(getOwner() != null){
			return Component.translatable(ISummonedCreature.NAMEPLATE_TRANSLATION_KEY, getOwner().getName(),
					Component.translatable("entity." + this.getEncodeId() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking
		// directly at the entity
		return Wizardry.settings.summonedCreatureNames && getOwner() != null;
	}

}
