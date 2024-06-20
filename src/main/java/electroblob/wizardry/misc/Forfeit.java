package electroblob.wizardry.misc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.entity.construct.EntityArrowRain;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityVexMinion;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityMagicFireball;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Banish;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.SpellProperties.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A {@code Forfeit} object represents a negative effect that may happen when a player attempts to cast an
 * undiscovered spell. The nature and severity of the forfeit depends on the element and tier of the spell that was
 * attempted.<br>
 * <br>
 * Adding a new forfeit is as simple as calling {@link Forfeit#add(Tier, Element, Forfeit)} and supplying the
 * {@code Forfeit} instance along with a tier and element to associate it with. To create a forfeit, you may extend
 * this class and instantiate it, or use {@link Forfeit#create(ResourceLocation, BiConsumer)} to concisely define the
 * behaviour (this method is provided since most forfeits' behaviour code is fairly brief and would otherwise result in
 * a large number of trivial 'stub' classes - in fact, many of wizardry's forfeits are defined in a single line).<br>
 * <br>
 * This class also handles the (event-driven) selection of forfeits and determines when one should be applied.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
// With the exception of sound events, everything forfeit-related is done right here. How about that for modularity? :P
@Mod.EventBusSubscriber
public abstract class Forfeit {

	private static final ListMultimap<Pair<Tier, Element>, Forfeit> forfeits = ArrayListMultimap.create();

	private static final float TIER_CHANGE_CHANCE = 0.2f;

	private final ResourceLocation name;

	protected final SoundEvent sound;

	public Forfeit(ResourceLocation name){
		this.name = name;
		this.sound = WizardrySounds.createSound("forfeit." + name.getPath());
	}

	public abstract void apply(Level world, Player player);

	/**
	 * Returns an {@link Component} for the message displayed when this forfeit is activated.
	 * @param implementName An {@code ITextComponent} for the name of the implement being used. This is usually
	 *                      something generic like 'wand' or 'scroll'.
	 * @return An {@code ITextComponent} representing this forfeit's message, for use in chat messages.
	 * @see Forfeit#getMessageForWand()
	 * @see Forfeit#getMessageForScroll()
	 */
	public Component getMessage(Component implementName){
		return Component.translatable("forfeit." + name.toString(), implementName);
	}

	/** Wrapper for {@link Forfeit#getMessage(Component)} with {@code implementName} set to the lang file key
	 * {@code item.ebwizardry:wand.generic} */
	public Component getMessageForWand(){
		return getMessage(Component.translatable("item." + Wizardry.MODID + ":wand.generic"));
	}

	/** Wrapper for {@link Forfeit#getMessage(Component)} with {@code implementName} set to the lang file key
	 * {@code item.ebwizardry:scroll.generic} */
	public Component getMessageForScroll(){
		return getMessage(Component.translatable("item." + Wizardry.MODID + ":scroll.generic"));
	}

	/** Returns the {@link SoundEvent} played when this forfeit is activated. */
	public SoundEvent getSound(){
		return sound;
	}

	public static void add(Tier tier, Element element, Forfeit forfeit){
		forfeits.put(Pair.of(tier, element), forfeit);
	}

	public static Forfeit getRandomForfeit(Random random, Tier tier, Element element){
		float f = random.nextFloat();
		if(f < TIER_CHANGE_CHANCE) tier = tier.previous();
		else if(f > 1 - TIER_CHANGE_CHANCE) tier = tier.next();
		List<Forfeit> matches = forfeits.get(Pair.of(tier, element));
		if(matches.isEmpty()){
			Wizardry.logger.warn("No forfeits with tier {} and element {}!", tier, element);
			return null;
		}
		return matches.get(random.nextInt(matches.size()));
	}

	public static Collection<Forfeit> getForfeits(){
		return Collections.unmodifiableCollection(forfeits.values());
	}

	/** Static helper method that creates a {@code Forfeit} with the given name and an effect specified by the given
	 * consumer. This allows code to use a neater lambda expression rather than an anonymous class. */
	public static Forfeit create(ResourceLocation name, BiConsumer<Level, Player> effect){
		return new Forfeit(name){
			@Override
			public void apply(Level world, Player player){
				effect.accept(world, player);
			}
		};
	}

	/** Internal wrapper for {@link Forfeit#create(ResourceLocation, BiConsumer)} so I don't have to put wizardry's
	 * mod ID in every time. */
	private static Forfeit create(String name, BiConsumer<Level, Player> effect){
		return create(new ResourceLocation(Wizardry.MODID, name), effect);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL) // Forfeits come after spell disabling but before modifiers
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){

		if(!Wizardry.settings.discoveryMode) return;

		if(event.getCaster() instanceof Player && !((Player)event.getCaster()).isCreative()
				&& (event.getSource() == SpellCastEvent.Source.WAND || event.getSource() == SpellCastEvent.Source.SCROLL)){

			Player player = (Player)event.getCaster();
			WizardData data = WizardData.get(player);

			float chance = (float)Wizardry.settings.forfeitChance;
			if(ItemArtefact.isArtefactActive(player, WizardryItems.amulet_wisdom)) chance *= 0.5;

			// Use the synchronised random to ensure the same outcome on client- and server-side
			if(data.synchronisedRandom.nextFloat() < chance && !data.hasSpellBeenDiscovered(event.getSpell())){

				event.setCanceled(true);

				Forfeit forfeit = getRandomForfeit(data.synchronisedRandom, event.getSpell().getTier(), event.getSpell().getElement());

				if(forfeit == null){ // Should never happen, but just in case...
					if(!event.getWorld().isClientSide) player.sendSystemMessage(Component.translatable("forfeit.ebwizardry:do_nothing"));
					return;
				}

				forfeit.apply(event.getWorld(), player);

				ItemStack stack = player.getMainHandItem();

				if(!(stack.getItem() instanceof ISpellCastingItem)){
					stack = player.getOffhandItem();
					if(!(stack.getItem() instanceof ISpellCastingItem)) stack = ItemStack.EMPTY;
				}

				if(!stack.isEmpty()){
					// Still need to charge the player mana or consume the scroll
					if(event.getSource() == Source.SCROLL){
						if(!player.isCreative()) stack.shrink(1);
					}else if(stack.getItem() instanceof IManaStoringItem){
						int cost = (int)(event.getSpell().getCost() * event.getModifiers().get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding
						((IManaStoringItem)stack.getItem()).consumeMana(stack, cost, player);
					}
				}

				WizardryAdvancementTriggers.spell_failure.triggerFor(player);

				EntityUtils.playSoundAtPlayer(player, forfeit.getSound(), WizardrySounds.SPELLS, 1, 1);

				if(!event.getWorld().isClientSide) player.sendSystemMessage(
						event.getSource() == SpellCastEvent.Source.WAND ? forfeit.getMessageForWand() : forfeit.getMessageForScroll());
			}
		}
	}

	/** Called from the preInit method in the main mod class to set up all the forfeits. */
	public static void register(){

		add(Tier.NOVICE, Element.FIRE, create("burn_self", (w, p) -> p.setSecondsOnFire(5)));

		add(Tier.APPRENTICE, Element.FIRE, create("fireball", (w, p) -> {
			if(!w.isClientSide){
				EntityMagicFireball fireball = new EntityMagicFireball(w);
				Vec3 vec = p.getEyePosition(1).add(p.getLookAngle().scale(6));
				fireball.setPos(vec.x, vec.y, vec.z);
				fireball.shoot(p.getX(), p.getY() + p.getEyeHeight(), p.getZ(), 1.5f, 1);
				w.addFreshEntity(fireball);
			}
		}));

		add(Tier.APPRENTICE, Element.FIRE, create("firebomb", (w, p) -> {
			if(!w.isClientSide){
				EntityFirebomb firebomb = new EntityFirebomb(w);
				firebomb.setPos(p.getX(), p.getY() + 5, p.getZ());
				w.addFreshEntity(firebomb);
			}
		}));

		add(Tier.ADVANCED, Element.FIRE, create("explode", (w, p) -> w.explode(null, p.getX(), p.getY(), p.getZ(), 1, BlockInteraction.NONE)));

		add(Tier.ADVANCED, Element.FIRE, create("blazes", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 3; i++){
					BlockPos pos = BlockUtils.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityBlazeMinion blaze = new EntityBlazeMinion(w);
					blaze.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(blaze);
				}
			}
		}));

		add(Tier.MASTER, Element.FIRE, create("burn_surroundings", (w, p) -> {
			if(!w.isClientSide && EntityUtils.canDamageBlocks(p, w)){
				List<BlockPos> sphere = BlockUtils.getBlockSphere(p.blockPosition(), 6);
				for(BlockPos pos : sphere){
					if(w.random.nextBoolean() && w.isEmptyBlock(pos) && BlockUtils.canPlaceBlock(p, w, pos))
						w.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
				}
			}
		}));

		add(Tier.MASTER, Element.FIRE, create("meteors", (w, p) -> {
			if(!w.isClientSide) for(int i=0; i<5; i++) w.addFreshEntity(new EntityMeteor(w, p.getX() + w.random.nextDouble() * 16 - 8,
						p.getY() + 40 + w.random.nextDouble() * 30, p.getZ() + w.random.nextDouble() * 16 - 8,
						1, EntityUtils.canDamageBlocks(p, w)));
		}));

		add(Tier.NOVICE, Element.ICE, create("freeze_self", (w, p) -> p.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(), 200))));

		add(Tier.APPRENTICE, Element.ICE, create("freeze_self_2", (w, p) -> p.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(), 300, 1))));

		add(Tier.APPRENTICE, Element.ICE, create("ice_spikes", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 5; i++){
					EntityIceSpike iceSpike = new EntityIceSpike(w);
					double x = p.getX() + 2 - w.random.nextFloat() * 4;
					double z = p.getZ() + 2 - w.random.nextFloat() * 4;
					Integer y = BlockUtils.getNearestSurface(w, new BlockPos(x, p.getY(), z), Direction.UP, 2, true,
							BlockUtils.SurfaceCriteria.basedOn((t, u) -> t.getBlockState(u).isCollisionShapeFullBlock(t, u)));
					if(y == null) break;
					iceSpike.setFacing(Direction.UP);
					iceSpike.setPos(x, y, z);
					w.addFreshEntity(iceSpike);
				}
			}
		}));

		add(Tier.ADVANCED, Element.ICE, create("blizzard", (w, p) -> {
			if(!w.isClientSide){
				EntityBlizzard blizzard = new EntityBlizzard(w);
				blizzard.setPos(p.getX(), p.getY(), p.getZ());
				w.addFreshEntity(blizzard);
			}
		}));

		add(Tier.ADVANCED, Element.ICE, create("ice_wraiths", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 3; i++){
					BlockPos pos = BlockUtils.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityIceWraith iceWraith = new EntityIceWraith(w);
					iceWraith.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(iceWraith);
				}
			}
		}));

		add(Tier.MASTER, Element.ICE, create("hailstorm", (w, p) -> {
			if(!w.isClientSide){
				EntityHailstorm hailstorm = new EntityHailstorm(w);
				hailstorm.setPos(p.getX(), p.getY() + 5, p.getZ() - 3); // Subtract 3 from z because it's facing south (yaw 0)
				w.addFreshEntity(hailstorm);
			}
		}));

		add(Tier.MASTER, Element.ICE, create("ice_giant", (w, p) -> {
			if(!w.isClientSide){
				EntityIceGiant iceGiant = new EntityIceGiant(w);
				iceGiant.setPos(p.getX() + p.getLookAngle().x * 4, p.getY(), p.getZ() + p.getLookAngle().z * 4);
				w.addFreshEntity(iceGiant);
			}
		}));

		add(Tier.NOVICE, Element.LIGHTNING, create("thunder", (w, p) -> {
			p.push(-p.getLookAngle().x, 0, -p.getLookAngle().z);
			if(w.isClientSide) w.addParticle(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 0, 0, 0);
		}));

		add(Tier.APPRENTICE, Element.LIGHTNING, create("storm", (w, p) -> {
			if(!Spells.invoke_weather.isEnabled(Context.WANDS)) return;
			int standardWeatherTime = (300 + (new Random()).nextInt(600)) * 20;
			if(!w.isClientSide) {
				((ServerLevel)w).setWeatherParameters(standardWeatherTime, standardWeatherTime, true, true);
			}
		}));

		add(Tier.APPRENTICE, Element.LIGHTNING, create("lightning_sigils", (w, p) -> {
			if(!w.isClientSide){
				for(Direction direction : Direction.HORIZONTALS){
					BlockPos pos = p.blockPosition().relative(direction, 2);
					Integer y = BlockUtils.getNearestFloor(w, pos, 2);
					if(y == null) continue;
					EntityLightningSigil sigil = new EntityLightningSigil(w);
					sigil.setPos(pos.getX() + 0.5, y, pos.getZ() + 0.5);
					w.addFreshEntity(sigil);
				}
			}
		}));

		add(Tier.ADVANCED, Element.LIGHTNING, create("lightning", (w, p) -> {
			LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, w);
			bolt.setVisualOnly(false);
			bolt.setPos(p.getX(), p.getY(), p.getZ());
			w.addFreshEntity(bolt);
		}));

		add(Tier.ADVANCED, Element.LIGHTNING, create("paralyse_self", (w, p) -> p.addEffect(new MobEffectInstance(WizardryPotions.PARALYSIS.get(), 200))));

		add(Tier.ADVANCED, Element.LIGHTNING, create("lightning_wraiths", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 3; i++){
					BlockPos pos = BlockUtils.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityLightningWraith lightningWraith = new EntityLightningWraith(w);
					lightningWraith.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(lightningWraith);
				}
			}
		}));

		add(Tier.MASTER, Element.LIGHTNING, create("storm_elementals", (w, p) -> {
			if(!w.isClientSide){
				for(Direction direction : Direction.HORIZONTALS){
					BlockPos pos = p.blockPosition().relative(direction, 3);
					EntityStormElemental stormElemental = new EntityStormElemental(w);
					stormElemental.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(stormElemental);
				}
			}
		}));

		add(Tier.NOVICE, Element.NECROMANCY, create("nausea", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400))));

		add(Tier.APPRENTICE, Element.NECROMANCY, create("zombie_horde", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 3; i++){
					BlockPos pos = BlockUtils.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityZombieMinion zombie = new EntityZombieMinion(w);
					zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(zombie);
				}
			}
		}));

		add(Tier.ADVANCED, Element.NECROMANCY, create("wither_self", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.WITHER, 400))));

		add(Tier.MASTER, Element.NECROMANCY, create("cripple_self", (w, p) -> p.hurt(DamageSource.MAGIC, p.getHealth() - 1)));

		add(Tier.MASTER, Element.NECROMANCY, create("shadow_wraiths", (w, p) -> {
			if(!w.isClientSide){
				for(Direction direction : Direction.HORIZONTALS){
					BlockPos pos = p.blockPosition().relative(direction, 3);
					EntityShadowWraith wraith = new EntityShadowWraith(w);
					wraith.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.addFreshEntity(wraith);
				}
			}
		}));

		add(Tier.NOVICE, Element.EARTH, create("snares", (w, p) -> {
			if(!w.isClientSide && EntityUtils.canDamageBlocks(p, w)){
				for(Direction direction : Direction.HORIZONTALS){
					BlockPos pos = p.blockPosition().relative(direction);
					if(BlockUtils.canBlockBeReplaced(w, pos) && BlockUtils.canPlaceBlock(p, w, pos))
						w.setBlockAndUpdate(pos, WizardryBlocks.snare.defaultBlockState());
				}
			}
		}));

		add(Tier.NOVICE, Element.EARTH, create("squid", (w, p) -> {
			if(!w.isClientSide){
				Squid squid = new Squid(EntityType.SQUID, w);
				squid.setPos(p.getX(), p.getY() + 3, p.getZ());
				w.addFreshEntity(squid);
			}
		}));

		add(Tier.APPRENTICE, Element.EARTH, create("uproot_plants", (w, p) -> {
			if(!w.isClientSide && EntityUtils.canDamageBlocks(p, w)){
				List<BlockPos> sphere = BlockUtils.getBlockSphere(p.blockPosition(), 5);
				sphere.removeIf(pos -> !(w.getBlockState(pos).getBlock() instanceof IPlantable) || !BlockUtils.canBreakBlock(p, w, pos));
				sphere.forEach(pos -> w.destroyBlock(pos, true));
			}
		}));

		add(Tier.APPRENTICE, Element.EARTH, create("poison_self", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 1))));

		add(Tier.ADVANCED, Element.EARTH, create("flood", (w, p) -> {
			if(!w.isClientSide && EntityUtils.canDamageBlocks(p, w)){
				List<BlockPos> sphere = BlockUtils.getBlockSphere(p.blockPosition().above(), 2);
				sphere.removeIf(pos -> !BlockUtils.canBlockBeReplaced(w, pos, true) || !BlockUtils.canPlaceBlock(p, w, pos));
				sphere.forEach(pos -> w.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState()));
			}
		}));

		add(Tier.MASTER, Element.EARTH, create("bury_self", (w, p) -> {
			if(!w.isClientSide){
				List<BlockPos> sphere = BlockUtils.getBlockSphere(p.blockPosition(), 4);
				sphere.removeIf(pos -> !w.getBlockState(pos).isCollisionShapeFullBlock(w, pos) || BlockUtils.isBlockUnbreakable(w, pos) || BlockUtils.canBreakBlock(p, w, pos));
				sphere.forEach(pos -> {
					FallingBlockEntity block = new FallingBlockEntity(w, pos.getX() + 0.5, pos.getY() + 0.5,
							pos.getZ() + 0.5, w.getBlockState(pos));
					block.motionY = 0.3 * (4 - (p.blockPosition().getY() - pos.getY()));
					w.addFreshEntity(block);
				});
			}
		}));

		add(Tier.NOVICE, Element.SORCERY, create("spill_inventory", (w, p) -> {
			for(int i = 0; i < p.getInventory().items.size(); i++){
				ItemStack stack = p.getInventory().items.get(i);
				if(!stack.isEmpty()){
					p.drop(stack, true, false);
					p.getInventory().items.set(i, ItemStack.EMPTY);
				}
			}
		}));

		add(Tier.APPRENTICE, Element.SORCERY, create("teleport_self", (w, p) -> ((Banish)Spells.banish).teleport(p, w, 8 + w.random.nextDouble() * 8)));

		add(Tier.ADVANCED, Element.SORCERY, create("levitate_self", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200))));

		add(Tier.ADVANCED, Element.SORCERY, create("vex_horde", (w, p) -> {
			if(!w.isClientSide){
				for(int i = 0; i < 4; i++){
					BlockPos pos = BlockUtils.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityVexMinion vex = new EntityVexMinion(w);
					vex.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
					w.addFreshEntity(vex);
				}
			}
		}));

		add(Tier.MASTER, Element.SORCERY, create("black_hole", (w, p) -> {
			EntityBlackHole blackHole = new EntityBlackHole(w);
			Vec3 vec = p.getEyePosition(1).add(p.getLookAngle().scale(4));
			blackHole.setPos(vec.x, vec.y, vec.z);
			w.addFreshEntity(blackHole);
		}));

		add(Tier.MASTER, Element.SORCERY, create("arrow_rain", (w, p) -> {
			if(!w.isClientSide){
				EntityArrowRain arrowRain = new EntityArrowRain(w);
				arrowRain.setPos(p.getX(), p.getY() + 5, p.getZ() - 3); // Subtract 3 from z because it's facing south (yaw 0)
				w.addFreshEntity(arrowRain);
			}
		}));

		add(Tier.MASTER, Element.SORCERY, create("teleport_self_large_distance", (w, p) -> ((Banish)Spells.banish).teleport(p, w, 8 + w.random.nextDouble() * 700)));

		add(Tier.NOVICE, Element.HEALING, create("damage_self", (w, p) -> p.hurt(DamageSource.MAGIC, 4)));

		add(Tier.NOVICE, Element.HEALING, create("spill_armour", (w, p) -> {
			for(int i = 0; i < p.getInventory().armor.size(); i++){
				ItemStack stack = p.getInventory().armor.get(i);
				if(!stack.isEmpty()){
					p.drop(stack, true, false);
					p.getInventory().armor.set(i, ItemStack.EMPTY);
				}
			}
		}));

		add(Tier.APPRENTICE, Element.HEALING, create("hunger", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 4))));

		add(Tier.APPRENTICE, Element.HEALING, create("blind_self", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200))));

		add(Tier.ADVANCED, Element.HEALING, create("weaken_self", (w, p) -> p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 3))));

		add(Tier.ADVANCED, Element.HEALING, create("jam_self", (w, p) -> p.addEffect(new MobEffectInstance(WizardryPotions.ARCANE_JAMMER.get(), 300))));

		add(Tier.MASTER, Element.HEALING, create("curse_self", (w, p) -> p.addEffect(new MobEffectInstance(WizardryPotions.CURSE_OF_UNDEATH.get(), Integer.MAX_VALUE))));

	}

}