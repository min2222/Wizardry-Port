package electroblob.wizardry.item;

import com.google.common.collect.Multimap;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.LightningHammer;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ItemLightningHammer extends Item implements IConjuredItem {

	public static final String DURATION_NBT_KEY = "duration";
	// Annoyingly we can't implement this for attack damage, but at least it gets saved for when the hammer is thrown
	public static final String DAMAGE_MULTIPLIER_NBT_KEY = "damageMultiplier";

	public static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("d4c3bd93-c8e3-49c5-b35b-9356663bad1b");

	private static final double ATTACK_SPEED = -3.2;
	private static final double CHAINING_RANGE = 4;
	private static final float CHAINING_DAMAGE = 4;
	private static final double THROW_SPEED = 0.75;
	private static final double MOVEMENT_SPEED_REDUCTION = -0.25;

	public ItemLightningHammer(){
		super();
		setMaxDamage(600);
		setMaxStackSize(1);
		setNoRepair();
		setCreativeTab(null);
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.EPIC;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(DURATION_NBT_KEY)){
			return stack.getTagCompound().getInteger(DURATION_NBT_KEY);
		}
		return super.getMaxDamage(stack);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	private float getDamageMultiplier(ItemStack stack){
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(DAMAGE_MULTIPLIER_NBT_KEY)){
			return stack.getTagCompound().getFloat(DAMAGE_MULTIPLIER_NBT_KEY);
		}
		return 1;
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EquipmentSlot slot){

		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);

		if(slot == EquipmentSlot.MAINHAND){
			float attackDamage = Spells.lightning_hammer.arePropertiesInitialised() ?
					Spells.lightning_hammer.getProperty(Spell.DIRECT_DAMAGE).floatValue() : 10; // Fallback for search tree init, value doesn't really matter
			multimap.put(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", attackDamage, EntityUtils.Operations.ADD));
			multimap.put(Attributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", ATTACK_SPEED, EntityUtils.Operations.ADD));
			multimap.put(Attributes.MOVEMENT_SPEED.getName(), new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "Weapon modifier", MOVEMENT_SPEED_REDUCTION, EntityUtils.Operations.MULTIPLY_FLAT));
		}

		return multimap;
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void onUpdate(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setItemDamage(damage + 1);
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(!world.isRemote){
			EntityHammer hammer = new EntityHammer(world);
			Vec3 look = player.getLookVec();
			Vec3 vec = player.getPositionEyes(1).add(look);
			hammer.setPositionAndRotation(vec.x, vec.y - hammer.height/2, vec.z, player.rotationYawHead - 90, 0);
			// For some reason the above method insists on clamping the pitch to between -90 and 90
			hammer.rotationPitch = 180 + player.rotationPitch;
			hammer.prevRotationPitch = hammer.rotationPitch;

			float attackStrength = player.getCooledAttackStrength(0);
			double speed = THROW_SPEED * attackStrength; // Throw distance depends on the attack meter
			hammer.addVelocity(look.x * speed, look.y * speed, look.z * speed);
			hammer.lifetime = stack.getMaxDamage() - stack.getItemDamage();
			hammer.setCaster(player);
			hammer.damageMultiplier = getDamageMultiplier(stack);
			hammer.spin = true;
			world.spawnEntity(hammer);
		}

		EntityUtils.playSoundAtPlayer(player, WizardrySounds.ENTITY_HAMMER_THROW, 1.0F, 0.8f);

		//player.swingArm(hand);

		// Use this instead of stack.shrink so it works regardless of whether the player is in creative mode or not
		player.setHeldItem(hand, ItemStack.EMPTY);

		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack par2ItemStack){
		return false;
	}

	@Override
	public int getItemEnchantability(){
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player){
		return false;
	}

	// Can't be done in hitEntity because that's only called server-side, and after the cooldown is reset
	@SubscribeEvent
	public static void onAttackEntityEvent(AttackEntityEvent event){

		ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();

		if(stack.getItem() instanceof ItemLightningHammer && event.getTarget() instanceof LivingEntity){

			Player wielder = event.getEntityPlayer();
			LivingEntity hit = (LivingEntity)event.getTarget();

			float attackStrength = wielder.getCooledAttackStrength(0);

			double dx = wielder.posX - hit.posX;
			double dz;
			for(dz = wielder.posZ - hit.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random())
					* 0.01D){
				dx = (Math.random() - Math.random()) * 0.01D;
			}

			hit.knockBack(wielder, 2 * attackStrength, dx, dz);

			if(attackStrength == 1){ // Only chains when the attack meter is full

				List<LivingEntity> nearby = EntityUtils.getLivingWithinRadius(CHAINING_RANGE, hit.posX, hit.posY, hit.posZ, hit.world);

				nearby.remove(hit);
				nearby.remove(wielder);
				// When held, the number of chaining targets is halved
				int maxTargets = Spells.lightning_hammer.getProperty(LightningHammer.SECONDARY_MAX_TARGETS).intValue() / 2;
				while(nearby.size() > maxTargets) nearby.remove(nearby.size() - 1);

				for(LivingEntity target : nearby){

					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(wielder, DamageType.SHOCK), CHAINING_DAMAGE * ((ItemLightningHammer)stack.getItem()).getDamageMultiplier(stack));

					if(hit.world.isRemote){
						ParticleBuilder.create(Type.LIGHTNING).pos(hit.getPositionVector().add(0, hit.height / 2, 0))
								.target(target).spawn(hit.world);
						ParticleBuilder.spawnShockParticles(hit.world, target.posX, target.posY + target.height / 2, target.posZ);
					}

					//target.playSound(WizardrySounds.SPELL_SPARK, 1, 1.5f + 0.4f * world.rand.nextFloat());
				}
			}
		}
	}

}
