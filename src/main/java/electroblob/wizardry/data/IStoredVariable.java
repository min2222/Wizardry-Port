package electroblob.wizardry.data;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import electroblob.wizardry.util.NBTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Extension of {@link IVariable} which adds NBT read/write methods. Instances of this interface must be
 * registered on load using {@link WizardData#registerStoredVariables(IStoredVariable...)} in order for NBT storage
 * to work. A good place to do this is in spell constructors, if that's where the variable is being used.
 * <p></p>
 * This interface is provided for complex cases that require custom NBT handling of some kind. In most cases,
 * {@link StoredVariable} should be sufficient.
 * <p></p>
 * @param <T> The type of variable stored.
 */
public interface IStoredVariable<T> extends IVariable<T> {

	/** Writes the value to the given NBT tag. */
	void write(CompoundTag nbt, T value);

	/** Reads the value from the given NBT tag. */
	T read(CompoundTag nbt);

	/**
	 * General-purpose implementation of {@link IStoredVariable}. In most cases, this should be sufficient. This class
	 * also contains a number of static methods for common implementations (primitives, {@code String}, {@code UUID},
	 * {@code BlockPos} and {@code ItemStack}).
	 * <p></p>
	 * @param <T> The type of variable stored.
	 * @param <E> The type of NBT tag the variable will be stored as.
	 */
	class StoredVariable<T, E extends Tag> implements IStoredVariable<T> {

		private final String key;
		private final Persistence persistence;

		private final Function<T, E> serialiser;
		private final Function<E, T> deserialiser;

		private boolean synced;

		private BiFunction<Player, T, T> ticker;

		/**
		 * Creates a new {@code StoredVariable} with the given key and serialisation behaviour.
		 * @param key The string key used to write the value to NBT (should be unique). This serves no other purpose.
		 * @param serialiser A function used to write the value to NBT.
		 * @param deserialiser A function used to read the value from NBT.
		 */
		public StoredVariable(String key, Function<T, E> serialiser, Function<E, T> deserialiser, Persistence persistence){
			this.key = key;
			this.serialiser = serialiser;
			this.deserialiser = deserialiser;
			this.persistence = persistence;
			this.ticker = (p, t) -> t; // Initialise this with a do-nothing function, can be overwritten later
		}

		@Override
		public String getKey() {
			return key;
		}

		/**
		 * Replaces this variable's update method with the given update function. <i>Beware of auto-unboxing of
		 * primitive types! For lambda expressions, check the second parameter isn't null before operating on it.
		 * For method references, do not reference a method that takes a primitive type. Otherwise, this will cause
		 * a (difficult to debug) {@link NullPointerException} if the key was not stored.</i>
		 * @param ticker A {@link BiFunction} specifying the actions to be performed on this variable each tick. The
		 *               {@code BiFunction} returns the new value for this variable.
		 * @return This {@code StoredVariable} object, allowing this method to be chained onto object creation.
		 */
		public StoredVariable<T, E> withTicker(BiFunction<Player, T, T> ticker){
			this.ticker = ticker;
			return this;
		}

		/**
		 * Adds synchronisation to this variable, meaning it will be sent to clients whenever {@link WizardData#sync()}
		 * is called (this always happens on player login, but other than that you'll need to do it yourself).
		 * @return This {@code StoredVariable} object, allowing this method to be chained onto object creation.
		 */
		public StoredVariable<T, E> setSynced(){
			this.synced = true;
			return this;
		}

		@Override
		public void write(CompoundTag nbt, T value){
			if(value != null) NBTExtras.storeTagSafely(nbt, key, serialiser.apply(value));
		}

		@Override
		@SuppressWarnings("unchecked") // Can't check it due to type erasure
		public T read(CompoundTag nbt){
			// A system allowing any kind of variable to be stored on the fly cannot be made without casting somewhere.
			// However, doing it like this means we only cast once, below, and proper regulation of access means we
			// can effectively guarantee the cast is safe.
			return nbt.contains(key) ? deserialiser.apply((E)nbt.get(key)) : null; // Still gotta check it ain't null
		}

		@Override
		public T update(Player player, T value){
			return ticker.apply(player, value);
		}

		@Override
		public boolean isPersistent(boolean respawn){
			return respawn ? persistence.persistsOnRespawn() : persistence.persistsOnDimensionChange();
		}

		@Override
		public boolean isSynced(){
			return synced;
		}

		@Override
		public void write(FriendlyByteBuf buf, T value){
			if(!synced) return;
			CompoundTag nbt = new CompoundTag();
			write(nbt, value);
			buf.writeNbt(nbt); // Sure, it's not super-efficient, but it's by far the simplest way!
		}

		@Override
		public T read(FriendlyByteBuf buf){
			if(!synced) return null; // Better to check in here because this method should only read if it needs to
			CompoundTag nbt = buf.readNbt();
			if(nbt == null) return null;
			return read(nbt);
		}

		// Standard implementations to shorten common usages a bit

		/** Creates a new {@code StoredVariable} for a byte value with the given key. */
		public static StoredVariable<Byte, ByteTag> ofByte(String key, Persistence persistence){
			return new StoredVariable<>(key, ByteTag::valueOf, ByteTag::getAsByte, persistence);
		}

		/** Creates a new {@code StoredVariable} for a boolean value with the given key. As per Minecraft's usual
		 * NBT conventions, the boolean value is stored as an {@link NBTTagByte} (1 = true, 0 = false). */
		public static StoredVariable<Boolean, ByteTag> ofBoolean(String key, Persistence persistence){
			return new StoredVariable<>(key, b -> ByteTag.valueOf((byte)(b?1:0)), t -> t.getAsByte() == 1, persistence);
		}

		/** Creates a new {@code StoredVariable} for an integer value with the given key. */
		public static StoredVariable<Integer, IntTag> ofInt(String key, Persistence persistence){
			return new StoredVariable<>(key, IntTag::valueOf, IntTag::getAsInt, persistence);
		}

		// I'm not going to do byte and long arrays here, if you really need them it's pretty obvious how to do it

		/** Creates a new {@code StoredVariable} for an integer array value with the given key. */
		public static StoredVariable<int[], IntArrayTag> ofIntArray(String key, Persistence persistence){
			return new StoredVariable<>(key, IntArrayTag::new, IntArrayTag::getAsIntArray, persistence);
		}

		/** Creates a new {@code StoredVariable} for a float value with the given key. */
		public static StoredVariable<Float, FloatTag> ofFloat(String key, Persistence persistence){
			return new StoredVariable<>(key, FloatTag::valueOf, FloatTag::getAsFloat, persistence);
		}

		/** Creates a new {@code StoredVariable} for a double value with the given key. */
		public static StoredVariable<Double, DoubleTag> ofDouble(String key, Persistence persistence){
			return new StoredVariable<>(key, DoubleTag::valueOf, DoubleTag::getAsDouble, persistence);
		}

		/** Creates a new {@code StoredVariable} for a short value with the given key. */
		public static StoredVariable<Short, ShortTag> ofShort(String key, Persistence persistence){
			return new StoredVariable<>(key, ShortTag::valueOf, ShortTag::getAsShort, persistence);
		}

		/** Creates a new {@code StoredVariable} for a long value with the given key. */
		public static StoredVariable<Long, LongTag> ofLong(String key, Persistence persistence){
			return new StoredVariable<>(key, LongTag::valueOf, LongTag::getAsLong, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link String} value with the given key. */
		public static StoredVariable<String, StringTag> ofString(String key, Persistence persistence){
			return new StoredVariable<>(key, StringTag::valueOf, StringTag::getAsString, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link BlockPos} value with the given key. */
		public static StoredVariable<BlockPos, CompoundTag> ofBlockPos(String key, Persistence persistence){
			return new StoredVariable<>(key, NbtUtils::writeBlockPos, NbtUtils::readBlockPos, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link UUID} value with the given key. */
		public static StoredVariable<UUID, IntArrayTag> ofUUID(String key, Persistence persistence){
			return new StoredVariable<>(key, NbtUtils::createUUID, NbtUtils::loadUUID, persistence);
		}

		/** Creates a new {@code StoredVariable} for an {@link ItemStack} value with the given key. */
		public static StoredVariable<ItemStack, CompoundTag> ofItemStack(String key, Persistence persistence){
			return new StoredVariable<>(key, ItemStack::serializeNBT, ItemStack::of, persistence);
		}

		/** Creates a new {@code StoredVariable} for an {@link NBTTagCompound} value with the given key. */
		public static StoredVariable<CompoundTag, CompoundTag> ofNBT(String key, Persistence persistence){
			return new StoredVariable<>(key, t -> t, t -> t, persistence); // No conversion required!
		}

		// Neither of these work just ignore them

//		/** Creates a new {@code StoredVariable} for an {@link NBTTagCompound} value with the given key which stores the
//		 * given {@code IVariable} for an entity. Entities cannot be stored directly as an {@code IStoredVariable}
//		 * because they require a world instance on construction. */
//		@SuppressWarnings("unchecked") // Can't check it due to type erasure
//		public static <T extends Entity> StoredVariable<NBTTagCompound, NBTTagCompound> ofNBTForEntity(String key, Persistence persistence, IVariable<T> toStore){
//			return ofNBT(key, persistence).withTicker((p, t) -> {
//				if(WizardData.get(p) != null){
//					try{
//						T e = (T)EntityList.createEntityByIDFromName(new ResourceLocation(t.getString("entityType")), p.world);
//						e.readFromNBT(t);
//						WizardData.get(p).setVariable(toStore, e);
//					}catch(ClassCastException e){
//						Wizardry.logger.error("Error reading entity from NBT: entity not of expected type", e);
//					}
//				}
//				return t;
//			});
//		}

//		/** Creates a new {@code StoredVariable} for an {@link Entity} value with the given key. The returned
//		 * {@code StoredVariable} has a ticker which extracts the entity from the given; this functionality will need to be
//		 * replicated in any replacement ticker function. */
//		@SuppressWarnings("unchecked") // Can't check it due to type erasure
//		public static <T extends Entity> StoredVariable<T, NBTTagCompound> ofEntity(String key, Persistence persistence, IVariable<NBTTagCompound> storage){
//			// Well this is horrible
//			return new IStoredVariable.StoredVariable<>(key,
//					(T e) -> {
//						NBTTagCompound nbt = new NBTTagCompound();
//						nbt.setString("entityType", EntityList.getKey(e).toString());
//						e.writeToNBT(nbt);
//						return nbt;
//					},
//					t -> null, persistence)
//					.withTicker((p, e) -> {
//						if(e == null){
//							try{
//								NBTTagCompound nbt = WizardData.get(p).getVariable(storage);
//								if(nbt == null) return null;
//								e = (T)EntityList.createEntityByIDFromName(new ResourceLocation(nbt.getString("entityType")), p.world);
//								e.readFromNBT(nbt);
//								return e;
//							}catch(ClassCastException x){
//								Wizardry.logger.error("Error reading stored variable from NBT: entity not of expected type", x);
//							}
//						}
//						return null;
//					});
//		}
	}
}
