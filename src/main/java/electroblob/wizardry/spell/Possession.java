package electroblob.wizardry.spell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.IVariable;
import electroblob.wizardry.data.IVariable.Variable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityStrayMinion;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.entity.projectile.EntityLargeMagicFireball;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import electroblob.wizardry.entity.projectile.EntityMagicArrow;
import electroblob.wizardry.entity.projectile.EntityMagicFireball;
import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.PacketPossession;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.potion.PotionSlowTime;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Possession extends SpellRay {

	/** A {@code ResourceLocation} representing the shader file used when possessing an entity. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/possession.json");

	/** The NBT tag name for storing the possessed flag in the target's tag compound, used only for rendering. */
	public static final String NBT_KEY = "possessed";
	/** The NBT tag name for storing the possessor's previous inventory in their tag compound. */
	public static final String INVENTORY_NBT_KEY = "prevInventory";

	/** The health (in half-hearts) below or equal to which the possessor will automatically stop possessing. */
	public static final String CRITICAL_HEALTH = "critical_health";

	private static final int PROJECTILE_COOLDOWN = 30;

	public static final IVariable<Integer> TIMER_KEY = new Variable<Integer>(Persistence.DIMENSION_CHANGE).withTicker(Possession::update);
	public static final IVariable<Mob> POSSESSEE_KEY = new Variable<>(Persistence.DIMENSION_CHANGE);
	public static final IVariable<Integer> SHOOT_COOLDOWN_KEY = new Variable<Integer>(Persistence.DIMENSION_CHANGE).withTicker((p, n) -> Math.max(n-1, 0));

	private static final Multimap<Class<? extends Mob>, BiConsumer<?, Player>> abilities = HashMultimap.create();
	private static final Map<Class<? extends Mob>, Function<Level, ? extends Projectile>> projectiles = new HashMap<>();
	private static final Set<Class<? extends Mob>> blacklist = new HashSet<>();

	private static final Map<Attribute, UUID> INHERITED_ATTRIBUTES;

	static {

		INHERITED_ATTRIBUTES = ImmutableMap.of(
				Attributes.MOVEMENT_SPEED, UUID.fromString("f65cfcaf-e7ec-4dfb-aa6c-711735d007e3"),
				Attributes.ATTACK_DAMAGE, UUID.fromString("ab67c89e-74a5-4e27-9621-40bffb4f7a03"),
				Attributes.KNOCKBACK_RESISTANCE, UUID.fromString("05529535-9bcf-42bb-8822-45f5ce6a8f08"));

		addAbility(Spider.class, (spider, player) -> { if(player.horizontalCollision) player.motionY = 0.2; });
		addAbility(Chicken.class, (chicken, player) -> { if(!player.isOnGround() && player.getDeltaMovement().y < 0) player.motionY *= 0.6D; });
		addAbility(Mob.class, (entity, player) -> { if(!entity.getType().fireImmune() && player.isOnFire()) player.clearFire(); });

		addProjectile(SnowGolem.class, t -> new Snowball(EntityType.SNOWBALL, t)); // Woooo snowballs!
		addProjectile(Blaze.class, EntityMagicFireball::new); // Ugh normal fireballs don't fit so let's just use mine!
		addProjectile(Ghast.class, EntityLargeMagicFireball::new);
		addProjectile(EntityIceWraith.class, EntityIceShard::new);
		addProjectile(EntityShadowWraith.class, EntityDarknessOrb::new);
		addProjectile(EntityStormElemental.class, EntityLightningDisc::new);
		addProjectile(Witch.class, t -> new ThrownPotion(EntityType.POTION, t));

		blacklist.add(EntityDecoy.class);
	}

	public Possession(){
		super("possession", SpellActions.POINT, false);
		addProperties(EFFECT_DURATION, CRITICAL_HEALTH);
	}

	@Override public boolean canBeCastBy(Mob npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser) { return false; }

	@Override
	public boolean requiresPacket(){
		return false; // Has its own packet
	}

	@Override
	protected SoundEvent[] createSounds(){
		return createSoundsWithSuffixes("possess", "end");
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3 look = caster.getLookAngle();
		Vec3 origin = new Vec3(caster.getX(), caster.getY() + caster.getEyeHeight() - Y_OFFSET, caster.getZ());

		if(!shootSpell(world, origin, look, caster, ticksInUse, modifiers)) return false;

//		if(casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) caster.swingArm(hand);
		this.playSound(world, caster, ticksInUse, -1, modifiers, "possess");
		return true;
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse,
                                  SpellModifiers modifiers){

		if(target instanceof Mob && !blacklist.contains(target.getClass()) && caster instanceof Player
				&& !isPossessing((Player)caster)){

			Player player = (Player)caster;

			if(!player.isCreative() && player.getHealth() <= getProperty(CRITICAL_HEALTH).floatValue()){
				player.displayClientMessage(Component.translatable(
						"spell." + this.getRegistryName() + ".insufficienthealth"), true);
				return false;
			}

			if(!world.isClientSide){
				int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.DURATION_UPGRADE.get()));
				if(possess(player, (Mob)target, duration)){
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse,
                                 SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	// ================================================ Helper methods ================================================

	/**
	 * Causes the given player to start possessing the given target for the given duration, and sets all relevant data
	 * for both entities accordingly. Also takes care of sending packets to update clients.
	 * @param possessor The player doing the possessing.
	 * @param target The entity being possessed.
	 * @param duration The number of ticks for which the possession should last. Pass in a negative integer to make the
	 * possession last indefinitely (until manually ended with the dismount key).
	 * @return True if the possession succeeded, false if for some reason it did not (only happens if the player's
	 * {@link WizardData} is null).
	 */
	public boolean possess(Player possessor, Mob target, int duration){

		if(possessor.isShiftKeyDown()) return false;

		if(WizardData.get(possessor) != null){

			WizardData.get(possessor).setVariable(POSSESSEE_KEY, target);
			WizardData.get(possessor).setVariable(TIMER_KEY, duration);

			possessor.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
			possessor.eyeHeight = target.getEyeHeight();
			setSize(possessor, target.getBbWidth(), target.getBbHeight());

			target.stopRiding();
			target.discard();
			target.setNoAi(true);
			target.setTarget(null);
			target.getPersistentData().putBoolean(NBT_KEY, true);

			// Attributes

			if(target instanceof EntityFlying || target instanceof net.minecraft.world.entity.passive.EntityFlying){
				possessor.capabilities.allowFlying = true;
				possessor.capabilities.isFlying = true;
			}

			// Apply attribute modifiers which change the player's attribute value to the target's value
			// Uses predefined UUIDs so we can easily remove them later
			attributes:
			for(Attribute attribute : INHERITED_ATTRIBUTES.keySet()){

				AttributeInstance instance = target.getAttributes().getInstance(attribute);

				if(instance != null){

					double targetValue = instance.getValue();
					double currentValue = possessor.getAttributes().getInstance(attribute).getValue();
					// Don't ask me why, but the player's base movement speed seems to be 0.1
					if(attribute == Attributes.MOVEMENT_SPEED) currentValue /= possessor.capabilities.getWalkSpeed();

					for(EquipmentSlot slot : EquipmentSlot.values()){
						if(target.getItemBySlot(slot).getAttributeModifiers(slot).containsKey(attribute.getName())){
							// If the mob has equipment, use the modifiers for that equipment instead of the mob's normal ones
							// Not doing this results in the player being able to one-hit most mobs when possessing a zombie pigman!
							continue attributes;
						}
					}

					possessor.getAttributes().getInstance(attribute).addPermanentModifier(new AttributeModifier(
							INHERITED_ATTRIBUTES.get(attribute), "possessionModifier", targetValue / currentValue,
							EntityUtils.Operations.MULTIPLY_FLAT));
				}
			}

			if(possessor.level.isClientSide){
				// Shaders and effects
				Wizardry.proxy.loadShader(possessor, SHADER);
				Wizardry.proxy.playBlinkEffect(possessor);

			}else{

				// Targeting

				for(Mob creature : EntityUtils.getEntitiesWithinRadius(16, possessor.getX(),
						possessor.getY(), possessor.getZ(), possessor.level, Mob.class)){
					// Mobs are dumb, if a player possesses something they're like "Huh?! Where'd you go?"
					// Of course, this won't last long if the player attacks them, since they'll revenge-target them
					if(creature.getTarget() == possessor && !creature.canAttack(target))
						creature.setTarget(null);
				}

				// Inventory and items

				if(possessor.getPersistentData() != null){
					NBTExtras.storeTagSafely(possessor.getPersistentData(), INVENTORY_NBT_KEY, possessor.inventory.writeToNBT(new ListTag()));
				}

				possessor.getInventory().clearContent();
				possessor.inventoryContainer.detectAndSendChanges();

				ItemStack stack = target.getMainHandItem().copy();

				if(target instanceof EnderMan && ((EnderMan)target).getCarriedBlock() != null){
					stack = new ItemStack(((EnderMan)target).getCarriedBlock().getBlock());

				}else if(stack.getItem() instanceof BowItem){
					Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
					enchantments.put(Enchantments.INFINITY_ARROWS, 1);
					EnchantmentHelper.setEnchantments(enchantments, stack);
					ItemStack arrow = new ItemStack(Items.ARROW);
					if(target instanceof Stray || target instanceof EntityStrayMinion){
						arrow = new ItemStack(Items.TIPPED_ARROW);
						PotionUtils.setPotion(arrow, Potions.SLOWNESS);
					}
					possessor.setItemInHand(InteractionHand.OFF_HAND, arrow);
				}

				possessor.setItemSlot(EquipmentSlot.MAINHAND, stack);

				// Packets

				WizardryPacketHandler.net.sendToAllTracking(new PacketPossession.Message(possessor, target, duration), possessor);
				if(possessor instanceof ServerPlayer){
					WizardryPacketHandler.net.sendTo(new PacketPossession.Message(possessor, target, duration), (ServerPlayer)possessor);
				}
			}

			return true;
		}

		return false;
	}

	/** Causes the given player to stop possessing their current possessee, if any, and resets all relevant data for
	 * both entities. Also takes care of sending packets to update clients. */
	public void endPossession(Player player){

		// Reverts the possessed entity back to normal

		Mob victim = getPossessee(player);

		if(victim != null){

			victim.revive();
			victim.setNoAi(false);
			victim.getPersistentData().remove(NBT_KEY);
			victim.setPos(player.getX(), player.getY(), player.getZ());
			if(!player.level.isClientSide) player.level.addFreshEntity(victim);

			for(MobEffectInstance effect : player.getActiveEffects()){
				if(effect.getEffect() instanceof PotionSlowTime) continue; // Don't transfer slow time
				victim.addEffect(effect);
			}
		}

		// Reverts the player back to normal

		player.clearActivePotions();

		player.eyeHeight = player.getDefaultEyeHeight(); // How convenient!

		if(WizardData.get(player) != null){
			WizardData.get(player).setVariable(TIMER_KEY, 0);
			WizardData.get(player).setVariable(POSSESSEE_KEY, null);
		}

		if(!player.capabilities.isCreativeMode){
			player.capabilities.allowFlying = false;
			player.capabilities.isFlying = false;
		}

		if(player.world.isClientSide && player == net.minecraft.client.Minecraft.getInstance().player){
			net.minecraft.client.Minecraft.getInstance().entityRenderer.stopUseShader();
			Wizardry.proxy.playBlinkEffect(player);
		}

		for(Attribute attribute : INHERITED_ATTRIBUTES.keySet()){
			player.getAttributeMap().getAttributeInstance(attribute).removeModifier(INHERITED_ATTRIBUTES.get(attribute));
		}

		if(player instanceof ServerPlayer){

			player.inventory.clear();

			if(player.getPersistentData() != null){
				player.inventory.readFromNBT(player.getPersistentData().getTagList(INVENTORY_NBT_KEY, NBT.TAG_COMPOUND));
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		this.playSound(player.world, player, 0, -1, null, "end");

		if(!player.world.isClientSide && player instanceof ServerPlayer){
			WizardryPacketHandler.net.sendToAllTracking(new PacketPossession.Message(player, null, 0), player);
			WizardryPacketHandler.net.sendTo(new PacketPossession.Message(player, null, 0), (ServerPlayer)player);
		}
	}

	/** Returns the {@code EntityLiving} that is currently being possessed by the given player, or null if the player is
	 * not currently possessing an entity. */
	@Nullable
	public static Mob getPossessee(Player player){
		return WizardData.get(player) == null ? null : WizardData.get(player).getVariable(POSSESSEE_KEY);
	}

	/** Returns true if the given player is currently possessing an entity, false otherwise. Just a shortcut for
	 * {@code Possession.getPossessee(player) != null}. */
	public static boolean isPossessing(Player player){
		return getPossessee(player) != null;
	}

	private static int update(Player player, Integer possessionTimer){

		if(possessionTimer == null) possessionTimer = 0;

		if(possessionTimer > 0){

			if(isPossessing(player) && !player.isShiftKeyDown()){

				possessionTimer--;

				if(player.world.isClientSide){
					ParticleBuilder.create(Type.DARK_MAGIC, player).clr(0.1f, 0, 0.3f).spawn(player.world);
					Wizardry.proxy.loadShader(player, SHADER);
				}

			}else{
				((Possession)Spells.possession).endPossession(player);
				return 0;
			}

		}else if(isPossessing(player)){
			((Possession)Spells.possession).endPossession(player);
		}

		return possessionTimer;
	}

	/** Adds the given {@link BiConsumer} to the list of abilities. An <i>ability</i> is an entity-specific action or
	 * effect that happens when a certain type of entity is possessed. For example, spiders can climb walls, endermen
	 * can pick up blocks, creepers explode, etc. Other mods may use this method to add their own possession abilities. */
	public static <T extends Mob> void addAbility(Class<T> entityType, BiConsumer<T, Player> ability){
		abilities.put(entityType, ability);
	}

	@SuppressWarnings("unchecked") // Guess what? Type erasure again!
	private static <T extends Mob> void performAbilities(Mob entity, Player player){
		// Now we have a type parameter T to work with we can ram the entity into the consumer without a compiler error
		for(Class<? extends Mob> entityType : abilities.keySet()){
			if(entityType.isAssignableFrom(entity.getClass())){
				abilities.get(entityType).forEach(a -> ((BiConsumer<T, Player>)a).accept((T)entity, player));
			}
		}
	}

	/** Adds the given factory to the list of projectiles. When a player right-clicks while possessing an entity of the
	 * given type, the given projectile factory will be invoked to create a projectile, which is then aimed and spawned. */
	public static <T extends Entity & Projectile> void addProjectile(Class<? extends Mob> entityType, Function<Level, T> factory){
		projectiles.put(entityType, factory);
	}

	/** Copied from Entity#setSize, with the call to move(...) removed. This is presumably also better than reflecting
	 * into Entity#setSize, which is protected. */
	private static void setSize(Entity entity, float width, float height){

		if(width != entity.getBbWidth() || height != entity.getBbHeight()){

			entity.getBbWidth() = width;
			entity.getBbHeight() = height;

			double halfwidth = (double)width / 2.0D;
			entity.setBoundingBox(new AABB(entity.getX() - halfgetBbWidth(), entity.getY(), entity.getZ() - halfgetBbWidth(), entity.getX() + halfgetBbWidth(), entity.getY() + (double)entity.getBbHeight(), entity.getZ() + halfgetBbWidth()));
		}
	}

	// ================================================ Event Handlers ================================================
	// We got every kind of event handler goin', folks!

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.phase == TickEvent.Phase.START){

			Mob possessee = getPossessee(event.player);

			if(possessee != null){
				// Updating these to the player's variables won't have an effect on player movement, but it will
				// affect various bits of mob-specific logic
				possessee.setPos(event.player.getX(), event.player.getY(), event.player.getZ());
				possessee.setDeltaMovement(event.player.getDeltaMovement());
				possessee.setOnGround(event.player.isOnGround());

				possessee.tick(); // Event though it's not in the world, it still needs updating
				possessee.tickCount++; // Normally gets updated from World

				if(possessee.getHealth() <= 0){
					((Possession)Spells.possession).endPossession(event.player);
				}

				performAbilities(possessee, event.player);
			}
		}

		// Right at the end of EntityPlayer#tick() it calls EntityPlayer#updateSize(), which resets the player's
		// size (and is also where this event is fired from, oddly enough) ... but not on my watch!
		if(event.phase == TickEvent.Phase.END){
			Mob possessee = getPossessee(event.player);
			if(possessee != null){
				setSize(event.player, possessee.getBbWidth(), possessee.getBbHeight());
			}
		}
	}

	// When possessing, attacks are diverted to the possessed entity for armour, immunity, resistance calculations
	// and so on, then when the damage is actually applied, the player is also damaged via onLivingDamageEvent below
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingAttackEvent(LivingAttackEvent event){

		if(event.getEntity() instanceof Player && event.getSource() != DamageSource.OUT_OF_WORLD){

			Mob possessee = getPossessee((Player)event.getEntity());

			if(possessee != null){
				DamageSafetyChecker.attackEntitySafely(possessee, event.getSource(), event.getAmount(), event.getSource().getMsgId());
				event.setCanceled(true);
			}
		}
	}

	// LivingDamageEvent used in preference to LivingHurtEvent because the player is 'inside' the possessed entity, so
	// any damage should come through that entity (and any armour, potions, enchantments etc. it has) first.
	@SubscribeEvent
	public static void onLivingDamageEvent(LivingDamageEvent event){

		for(Player player : event.getEntity().level.players()){

			Mob possessee = getPossessee(player);

			if(possessee == event.getEntity()){
				// Possessors take half of all damage taken by the entity they are possessing. If the possessor receives
				// fatal/critical damage (i.e. damage that takes them to half a heart or less), their health is reset to half
				// a heart and the possession ends.
				if(!player.getAbilities().instabuild){
					// TODO: Make this a proper DamageSource?
					DamageSafetyChecker.attackEntitySafely(player, DamageSource.OUT_OF_WORLD, event.getAmount() / 2,
							DamageSource.OUT_OF_WORLD.getMsgId());
				}

				if(player.getHealth() <= Spells.possession.getProperty(CRITICAL_HEALTH).floatValue()){
					player.setHealth(Spells.possession.getProperty(CRITICAL_HEALTH).floatValue());
					((Possession)Spells.possession).endPossession(player);
				}
			}
		}
	}

	// Prevents possessing players from interacting with blocks and controls projectile shooting
	@SubscribeEvent
	public static void onPlayerInteractEvent(PlayerInteractEvent event){

		if(event instanceof PlayerInteractEvent.RightClickItem) return; // Can always do this

		Mob possessee = getPossessee(event.getEntity());

		if(possessee != null){

			// Let endermen interact with blocks
			if(possessee instanceof EnderMan && (
					(event instanceof PlayerInteractEvent.RightClickBlock && ((EnderMan)possessee).getCarriedBlock() != null)
							|| (event instanceof PlayerInteractEvent.LeftClickBlock && ((EnderMan)possessee).getCarriedBlock() == null)))
				return;

			if(WizardData.get(event.getEntity()) != null && event.getLevel().isClientSide
					&& (event instanceof PlayerInteractEvent.RightClickEmpty
					|| event instanceof PlayerInteractEvent.EntityInteract
					|| event instanceof PlayerInteractEvent.RightClickBlock)){

				Integer cooldown = WizardData.get(event.getEntity()).getVariable(SHOOT_COOLDOWN_KEY);

				if(cooldown == null || cooldown == 0){

					WizardryPacketHandler.net.sendToServer(new PacketControlInput.Message(PacketControlInput.ControlType.POSSESSION_PROJECTILE));
					WizardData.get(event.getEntity()).setVariable(SHOOT_COOLDOWN_KEY, PROJECTILE_COOLDOWN);

					if(possessee instanceof EntityLightningWraith){
						Spells.ARC.cast(event.getLevel(), event.getEntity(), InteractionHand.MAIN_HAND, 0, new SpellModifiers());
					}

					if(possessee instanceof Creeper){
						((Creeper)possessee).ignite();
					}
				}
			}

			if(event.isCancelable()) event.setCanceled(true);
		}
	}

	/** Called via packets to shoot a projectile if the entity currently possessed by the given player can do so. */
	public static void shootProjectile(Player possessor){

		if(WizardData.get(possessor) != null){

			Integer cooldown = WizardData.get(possessor).getVariable(SHOOT_COOLDOWN_KEY);

			if(cooldown == null || cooldown == 0){

				Mob possessee = getPossessee(possessor);

				if(possessee != null){

					if(possessee instanceof EntityLightningWraith){
						Spells.ARC.cast(possessor.level, possessor, InteractionHand.MAIN_HAND, 0, new SpellModifiers());
					}

					if(possessee instanceof Creeper){
						((Possession)Spells.possession).endPossession(possessor);
						((Creeper)possessee).ignite(); // Aaaaaaand.... RUN!
					}

					Function<Level, ? extends Projectile> factory = projectiles.get(possessee.getClass());

					if(factory != null){

						Projectile projectile = factory.apply(possessor.level);
						Vec3 look = possessor.getLookAngle();
						((Entity)projectile).setPos(possessor.getX() + look.x, possessor.getY() + possessor.getEyeHeight() + look.y, possessor.getZ() + look.z);
						projectile.shoot(look.x, look.y, look.z, 1.6f, EntityUtils.getDefaultAimingError(possessor.level.getDifficulty()));

						if(projectile instanceof EntityMagicProjectile) ((EntityMagicProjectile)projectile).setCaster(possessor);
						else if(projectile instanceof EntityMagicArrow) ((EntityMagicArrow)projectile).setCaster(possessor);

						possessor.level.addFreshEntity((Entity)projectile);

					}

					WizardData.get(possessor).setVariable(SHOOT_COOLDOWN_KEY, PROJECTILE_COOLDOWN);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){ // Not fired for revenge-targeting
		if(event.getTarget() instanceof Player && event.getEntity() instanceof Mob){
			Mob possessee = getPossessee((Player)event.getTarget());
			Mob attacker = (Mob)event.getEntity();
			if(possessee != null && !attacker.canAttack(possessee)){
				((Mob)event.getEntity()).setTarget(null); // Mobs can't target a player possessing an entity they don't normally attack
			}
		}
	}

	// With these two methods I'm pretty sure it's watertight

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event){
		if(event.getEntity() instanceof Player && isPossessing((Player)event.getEntity())){
			((Possession)Spells.possession).endPossession((Player)event.getEntity()); // Just in case, to make sure the player drops their items
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
		if(isPossessing(event.getEntity())) ((Possession)Spells.possession).endPossession(event.player);
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){

		Mob possessee = getPossessee(event.getPlayer());

		if(possessee instanceof EnderMan){
			if(((EntityEnderman)possessee).getHeldBlockState() == null){
				((EntityEnderman)possessee).setHeldBlockState(event.getState());
				event.getPlayer().setHeldItem(EnumHand.MAIN_HAND, new ItemStack(event.getState().getBlock()));
				event.setExpToDrop(0);
				event.getWorld().setBlockToAir(event.getPos()); // Remove block before it can drop
			}else{
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(BlockEvent.PlaceEvent event){

		Mob possessee = getPossessee(event.getPlayer());

		if(possessee instanceof EnderMan){
			if(((EnderMan)possessee).getCarriedBlock() == event.getState()){
				((EnderMan)possessee).setCarriedBlock(null);
			}else{
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onEntityItemPickupEvent(EntityItemPickupEvent event){ // Why are there two item pickup events?

		Mob possessee = getPossessee(event.getEntity());

		if(possessee != null){

			event.setCanceled(true);

//			if(possessee.canPickUpLoot() && possessee.getMainHandItem().isEmpty()){
//				possessee.setHeldItem(EnumHand.MAIN_HAND, event.getItem().getItem());
//			}else{
//				event.setCanceled(true);
//			}
		}
	}

	@SubscribeEvent
	static void onItemTossEvent(ItemTossEvent event){
		if(isPossessing(event.getPlayer())){ // Can't drop items while possessing
			event.setCanceled(true);
			event.getPlayer().getInventory().add(event.getEntity().getItem());
		}
	}

	@SubscribeEvent
	public static void onAttackEntityEvent(AttackEntityEvent event){

		Mob possessee = getPossessee(event.getEntity());

		if(possessee == null) return;

		if(possessee instanceof Creeper){
			event.setCanceled(true); // Why do creepers have a melee AI?!
		}else if(possessee.goalSelector.getAvailableGoals().stream().noneMatch(t -> t.getGoal() instanceof MeleeAttackGoal)){
			event.setCanceled(true); // Can't melee with a non-melee mob
		}
	}

}
