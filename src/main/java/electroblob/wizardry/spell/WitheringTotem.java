package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.entity.construct.EntityWitheringTotem;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;

public class WitheringTotem extends SpellConstructRanged<EntityWitheringTotem> {

	public static final String MAX_TARGETS = "max_targets";
	public static final String MAX_EXPLOSION_DAMAGE = "max_explosion_damage";

	public WitheringTotem(){
		super("withering_totem", EntityWitheringTotem::new, false);
		this.addProperties(EFFECT_RADIUS, MAX_TARGETS, DAMAGE, MAX_EXPLOSION_DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.floor(false);
	}

	@Override
	protected void addConstructExtras(EntityWitheringTotem construct, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){
		construct.setPos(construct.position().add(0, 1.2, 0));
	}
}
