package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WitherSkull extends Spell {

	public static final String ACCELERATION = "acceleration";

	public WitherSkull(){
		super("wither_skull", UseAnim.NONE, false);
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

		Vec3 look = caster.getLookAngle();

		if(!world.isClientSide){

			net.minecraft.world.entity.projectile.WitherSkull witherskull = new net.minecraft.world.entity.projectile.WitherSkull(world, caster, 1, 1, 1);

			witherskull.setPos(caster.getX() + look.x, caster.getY() + look.y + 1.3, caster.getZ() + look.z);

			double acceleration = getProperty(ACCELERATION).doubleValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());

			witherskull.xPower = look.x * acceleration;
			witherskull.yPower = look.y * acceleration;
			witherskull.zPower = look.z * acceleration;

			witherskull.setOwner(caster);
			world.addFreshEntity(witherskull);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}
		caster.swing(hand);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		if(target != null){

			if(!world.isClientSide){

				net.minecraft.world.entity.projectile.WitherSkull witherskull = new net.minecraft.world.entity.projectile.WitherSkull(world, caster, 1, 1, 1);

				double dx = target.getX() - caster.getX();
				double dy = target.getY() + (double)(target.getBbHeight() / 2.0F)
						- (caster.getY() + (double)(caster.getBbHeight() / 2.0F));
				double dz = target.getZ() - caster.getZ();

				witherskull.xPower = dx / caster.distanceTo(target) * 0.1;
				witherskull.yPower = dy / caster.distanceTo(target) * 0.1;
				witherskull.zPower = dz / caster.distanceTo(target) * 0.1;

				witherskull.setOwner(caster);
				witherskull.setPos(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());

				world.addFreshEntity(witherskull);
				this.playSound(world, caster, ticksInUse, -1, modifiers);
			}

			caster.swing(hand);
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
