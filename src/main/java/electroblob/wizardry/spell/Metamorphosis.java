package electroblob.wizardry.spell;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Metamorphosis extends SpellRay {

	public static final BiMap<Class<? extends LivingEntity>, Class<? extends LivingEntity>> TRANSFORMATIONS = HashBiMap.create();

	static {
		addTransformation(EntityPig.class, EntityPigZombie.class);
		addTransformation(EntityCow.class, EntityMooshroom.class);
		addTransformation(EntityChicken.class, EntityBat.class);
		addTransformation(EntityZombie.class, EntityHusk.class);
		addTransformation(EntitySkeleton.class, EntityStray.class, EntityWitherSkeleton.class);
		addTransformation(EntitySpider.class, EntityCaveSpider.class);
		addTransformation(EntitySlime.class, EntityMagmaCube.class);
		addTransformation(EntityZombieMinion.class, EntityHuskMinion.class);
		addTransformation(EntitySkeletonMinion.class, EntityStrayMinion.class, EntityWitherSkeletonMinion.class);
	}

	/** Adds circular mappings between the given entity classes to the transformations map. In other words, given an
	 * array of entity classes [A, B, C, D], adds mappings A -> B, B -> C, C -> D and D -> A. */
	@SafeVarargs
	public static void addTransformation(Class<? extends LivingEntity>... entities){
		Class<? extends LivingEntity> previousEntity = entities[entities.length - 1];
		for(Class<? extends LivingEntity> entity : entities){
			TRANSFORMATIONS.put(previousEntity, entity);
			previousEntity = entity;
		}
	}
	
	public Metamorphosis(){
		super("metamorphosis", SpellActions.POINT, false);
		this.soundValues(0.5f, 1f, 0);
	}
	
	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){

			double xPos = target.posX;
			double yPos = target.posY;
			double zPos = target.posZ;

			// Sneaking allows the entities to be cycled through in the other direction.
			// Dispensers always cycle through entities in the normal direction.
			Class<? extends LivingEntity> newEntityClass = caster != null && caster.isSneaking() ?
					TRANSFORMATIONS.inverse().get(target.getClass()) : TRANSFORMATIONS.get(target.getClass());

			if(newEntityClass == null) return false;

			LivingEntity newEntity = null;

			try {
				newEntity = newEntityClass.getConstructor(Level.class).newInstance(world);
			} catch (Exception e){
				Wizardry.logger.error("Error while attempting to transform entity " + target.getClass() + " to entity "
						+ newEntityClass);
				e.printStackTrace();
			}
			
			if(newEntity == null) return false;

			if(!world.isRemote){
				// Transfers attributes from the old entity to the new one.
				newEntity.setHealth(((LivingEntity)target).getHealth());
				CompoundTag tag = new CompoundTag();
				target.writeToNBT(tag);
				// Remove the UUID because keeping it the same causes the entity to disappear
				NBTExtras.removeUniqueId(tag, "UUID");
				newEntity.readFromNBT(tag);

				target.setDead();
				newEntity.setPosition(xPos, yPos, zPos);
				world.spawnEntity(newEntity);
				
			}else{
				for(int i=0; i<20; i++){
					ParticleBuilder.create(Type.DARK_MAGIC, world.rand, xPos, yPos + 1, zPos, 1, false)
							.clr(0.1f, 0, 0).spawn(world);
				}
				ParticleBuilder.create(Type.BUFF).pos(xPos, yPos, zPos).clr(0xd363cb).spawn(world);
			}

			this.playSound(world, (LivingEntity)target, ticksInUse, -1, modifiers);
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
