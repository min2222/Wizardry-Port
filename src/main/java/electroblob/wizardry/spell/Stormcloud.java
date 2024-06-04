package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityStormcloud;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public class Stormcloud extends SpellConstructRanged<EntityStormcloud> {

	public Stormcloud(){
		super("stormcloud", EntityStormcloud::new, false);
		this.addProperties(DAMAGE, EFFECT_RADIUS);
		this.floor(true);
	}

	@Override
	protected void addConstructExtras(EntityStormcloud construct, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){
		construct.getY() += 5;
	}
}
