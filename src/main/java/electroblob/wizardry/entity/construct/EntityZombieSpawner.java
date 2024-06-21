package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.living.EntityHuskMinion;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.SpellMinion;
import electroblob.wizardry.spell.ZombieApocalypse;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EntityZombieSpawner extends EntityMagicConstruct {

	private static final double MAX_NUDGE_DISTANCE = 0.1; // Prevents zombies all bunching up directly below the spawner

	public boolean spawnHusks;

	private int spawnTimer = 10;

	public EntityZombieSpawner(Level world){
		super(WizardryEntities.ZOMBIE_SPAWNER.get(), world);
	}
	
	public EntityZombieSpawner(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
	}

	@Override
	public void tick(){

		super.tick();

		if(lifetime - tickCount > 10 && spawnTimer-- == 0){

			this.playSound(WizardrySounds.ENTITY_ZOMBIE_SPAWNER_SPAWN, 1, 1);

			if(!level.isClientSide){

				EntityZombieMinion zombie = spawnHusks ? new EntityHuskMinion(level) : new EntityZombieMinion(level);

				zombie.setPos(this.getX() + (random.nextDouble() * 2 - 1) * MAX_NUDGE_DISTANCE, this.getY(),
						this.getZ() + (random.nextDouble() * 2 - 1) * MAX_NUDGE_DISTANCE);
				zombie.setCaster(this.getCaster());
				// Modifier implementation
				// Attribute modifiers are pretty opaque, see https://minecraft.gamepedia.com/Attribute#Modifiers
				zombie.setLifetime(Spells.ZOMBIE_APOCALYPSE.getProperty(SpellMinion.MINION_LIFETIME).intValue());
				AttributeInstance attribute = zombie.getAttribute(Attributes.ATTACK_DAMAGE);
				attribute.addTransientModifier(new AttributeModifier(SpellMinion.POTENCY_ATTRIBUTE_MODIFIER,
						damageMultiplier - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
				zombie.setHealth(zombie.getMaxHealth()); // Need to set this because we may have just modified the value
				zombie.invulnerableTime = 30; // Prevent fall damage
				zombie.hideParticles(); // Hide spawn particles or they pop out the top of the hidden box

				level.addFreshEntity(zombie);
			}

			spawnTimer += Spells.ZOMBIE_APOCALYPSE.getProperty(ZombieApocalypse.MINION_SPAWN_INTERVAL).intValue() + random.nextInt(20);
		}

		if(level.isClientSide){

			float b = 0.15f;

			for(double r = 1.5; r < 4; r += 0.2){
				ParticleBuilder.create(Type.CLOUD).clr(b-=0.02, 0, 0).pos(getX(), getY() - 0.3, getZ()).scale(0.5f / (float)r)
						.spin(r, 0.02/r * (1 + level.random.nextDouble())).spawn(level);
			}

		}

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt){
		super.addAdditionalSaveData(nbt);
		nbt.putInt("spawnTimer", spawnTimer);
		nbt.putBoolean("spawnHusks", spawnHusks);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt){
		super.readAdditionalSaveData(nbt);
		this.spawnTimer = nbt.getInt("spawnTimer");
		this.spawnHusks = nbt.getBoolean("spawnHusks");
	}

}
