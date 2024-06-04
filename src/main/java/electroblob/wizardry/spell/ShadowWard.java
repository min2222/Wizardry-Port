package electroblob.wizardry.spell;

import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.EnumAction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShadowWard extends Spell {

	public static final String REFLECTED_FRACTION = "reflected_fraction";

	public ShadowWard(){
		super("shadow_ward", EnumAction.BLOCK, true);
		addProperties(REFLECTED_FRACTION);
		soundValues(0.6f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(Level world, LivingEntity entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(world.isRemote){
			double dx = -1 + 2 * world.rand.nextFloat();
			double dy = -1 + world.rand.nextFloat();
			double dz = -1 + 2 * world.rand.nextFloat();
			world.spawnParticle(ParticleTypes.PORTAL, caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ, dx, dy, dz);
		}

		if(ticksInUse % 50 == 0){
			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}

		return true;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof LivingEntity){

			if(EntityUtils.isCasting(event.getEntityLiving(), Spells.shadow_ward) && !event.getSource().isUnblockable()
					&& !(event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).isRetaliatory())){

				event.setCanceled(true);

				float reflectedFraction = Mth.clamp(Spells.shadow_ward.getProperty(REFLECTED_FRACTION).floatValue(), 0, 1);

				// Now we can preserve the original damage source (sort of) as long as we make it retaliatory.
				// For some reason this isn't working, so I've reverted to plain old magic damage for now.
				//event.getEntityLiving().attackEntityFrom(
				//		MagicDamage.causeDirectMagicDamage(event.getSource().getTrueSource(), DamageType.MAGIC, true), event.getAmount() * 0.5f);
				DamageSafetyChecker.attackEntitySafely(event.getEntity(), DamageSource.MAGIC, event.getAmount()
						* (1 - reflectedFraction), event.getSource().getDamageType());
				event.getSource().getTrueSource().attackEntityFrom(MagicDamage.causeDirectMagicDamage(
						event.getEntityLiving(), DamageType.MAGIC, true), event.getAmount() * reflectedFraction);
			}
		}
	}

}
