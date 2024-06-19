package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class Shockwave extends SpellAreaEffect {

	public static final String MAX_REPULSION_VELOCITY = "max_repulsion_velocity";
	/** The radius within which maximum damage is dealt and maximum repulsion velocity is applied. */
	private static final double EPICENTRE_RADIUS = 1;

	public Shockwave(){
		super("shockwave", SpellActions.POINT_DOWN, false);
		this.soundValues(2, 0.5f, 0);
		this.alwaysSucceed(true);
		addProperties(DAMAGE, MAX_REPULSION_VELOCITY);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		float radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		if(target instanceof Player){

			if(!Wizardry.settings.playersMoveEachOther) return false;

			if(ItemArtefact.isArtefactActive((Player)target, WizardryItems.amulet_anchoring)){
				if(!world.isClientSide && caster instanceof Player) ((Player)caster).displayClientMessage(
						Component.translatable("spell.resist", target.getName(),
								this.getNameForTranslationFormatted()), true);
				return false;
			}
		}

		// Produces a linear profile from 0 at the edge of the radius to 1 at the epicentre radius, then
		// a constant value of 1 within the epicentre radius.
		float proximity = (float)(1 - (Math.max(origin.distanceTo(target.position()) - EPICENTRE_RADIUS, 0))/(radius - EPICENTRE_RADIUS));

		// Damage increases closer to player up to a maximum of 4 hearts (at 1 block distance).
		target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
				getProperty(DAMAGE).floatValue() * proximity * modifiers.get(SpellModifiers.POTENCY));

		if(!world.isClientSide){

			// Entity speed increases closer to the player to a maximum of 3 (at 1 block distance).
			// This is the entity's speed compared to its distance from the player. Used for a similar triangles
			// based x, y and z speed calculation.
			double velocityFactor = proximity * getProperty(MAX_REPULSION_VELOCITY).floatValue();

			double dx = target.getX() - origin.x;
			double dy = target.getY() + 1 - origin.y;
			double dz = target.getZ() - origin.z;

			target.motionX = velocityFactor * dx;
			target.motionY = velocityFactor * dy;
			target.motionZ = velocityFactor * dz;

			// Player motion is handled on that player's client so needs packets
			if(target instanceof ServerPlayer){
				((ServerPlayer)target).connection.sendPacket(new SPacketEntityVelocity(target));
			}
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){

		// Can't put this in affectEntity(...) because it's only called for non-allies, plus here is client-side already
		EntityUtils.getEntitiesWithinRadius(radius, origin.x, origin.y, origin.z, world, Player.class)
				.forEach(p -> Wizardry.proxy.shakeScreen(p, 10));

		double particleX, particleZ;

		for(int i = 0; i < 40; i++){

			particleX = origin.x - 1.0d + 2 * world.random.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.random.nextDouble();

			BlockState block = world.getBlockState(new BlockPos(origin.x, origin.y - 0.5, origin.z));

			if(block != null){
				world.addParticle(ParticleTypes.BLOCK_DUST, particleX, origin.y,
						particleZ, particleX - origin.x, 0, particleZ - origin.z, Block.getStateId(block));
			}
		}

		ParticleBuilder.create(Type.SPHERE).pos(origin.add(0, 0.1, 0)).scale((float)radius * 0.8f).clr(0.8f, 0.9f, 1).spawn(world);

		world.addParticle(ParticleTypes.EXPLOSION, origin.x, origin.y + 0.1, origin.z, 0, 0, 0);
	}

}
