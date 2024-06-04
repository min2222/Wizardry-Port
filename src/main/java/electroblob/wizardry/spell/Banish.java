package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Banish extends SpellRay {

	public static final String MINIMUM_TELEPORT_DISTANCE = "minimum_teleport_distance";
	public static final String MAXIMUM_TELEPORT_DISTANCE = "maximum_teleport_distance";

	public Banish(){
		super("banish", SpellActions.POINT, false);
		this.addProperties(MINIMUM_TELEPORT_DISTANCE, MAXIMUM_TELEPORT_DISTANCE);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof LivingEntity){

			LivingEntity entity = (LivingEntity)target;

			double minRadius = getProperty(MINIMUM_TELEPORT_DISTANCE).doubleValue();
			double maxRadius = getProperty(MAXIMUM_TELEPORT_DISTANCE).doubleValue();
			double radius = (minRadius + world.rand.nextDouble() * maxRadius-minRadius) * modifiers.get(WizardryItems.blast_upgrade);

			teleport(entity, world, radius);
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(ParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.2f, 0, 0.2f).spawn(world);
	}

	// Extracted as a separate method for external use
	public boolean teleport(LivingEntity entity, Level world, double radius){

		float angle = world.rand.nextFloat() * (float)Math.PI * 2;

		int x = Mth.floor(entity.posX + Mth.sin(angle) * radius);
		int z = Mth.floor(entity.posZ - Mth.cos(angle) * radius);
		Integer y = BlockUtils.getNearestFloor(world,
				new BlockPos(x, (int)entity.posY, z), (int)radius);

		if(world.isRemote){

			for(int i=0; i<10; i++){
				double dx1 = entity.posX;
				double dy1 = entity.posY + entity.height * world.rand.nextFloat();
				double dz1 = entity.posZ;
				world.spawnParticle(ParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}

			if(entity instanceof Player) Wizardry.proxy.playBlinkEffect((Player)entity);
		}

		if(y != null){

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the target does
			// not teleport 1 block above the ground.
			if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
				y--;
			}

			if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
					|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
				return false;
			}

			if(!world.isRemote){
				entity.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			}

			this.playSound(world, entity, 0, -1, new SpellModifiers());
		}

		return true;
	}

}
