package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Earthquake;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;

public class EntityEarthquake extends EntityMagicConstruct { // NOT a scaled construct, the size is controlled by time

	public EntityEarthquake(Level world){
		this(WizardryEntities.EARTHQUAKE.get(), world);
	}
	
	public EntityEarthquake(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
	}

	public void tick(){

		super.tick();

		double speed = Spells.EARTHQUAKE.getProperty(Earthquake.SPREAD_SPEED).doubleValue();

		if(!level.isClientSide && EntityUtils.canDamageBlocks(getCaster(), level)){

			// The further the earthquake is going to spread, the finer the angle increments.
			for(float angle = 0; angle < 2 * Math.PI; angle += Math.PI / (lifetime * 1.5)){

				// Calculates coordinates for the block to be moved. The radius increases with time. The +1.5 is to
				// leave blocks in the centre untouched.
				int x = this.getX() < 0 ? (int)(this.getX() + ((this.tickCount * speed) + 1.5) * Mth.sin(angle) - 1)
						: (int)(this.getX() + ((this.tickCount * speed) + 1.5) * Mth.sin(angle));
				int y = (int)(this.getY() - 0.5);
				int z = this.getZ() < 0 ? (int)(this.getZ() + ((this.tickCount * speed) + 1.5) * Mth.cos(angle) - 1)
						: (int)(this.getZ() + ((this.tickCount * speed) + 1.5) * Mth.cos(angle));

				BlockPos pos = new BlockPos(x, y, z);

				if(!BlockUtils.isBlockUnbreakable(level, pos) && !level.isEmptyBlock(pos) && level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)
						// Checks that the block above is not solid, since this causes the falling sand to vanish.
						&& !level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above()) && BlockUtils.canBreakBlock(getCaster(), level, pos)){

					// Falling blocks do the setting block to air themselves.
					FallingBlockEntity fallingblock = new FallingBlockEntity(level, x + 0.5, y + 0.5, z + 0.5,
							level.getBlockState(new BlockPos(x, y, z)));
					fallingblock.setDeltaMovement(fallingblock.getDeltaMovement().x, 0.3, fallingblock.getDeltaMovement().z);
					level.addFreshEntity(fallingblock);
				}
			}

		}

		List<LivingEntity> targets = EntityUtils
				.getLivingWithinRadius((this.tickCount * speed) + 1.5, this.getX(), this.getY(), this.getZ(), level);

		// In this particular instance, the caster is completely unaffected because they will always be in the
		// centre.
		targets.remove(this.getCaster());

		for(LivingEntity target : targets){

			// Searches in a 1 wide ring.
			if(this.distanceTo(target) > (this.tickCount * speed) + 0.5 && target.getY() < this.getY() + 1
					&& target.getY() > this.getY() - 1){

				// Knockback must be removed in this instance, or the target will fall into the floor.
				double motionX = target.getDeltaMovement().x;
				double motionZ = target.getDeltaMovement().z;

				if(this.isValidTarget(target)){
					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.BLAST),
							10 * this.damageMultiplier);
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
				}

				// All targets are thrown, even those immune to the damage, so they don't fall into the ground.
				target.setDeltaMovement(motionX, 0.8, motionZ);// Throws target into the air.

				// Player motion is handled on that player's client so needs packets
				if(target instanceof ServerPlayer){
					((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
				}
			}
		}
	}

}
