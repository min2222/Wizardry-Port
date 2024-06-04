package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityRadiantTotem;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class RadiantTotem extends SpellConstructRanged<EntityRadiantTotem> {

	public static final String MAX_TARGETS = "max_targets";

	public RadiantTotem(){
		super("radiant_totem", EntityRadiantTotem::new, false);
		this.addProperties(EFFECT_RADIUS, MAX_TARGETS, HEALTH, DAMAGE);
		this.floor(false);
	}

	@Override
	protected void addConstructExtras(EntityRadiantTotem construct, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){
		construct.posY += 1.2;
	}
}
