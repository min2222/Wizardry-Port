 package electroblob.wizardry.spell;

 import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

 public class Earthquake extends SpellConstruct<EntityEarthquake> {

	public static final String SPREAD_SPEED = "spread_speed";

	public Earthquake(){
		super("earthquake", SpellActions.POINT_DOWN, EntityEarthquake::new, true);
		this.soundValues(2, 1, 0);
		this.overlap(true);
		this.floor(true);
		addProperties(EFFECT_RADIUS, SPREAD_SPEED);
	}
	
	// This one spawns particles
	@Override public boolean requiresPacket(){ return true; }
	
	@Override
	protected void addConstructExtras(EntityEarthquake construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		// Calculates the lifetime based on the base radius and spread speed
		// Also overwrites the -1 lifetime set due to permanent being true
		construct.lifetime = (int)(getProperty(EFFECT_RADIUS).floatValue()/getProperty(SPREAD_SPEED).floatValue()
				* modifiers.get(WizardryItems.BLAST_UPGRADE.get()));
	}
	
	@Override
	protected boolean spawnConstruct(Level world, double x, double y, double z, Direction side, LivingEntity caster, SpellModifiers modifiers){
		
		if(world.isClientSide){

			world.addParticle(ParticleTypes.EXPLOSION, x, y + 0.1, z, 0, 0, 0);

			double particleX, particleZ;

			for(int i=0; i<40; i++){

				particleX = x - 1.0d + 2 * world.random.nextDouble();
				particleZ = z - 1.0d + 2 * world.random.nextDouble();

				BlockState block = BlockUtils.getBlockEntityIsStandingOn(caster);
				world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), particleX, y,
						particleZ, particleX - x, 0, particleZ - z);
			}

			EntityUtils.getEntitiesWithinRadius(15, x, y, z, world, Player.class)
					.forEach(p -> Wizardry.proxy.shakeScreen(p, 12));

		}
		
		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

}
