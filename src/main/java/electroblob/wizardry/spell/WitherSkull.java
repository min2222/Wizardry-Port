package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.EnumAction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class WitherSkull extends Spell {

	public static final String ACCELERATION = "acceleration";

	public WitherSkull(){
		super("wither_skull", EnumAction.NONE, false);
		addProperties(ACCELERATION);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3 look = caster.getLookVec();

		if(!world.isRemote){

			EntityWitherSkull witherskull = new EntityWitherSkull(world, caster, 1, 1, 1);

			witherskull.setPosition(caster.posX + look.x, caster.posY + look.y + 1.3, caster.posZ + look.z);

			double acceleration = getProperty(ACCELERATION).doubleValue() * modifiers.get(WizardryItems.range_upgrade);

			witherskull.accelerationX = look.x * acceleration;
			witherskull.accelerationY = look.y * acceleration;
			witherskull.accelerationZ = look.z * acceleration;

			witherskull.shootingEntity = caster;
			world.spawnEntity(witherskull);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){

				EntityWitherSkull witherskull = new EntityWitherSkull(world, caster, 1, 1, 1);

				double dx = target.posX - caster.posX;
				double dy = target.posY + (double)(target.height / 2.0F)
						- (caster.posY + (double)(caster.height / 2.0F));
				double dz = target.posZ - caster.posZ;

				witherskull.accelerationX = dx / caster.getDistance(target) * 0.1;
				witherskull.accelerationY = dy / caster.getDistance(target) * 0.1;
				witherskull.accelerationZ = dz / caster.getDistance(target) * 0.1;

				witherskull.shootingEntity = caster;
				witherskull.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);

				world.spawnEntity(witherskull);
				this.playSound(world, caster, ticksInUse, -1, modifiers);
			}

			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@SubscribeEvent
	public static void onEntityMobGriefingEvent(EntityMobGriefingEvent event){
		if(event.getEntity() instanceof Player){
			// If a player shot the wither skull, it should ignore the mob griefing gamerule and use playerBlockDamage instead
			event.setResult(Wizardry.settings.playerBlockDamage ? Event.Result.ALLOW : Event.Result.DENY);
		}
	}

}
