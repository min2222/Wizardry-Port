package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;

public class GreaterTelekinesis extends SpellRay {

	public static final String HOLD_RANGE = "hold_range";
	public static final String THROW_VELOCITY = "throw_velocity";

	/** Makes things a bit smoother-looking / 'realistic'. */
	private static final float UNDERSHOOT = 0.2f;

	public GreaterTelekinesis(){
		super("greater_telekinesis", SpellActions.POINT, true);
		this.aimAssist(0.4f);
		this.particleSpacing(1);
		this.particleJitter(0.05);
		this.particleVelocity(0.3);
		addProperties(HOLD_RANGE, THROW_VELOCITY, DAMAGE);
		this.soundValues(0.8f, 1, 0.2f);
	}

	@Override public boolean canBeCastBy(Mob npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser) { return false; }

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(Level world, LivingEntity entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		// Can't be cast by dispensers so we know caster isn't null, but just in case...
		if(caster != null && (target instanceof LivingEntity || target instanceof EntityLevitatingBlock || target instanceof PrimedTnt)){

			if(target instanceof Player && ((caster instanceof Player && !Wizardry.settings.playersMoveEachOther)
					|| ItemArtefact.isArtefactActive((Player)target, WizardryItems.amulet_anchoring))){

				if(!world.isClientSide && caster instanceof Player) ((Player)caster).displayClientMessage(
						Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
				return false;
			}

			if(target instanceof EntityLevitatingBlock){
				((EntityLevitatingBlock)target).suspend();
				((EntityLevitatingBlock)target).setCaster(caster); // Yep, you can steal other players' blocks in mid-air!
			}
			
			Vec3 targetPos = target.position().add(0, target.getBbHeight()/2, 0);
			
			if(caster.isShiftKeyDown()){
				
				Vec3 look = caster.getLookAngle().scale(getProperty(THROW_VELOCITY).floatValue() * modifiers.get(WizardryItems.range_upgrade));
				target.push(look.x, look.y, look.z);
				// No IntelliJ, it's not always false, that's not how polymorphism works
				if(caster instanceof Player) caster.swing(caster.getUsedItemHand() == null ? InteractionHand.MAIN_HAND : caster.getUsedItemHand());
				
			}else{
			
				EntityUtils.undoGravity(target);
				
				// The following code extrapolates the entity's current velocity to determine whether it will pass the
				// target position in the next tick, and adds or subtracts velocity accordingly.
				
				Vec3 vec = origin.add(caster.getLookAngle().scale(getProperty(HOLD_RANGE).floatValue()));
				
				Vec3 velocity = vec.subtract(targetPos).subtract(target.getDeltaMovement())
						.scale(1 - UNDERSHOOT);
				
				target.push(velocity.x, velocity.y, velocity.z);
			}
			
			// Player motion is handled on that player's client so needs packets
			if(target instanceof ServerPlayer){
				((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
			}
			
			if(world.isClientSide){

				ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f + 0.3f * world.random.nextFloat(), 1)
				.pos(origin.subtract(caster.position())).target(target).time(0)
				.scale(Mth.sin(ticksInUse * 0.3f) * 0.1f + 0.9f).spawn(world);
				
				if(ticksInUse % 18 == 1) ParticleBuilder.create(Type.FLASH).entity(target).pos(0, target.getBbHeight()/2, 0)
				.scale(2.5f).time(30).clr(0.2f, 0.8f, 1).fade(1f, 1f, 1f).spawn(world);
				
				ParticleBuilder.create(Type.SPARKLE, target).vel(0, 0.05, 0).time(15).scale(0.6f).clr(0.2f, 0.6f, 1)
				.fade(1f, 1f, 1f).spawn(world);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.canDamageBlocks(caster, world) && !BlockUtils.isBlockUnbreakable(world, pos)
				&& world.getBlockState(pos).getMaterial().isSolid()
				&& (world.getBlockEntity(pos) == null || !world.getBlockEntity(pos).getPersistentData().hasUUID(ArcaneLock.NBT_KEY))){
			
			if(!world.isClientSide){

				EntityLevitatingBlock block = new EntityLevitatingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						world.getBlockState(pos));

				block.time = 1;
				block.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				block.setCaster(caster);

				world.addFreshEntity(block);
				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
				
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
