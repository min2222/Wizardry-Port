package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;

public class IndirectMagicDamage extends IndirectEntityDamageSource implements IElementalDamage {

	private final DamageType type;
	private final boolean isRetaliatory;

	public IndirectMagicDamage(String name, Entity magic, Entity caster, DamageType type, boolean isRetaliatory){
		super(name, magic, caster);
		this.type = type;
		this.isRetaliatory = isRetaliatory;
		this.setMagicDamage();
		if(type == DamageType.FIRE) this.setSecondsOnFireDamage();
		if(type == DamageType.BLAST) this.setExplosion();
	}

	@Override
	public DamageType getType(){
		return type;
	}

	@Override
	public boolean isRetaliatory(){
		return isRetaliatory;
	}

}
