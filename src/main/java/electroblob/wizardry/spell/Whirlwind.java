package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Whirlwind extends SpellRay {

	public static final String REPULSION_VELOCITY = "repulsion_velocity";

	public Whirlwind(){
		super("whirlwind", SpellActions.POINT, false);
		this.soundValues(0.8f, 0.7f, 0.2f);
		addProperties(REPULSION_VELOCITY);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(target instanceof Player && ((caster instanceof Player && !Wizardry.settings.playersMoveEachOther)
				|| ItemArtefact.isArtefactActive((Player)target, WizardryItems.amulet_anchoring))){

			if(!world.isClientSide && caster instanceof Player) ((Player)caster).displayClientMessage(
					Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			return false;
		}

		// Left as EntityLivingBase because why not be able to move armour stands around?
		if(target instanceof LivingEntity){
			
			Vec3 vec = target.getPositionEyes(1).subtract(origin).normalize();

			if(!world.isClientSide){

				float velocity = getProperty(REPULSION_VELOCITY).floatValue() * modifiers.get(SpellModifiers.POTENCY);

				target.motionX = vec.x * velocity;
				target.motionY = vec.y * velocity + 1;
				target.motionZ = vec.z * velocity;

				// Player motion is handled on that player's client so needs packets
				if(target instanceof ServerPlayer){
					((ServerPlayer)target).connection.sendPacket(new SPacketEntityVelocity(target));
				}
			}

			if(world.isClientSide){
				
				double distance = target.getDistance(origin.x, origin.y, origin.z);
				
				for(int i = 0; i < 10; i++){
					double x = origin.x + world.random.nextDouble() - 0.5 + vec.x * distance * 0.5;
					double y = origin.y + world.random.nextDouble() - 0.5 + vec.y * distance * 0.5;
					double z = origin.z + world.random.nextDouble() - 0.5 + vec.z * distance * 0.5;
					world.addParticle(ParticleTypes.CLOUD, x, y, z, vec.x, vec.y, vec.z);
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

}
