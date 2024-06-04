package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class MindTrick extends SpellRay {

	public MindTrick(){
		super("mind_trick", SpellActions.POINT, false);
		this.soundValues(0.7f, 1, 0.4f);
		addProperties(EFFECT_DURATION);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){

			if(!level.isClientSide){

				if(target instanceof Player){

					((LivingEntity)target).addEffect(new MobEffectInstance(MobEffects.NAUSEA,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));

				}else if(target instanceof Mob){

					((Mob)target).setAttackTarget(null);
					((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.mind_trick,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
				
			}else{
				for(int i=0; i<10; i++){
					ParticleBuilder.create(Type.DARK_MAGIC, world.rand, target.getX(),
							target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
					.clr(0.8f, 0.2f, 1.0f).spawn(world);
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof LivingEntity){
			// Cancels the mind trick effect if the creature takes damage
			// This has been moved to within an (event.getSource().getEntity() instanceof EntityLivingBase) check so it
			// doesn't crash the game with a ConcurrentModificationException. If you think about it, mind trick only
			// ought to be cancelled if something attacks the entity since potions, drowning, cacti etc. don't affect the
			// targeting.
			if(event.getEntity().isPotionActive(WizardryPotions.mind_trick)){
				event.getEntity().removePotionEffect(WizardryPotions.mind_trick);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Mind trick
		// If the target is null already, no need to set it to null, or infinite loops will occur.
		if((event.getEntity().isPotionActive(WizardryPotions.mind_trick)
				|| event.getEntity().isPotionActive(WizardryPotions.fear))
				&& event.getEntity() instanceof Mob && event.getTarget() != null){
			((Mob)event.getEntity()).setAttackTarget(null);
		}
	}
}
