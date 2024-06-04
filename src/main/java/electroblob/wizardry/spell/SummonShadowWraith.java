package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityShadowWraith;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SummonShadowWraith extends SpellMinion<EntityShadowWraith> {

	public SummonShadowWraith(){
		super("summon_shadow_wraith", EntityShadowWraith::new);
		this.soundValues(1, 1.1f, 0.1f);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public String getDescription(){
		return "\u00A7k" + super.getDescription();
	}

}
