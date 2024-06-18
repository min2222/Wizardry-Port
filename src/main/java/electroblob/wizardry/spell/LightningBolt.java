package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LightningBolt extends SpellRay {

	/** The NBT key used to store the UUID of the entity that summoned the lightning bolt. */
	public static final String SUMMONER_NBT_KEY = "summoner";
	/** The NBT key used to store the damage modifier for the lightning bolt. */
	public static final String DAMAGE_MODIFIER_NBT_KEY = "damageModifier";
	/** An NBT key used to cancel vanilla's implementation of lightning damage; since there's no way of extracting the
	 * lightning bolt entity from {@code LivingAttackEvent} this has to be done using NBT. */
	public static final String IMMUNE_TO_LIGHTNING_NBT_KEY = "immuneToLightning";

	public LightningBolt(){
		super("lightning_bolt", SpellActions.POINT, false);
		this.ignoreLivingEntities(true);
		addProperties(DAMAGE);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.canSeeSky(pos.above())){

			if(!world.isClientSide){
				// Temporarily disable the fire tick gamerule if player block damage is disabled
				// Bit of a hack but it works fine!
				boolean doFireTick = world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK);
				if(doFireTick && !Wizardry.settings.playerBlockDamage) world.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, world.getServer());

                net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(EntityType.LIGHTNING_BOLT, world);
                lightning.setPos(pos.getX(), pos.getY(), pos.getZ());
                lightning.setVisualOnly(false);
				if(caster != null) lightning.getPersistentData().putUUID(SUMMONER_NBT_KEY, caster.getUUID());
				lightning.getPersistentData().putFloat(DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
				world.addFreshEntity(lightning);

				// Reset doFireTick to true if it was true before
				if(doFireTick && !Wizardry.settings.playerBlockDamage) world.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, world.getServer());
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onEntityStruckByLightningEvent(EntityStruckByLightningEvent event){

		float damageModifier = event.getLightning().getPersistentData().getFloat(DAMAGE_MODIFIER_NBT_KEY);

		Entity summoner = null;

		if(event.getLightning().getPersistentData().hasUUID(SUMMONER_NBT_KEY)){

			summoner = EntityUtils.getEntityByUUID(event.getLightning().level,
					event.getLightning().getPersistentData().getUUID(SUMMONER_NBT_KEY));

			if(!(summoner instanceof LivingEntity)) summoner = null;
		}

		if(damageModifier > 0 || summoner != null){

			DamageSource source = summoner == null ? DamageSource.LIGHTNING_BOLT
					: MagicDamage.causeIndirectMagicDamage(event.getLightning(), summoner, DamageType.SHOCK);
			float damage = Spells.lightning_bolt.getProperty(DAMAGE).floatValue() * damageModifier;

			// Don't need DamageSafetyChecker here because this isn't an attack event
			EntityUtils.attackEntityWithoutKnockback(event.getEntity(), source, damage);
			event.getEntity().getPersistentData().putBoolean(IMMUNE_TO_LIGHTNING_NBT_KEY, true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getEntity().getPersistentData().contains(IMMUNE_TO_LIGHTNING_NBT_KEY)
				&& event.getSource() == DamageSource.LIGHTNING_BOLT){
			event.setCanceled(true);
			event.getEntity().getPersistentData().remove(IMMUNE_TO_LIGHTNING_NBT_KEY);
		}
	}
	
}
