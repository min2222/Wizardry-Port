package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * This interface allows {@link MagicDamage} and {@link IndirectMagicDamage} to both be treated as instances of a single
 * type so that the damage type field can be accessed, rather than having to deal with each of them separately, which
 * would be inefficient and cumbersome (the latter of those classes cannot extend the former because they both need to
 * extend different subclasses of {@link DamageSource DamageSource}).
 * 
 * @since Wizardry 1.1
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public interface IElementalDamage {

	/** Returns the type of this damage, which determines how it interacts with different entities. */
	DamageType getType();
	/** Returns true is this damage is from a retaliatory effect (i.e. in response to other damage). Used to avoid
	 * infinite loops with retaliatory effects. */
	boolean isRetaliatory();

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() instanceof IElementalDamage){
			if(MagicDamage.isEntityImmune(((IElementalDamage)event.getSource()).getType(), event.getEntity())){
				event.setCanceled(true);
				// I would have liked to have done the 'resist' chat message here, but I overlooked the fact that I
				// would need an instance of the spell to get its display name!
				return;
			}
			// One convenient side effect of the new damage type system is that I can get rid of all the places where
			// creepers are charged and just put them here under shock damage - this is precisely the sort of
			// repetitive code I was trying to get rid of, since errors can (and did!) occur.
			if(event.getEntity() instanceof Creeper
					&& !((Creeper)event.getEntity()).isPowered()
					&& ((IElementalDamage)event.getSource()).getType() == DamageType.SHOCK){
				// Charges creepers when they are hit by shock damage
				EntityUtils.chargeCreeper((Creeper)event.getEntity());
			}

			if(event.getEntity().isInvertedHealAndHarm()
					&& ((IElementalDamage)event.getSource()).getType() == DamageType.RADIANT){
				event.getEntity().setSecondsOnFire(8); // Same as zombies/skeletons in sunlight
			}
		}
	}
}