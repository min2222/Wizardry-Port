package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CurseOfSoulbinding extends SpellRay {

	public static final IStoredVariable<Set<UUID>> TARGETS_KEY = new IStoredVariable.StoredVariable<>("soulboundCreatures",
			s -> NBTExtras.listToNBT(s, NbtUtils::createUUID),
			// For some reason gradle screams at me unless I explicitly declare the type of t here, despite IntelliJ being fine without it
			(ListTag t) -> new HashSet<>(NBTExtras.NBTToList(t, NbtUtils::loadUUID)),
			// Curse of soulbinding is lifted when the caster dies, but not when they switch dimensions.
			Persistence.DIMENSION_CHANGE);

	public CurseOfSoulbinding(){
		super("curse_of_soulbinding", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		WizardData.registerStoredVariables(TARGETS_KEY);
	}

	@Override public boolean canBeCastBy(Mob npc, boolean override) { return false; }
	// You can't damage a dispenser so this would be nonsense!
	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser) { return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target) && caster instanceof Player){
			WizardData data = WizardData.get((Player)caster);
			if(data != null){
				// Return false if soulbinding failed (e.g. if the target is already soulbound)
				if(getSoulboundCreatures(data).add(target.getUUID())){
					// This will actually run out in the end, but only if you leave Minecraft running for 3.4 years
					((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.curse_of_soulbinding, Integer.MAX_VALUE));
				}else{
					return false;
				}
			}
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
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.4f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8)).clr(1, 0.8f, 1).spawn(world);
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		if(!event.getEntity().level.isClientSide && event.getEntity() instanceof Player
				&& !event.getSource().isBypassArmor() && !(event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory())){

			Player player = (Player)event.getEntity();
			WizardData data = WizardData.get(player);

			if(data != null){

				for(Iterator<UUID> iterator = getSoulboundCreatures(data).iterator(); iterator.hasNext();){

					Entity entity = EntityUtils.getEntityByUUID(player.level, iterator.next());

					if (entity == null || (entity instanceof LivingEntity && !((LivingEntity) entity).hasEffect(WizardryPotions.curse_of_soulbinding))) {
						iterator.remove();
					} else if (entity instanceof LivingEntity) {
						// Retaliatory effect
						if(DamageSafetyChecker.attackEntitySafely(entity, MagicDamage.causeDirectMagicDamage(player,
								MagicDamage.DamageType.MAGIC, true), event.getAmount(), event.getSource().getMsgId(),
								DamageSource.MAGIC, false)){
							// Sound only plays if the damage succeeds
							entity.playSound(WizardrySounds.SPELL_CURSE_OF_SOULBINDING_RETALIATE, 1.0F, player.level.random.nextFloat() * 0.2F + 1.0F);
						}
					}
				}

			}
		}
	}

	public static Set<UUID> getSoulboundCreatures(WizardData data){

		if(data.getVariable(TARGETS_KEY) == null){
			Set<UUID> result = new HashSet<>();
			data.setVariable(TARGETS_KEY, result);
			return result;

		}else return data.getVariable(TARGETS_KEY);
	}

}
