package electroblob.wizardry.entity.construct;

import java.util.Comparator;
import java.util.List;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityStormcloud extends EntityScaledConstruct {

	public EntityStormcloud(Level world){
		this(WizardryEntities.STORMCLOUD.get(), world);
		setSize(Spells.stormcloud.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 2);
	}
	
	public EntityStormcloud(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.stormcloud.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 2);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void tick(){

		super.tick();

		this.move(MoverType.SELF, new Vec3(this.getDeltaMovement().x, 0, this.getDeltaMovement().z));

		if(this.level.isClientSide){

			float areaFactor = (getBbWidth() * getBbWidth()) / 36; // Ensures cloud/raindrop density stays the same for different sizes

			for(int i = 0; i < 2 * areaFactor; i++) ParticleBuilder.create(Type.CLOUD, this)
					.clr(0.3f, 0.3f, 0.3f).shaded(true).spawn(level);
		}

		boolean stormcloudRingActive = getCaster() instanceof Player && ItemArtefact.isArtefactActive((Player)getCaster(), WizardryItems.ring_stormcloud);

		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().expandTowards(0, -10, 0));

		targets.removeIf(t -> !this.isValidTarget(t));

		float damage = Spells.stormcloud.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier;

		for(LivingEntity target : targets){

			if(target.tickCount % 150 == 0){ // Use target's lifetime so they don't all get hit at once, looks better

				if(!this.level.isClientSide){
					EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(
							this, this.getCaster(), MagicDamage.DamageType.SHOCK), damage);
				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(target.getX(), getY() + getBbHeight()/2, target.getZ())
							.target(target).scale(2).spawn(level);
					ParticleBuilder.spawnShockParticles(level, target.getX(), target.getY() + target.getBbHeight(), target.getZ());
				}

				target.playSound(WizardrySounds.ENTITY_STORMCLOUD_THUNDER, 1, 1.6f);
				target.playSound(WizardrySounds.ENTITY_STORMCLOUD_ATTACK, 1, 1);

				if(stormcloudRingActive) this.lifetime -= 40; // Each strike prolongs the lifetime by 2 seconds with the ring
			}
		}

		if(stormcloudRingActive){
			EntityUtils.getLivingWithinRadius(getBbWidth() * 3, getX(), getY(), getZ(), level).stream()
					.filter(this::isValidTarget).min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(e -> {
				Vec3 vel = e.position().subtract(this.position()).normalize().scale(0.2);
				this.setDeltaMovement(vel.x, this.getDeltaMovement().y, vel.z);
			});
		}

	}

}
