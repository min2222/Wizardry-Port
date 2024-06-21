package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;

public class EntityCombustionRune extends EntityScaledConstruct {

	public EntityCombustionRune(Level world){
		this(WizardryEntities.COMBUSTION_RUNE.get(), world);
		setSize(2, 0.2f);
	}
	
	public EntityCombustionRune(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(2, 0.2f);
	}

	@Override
	protected boolean shouldScaleWidth(){
		return false; // We're using the blast modifier for an actual explosion here, rather than the entity size
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void tick(){

		super.tick();

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(getBbWidth()/2, getX(), getY(), getZ(), level);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					float strength = Spells.COMBUSTION_RUNE.getProperty(Spell.BLAST_RADIUS).floatValue() * sizeMultiplier;

					level.explode(this.getCaster(), this.getX(), this.getY(), this.getZ(), strength, true,
							EntityUtils.canDamageBlocks(getCaster(), level) ? BlockInteraction.DESTROY : BlockInteraction.NONE);

					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}else if(this.random.nextInt(15) == 0){
			double radius = 0.5 + random.nextDouble() * 0.3;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			level.addParticle(ParticleTypes.FLAME, this.getX() + radius * Mth.cos(angle), this.getY() + 0.1,
					this.getZ() + radius * Mth.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

}
