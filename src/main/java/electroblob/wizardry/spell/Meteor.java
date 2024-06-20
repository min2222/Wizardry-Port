package electroblob.wizardry.spell;

import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Meteor extends SpellRay {

	// It doesn't really make sense to have a blast radius when the explosion is measured by strength
	public static final String BLAST_STRENGTH = "blast_strength";

	public Meteor(){
		super("meteor", SpellActions.POINT, false);
		this.soundValues(3, 1, 0);
		this.ignoreLivingEntities(true);
		addProperties(BLAST_STRENGTH);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(ItemArtefact.isArtefactActive(caster, WizardryItems.RING_METEOR.get())){

			if(!world.isClientSide){

				EntityMeteor meteor = new EntityMeteor(world, caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ(),
						modifiers.get(WizardryItems.BLAST_UPGRADE.get()), EntityUtils.canDamageBlocks(caster, world));

				Vec3 direction = caster.getLookAngle().scale(2 * modifiers.get(WizardryItems.RANGE_UPGRADE.get()));
				meteor.setDeltaMovement(direction);

				world.addFreshEntity(meteor);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;

		}else{
			return super.cast(world, caster, hand, ticksInUse, modifiers);
		}
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
				EntityMeteor meteor = new EntityMeteor(world, pos.getX(), pos.getY() + 50, pos.getZ(),
						modifiers.get(WizardryItems.BLAST_UPGRADE.get()), EntityUtils.canDamageBlocks(caster, world));
				world.addFreshEntity(meteor);
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
