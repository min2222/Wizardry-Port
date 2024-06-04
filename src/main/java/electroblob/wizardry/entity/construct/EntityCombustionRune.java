package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityCombustionRune extends EntityScaledConstruct {

	public EntityCombustionRune(Level world){
		super(world);
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

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(width/2, getX(), getY(), getZ(), world);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					float strength = Spells.combustion_rune.getProperty(Spell.BLAST_RADIUS).floatValue() * sizeMultiplier;

					world.newExplosion(this.getCaster(), this.getX(), this.getY(), this.getZ(), strength, true,
							EntityUtils.canDamageBlocks(getCaster(), world));

					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}else if(this.random.nextInt(15) == 0){
			double radius = 0.5 + random.nextDouble() * 0.3;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			world.spawnParticle(ParticleTypes.FLAME, this.getX() + radius * Mth.cos(angle), this.getY() + 0.1,
					this.getZ() + radius * Mth.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
