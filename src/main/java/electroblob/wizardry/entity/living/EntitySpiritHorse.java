package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IEntityLivingData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.passive.EntityAnimal;
import net.minecraft.world.entity.passive.EntityHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;

/**
 * Does not implement ISummonedCreature because it has different despawning rules and because EntityHorse already has an
 * owner system.
 */
@SuppressWarnings("deprecation") // It's what Entity does, so...
public class EntitySpiritHorse extends Horse {

	public static final Attribute JUMP_STRENGTH = AbstractHorse.JUMP_STRENGTH;

	private int idleTimer = 0;

	private int dispelTimer = 0;

	private static final int DISPEL_TIME = 10;

	public EntitySpiritHorse(Level par1World){
		super(par1World);
	}

	@Override
	public String getName(){
		if(this.hasCustomName()){
			return this.getCustomNameTag();
		}else{
			return I18n.translateToLocal("entity.wizardry.spirit_horse.name");
		}
	}

	@Override
	public int getTotalArmorValue(){
		return 0;
	}

	@Override
	protected int getExperiencePoints(Player player){
		return 0;
	}

	@Override
	protected boolean canDropLoot(){
		return false;
	}

	@Override
	protected Item getDropItem(){
		return null;
	}

	@Override
	protected ResourceLocation getLootTable(){
		return null;
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
	}

	@Override
	public void openGUI(Player p_110199_1_){
	}

	@Override
	public boolean processInteract(Player player, InteractionHand hand){

		ItemStack itemstack = player.getItemInHand(hand);

		// Allows the owner (but not other players) to dispel the spirit horse using a wand (shift-clicking, because
		// clicking mounts the horse in this case).
		if(itemstack.getItem() instanceof ISpellCastingItem && this.getOwner() == player && player.isShiftKeyDown()){
			// Prevents accidental double clicking.
			if(this.tickCount > 20 && dispelTimer == 0){

				this.dispelTimer++;
				
				this.playSound(WizardrySounds.ENTITY_SPIRIT_HORSE_VANISH, 0.7F, random.nextFloat() * 0.4F + 1.0F);
				// This is necessary to prevent the wand's spell being cast when performing this action.
				return true;
			}
			return false;
		}

		return super.processInteract(player, hand);
	}
	
	private void spawnAppearParticles(){
		for(int i=0; i<15; i++){
			double x = this.getX() - this.width / 2 + this.random.nextFloat() * width;
			double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
			double z = this.getZ() - this.width / 2 + this.random.nextFloat() * width;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).spawn(world);
		}
	}

	// I wrote this one!
	private LivingEntity getOwner(){

		// I think the DataManager stores any objects, so it now stores the UUID instead of its string representation.
		Entity owner = EntityUtils.getEntityByUUID(world, this.getOwnerUniqueId());

		if(owner instanceof LivingEntity){
			return (LivingEntity)owner;
		}else{
			return null;
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
			double x = this.getX() - this.width / 2 + this.random.nextFloat() * width;
			double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
			double z = this.getZ() - this.width / 2 + this.random.nextFloat() * width;
			ParticleBuilder.create(Type.DUST).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).shaded(true).spawn(world);
		}

		// Spirit horse disappears a short time after being dismounted.
		if(!this.isBeingRidden()){
			this.idleTimer++;
		}else if(this.idleTimer > 0){
			this.idleTimer = 0;
		}

		if(this.idleTimer > 200 && dispelTimer == 0){
			
			this.playSound(WizardrySounds.ENTITY_SPIRIT_HORSE_VANISH, 0.7F, random.nextFloat() * 0.4F + 1.0F);
			
			this.dispelTimer++;
		}
	}

	@Override
	public boolean canMateWith(EntityAnimal par1EntityAnimal){
		return false;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data){

		// Adds Particles on spawn. Due to client/server differences this cannot be done in the item.
		if(this.level.isClientSide){
			this.spawnAppearParticles();
		}

		return super.onInitialSpawn(difficulty, data);
	}

	@Override
	public Component getDisplayName(){
		if(getOwner() != null){
			return Component.translatable(ISummonedCreature.NAMEPLATE_TRANSLATION_KEY, getOwner().getName(),
					Component.translatable("entity." + this.getEntityString() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getOwner() != null;
	}

}
