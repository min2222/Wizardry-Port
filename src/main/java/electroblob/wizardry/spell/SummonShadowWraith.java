package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityShadowWraith;

public class SummonShadowWraith extends SpellMinion<EntityShadowWraith> {

	public SummonShadowWraith(){
		super("summon_shadow_wraith", EntityShadowWraith::new);
		this.soundValues(1, 1.1f, 0.1f);
	}

}
