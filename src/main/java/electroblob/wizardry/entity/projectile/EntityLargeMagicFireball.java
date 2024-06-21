package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * It's like {@link EntityMagicFireball}, but bigger... the wizardry version of vanilla's
 * {@link net.minecraft.world.entity.projectile.EntityLargeFireball}
 */
@Mod.EventBusSubscriber
public class EntityLargeMagicFireball extends EntityMagicFireball {

	public static final String EXPLOSION_POWER = "explosion_power";

	/** The entity blast multiplier. This is now synced and saved centrally from {@link EntityBomb}. */
	public float blastMultiplier = 1.0f;

	/** The explosion power of this fireball. If this is -1, the damage for the fireball
	 * spell will be used instead; this is for when the fireball is not from a spell (i.e. a vanilla fireball replacement). */
	protected float explosionPower = -1;

	public EntityLargeMagicFireball(Level world){
		this(WizardryEntities.LARGE_MAGIC_FIREBALL.get(), world);
	}
	
	public EntityLargeMagicFireball(EntityType<? extends EntityMagicFireball> type, Level world){
		super(type, world);
	}

	public void setExplosionPower(float explosionPower){
		this.explosionPower = explosionPower;
	}

	public float getExplosionPower(){
		return explosionPower == -1 ? Spells.GREATER_FIREBALL.getProperty(EXPLOSION_POWER).floatValue() : explosionPower;
	}

	@Override
	public float getDamage(){
		return damage == -1 ? Spells.GREATER_FIREBALL.getProperty(Spell.DAMAGE).floatValue() : damage;
	}

	@Override
	protected DamageSource getDamageSource(Entity entityHit){
		if(entityHit instanceof Ghast){
			return MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.MAGIC).setProjectile();
		}else{
			return super.getDamageSource(entityHit);
		}
	}

	@Override
	protected void onHit(HitResult rayTrace){

		if(!level.isClientSide){
			boolean terrainDamage = EntityUtils.canDamageBlocks(this.getOwner(), level);
			this.level.explode(null, this.getX(), this.getY(), this.getZ(), getExplosionPower() * blastMultiplier, terrainDamage, terrainDamage ? BlockInteraction.DESTROY : BlockInteraction.NONE);
		}

		super.onHit(rayTrace);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer){
		buffer.writeFloat(blastMultiplier);
		super.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer){
		blastMultiplier = buffer.readFloat();
		super.readSpawnData(buffer);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putFloat("blastMultiplier", blastMultiplier);
	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(EntityJoinLevelEvent event){
		// Replaces all vanilla large fireballs with wizardry ones
		if(Wizardry.settings.replaceVanillaFireballs && event.getEntity() instanceof LargeFireball){

			event.setCanceled(true);

			EntityLargeMagicFireball fireball = new EntityLargeMagicFireball(event.getLevel());
			fireball.setOwner(((LargeFireball)event.getEntity()).getOwner());
			fireball.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
			fireball.setDamage(6);
			// Don't set the burn duration because vanilla large fireballs don't set mobs on fire directly
			fireball.setExplosionPower(((LargeFireball)event.getEntity()).explosionPower);
			fireball.setLifetime(75);

			fireball.setDeltaMovement(((LargeFireball)event.getEntity()).xPower * ACCELERATION_CONVERSION_FACTOR, ((LargeFireball)event.getEntity()).yPower * ACCELERATION_CONVERSION_FACTOR, ((LargeFireball)event.getEntity()).zPower * ACCELERATION_CONVERSION_FACTOR);

			event.getLevel().addFreshEntity(fireball);
		}
	}
}
