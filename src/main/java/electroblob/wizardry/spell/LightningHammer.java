package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class LightningHammer extends SpellConstructRanged<EntityHammer> {

	public static final String ATTACK_INTERVAL = "attack_interval";
	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public LightningHammer(){
		super("lightning_hammer", EntityHammer::new, false);
		this.soundValues(3, 1, 0);
		this.floor(true);
		this.overlap(true);
		addProperties(EFFECT_RADIUS, SECONDARY_MAX_TARGETS, ATTACK_INTERVAL, DIRECT_DAMAGE, SPLASH_DAMAGE);
	}

	@Override
	protected boolean spawnConstruct(Level world, double x, double y, double z, Direction side, LivingEntity caster, SpellModifiers modifiers){
		if(!world.canSeeSky(new BlockPos(x, y, z))) return false;
		return super.spawnConstruct(world, x, y + 50, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityHammer construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		construct.setDeltaMovement(construct.getDeltaMovement().x, -2, construct.getDeltaMovement().z);
	}

}
