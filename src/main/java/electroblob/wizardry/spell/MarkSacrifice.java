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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MarkSacrifice extends SpellRay {

	private static final float DAMAGE_INCREASE_PER_LEVEL = 0.6f;

	public MarkSacrifice(){
		super("mark_sacrifice", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.mark_of_sacrifice,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0xe90e48).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8)).clr(0xff7bbb).spawn(world);
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		MobEffectInstance effect = event.getEntity().getEffect(WizardryPotions.mark_of_sacrifice);

		if(effect != null && event.getSource().isMagic()){
			event.setAmount(event.getAmount() * (1 + (1 + effect.getAmplifier()) * DAMAGE_INCREASE_PER_LEVEL));
			event.getEntity().removeEffect(WizardryPotions.mark_of_sacrifice);
		}
	}

}
