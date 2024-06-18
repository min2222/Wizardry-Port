package electroblob.wizardry.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import electroblob.wizardry.Wizardry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

/**
 * Contains a number of useful static methods for interacting with NBT data, particularly involving collections.
 * This was split off from {@code WizardryUtilities} as of wizardry 4.2 in an effort to make the code easier to navigate.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
public final class NBTExtras {

	private NBTExtras(){} // No instances!

	/**
	 * Generic method that stores any Map to an NBTTagList, given two functions that convert the key and value types in
	 * that map to subclasses of NBTBase. For what it's worth, there is very little point in using this unless you can
	 * use something more concise than an anonymous class to do the conversion. A lambda expression, or better, a method
	 * reference, would fit nicely. For example, take ExtendedPlayer's use of this to store conjured item durations:
	 * <p></p>
	 * <code>properties.setTag("conjuredItems", WizardryUtilities.mapToNBT(this.conjuredItemDurations,
	 * item -> new NBTTagInt(Item.getIdFromItem((Item)item)), NBTTagInt::new));</code>
	 * <p></p>
	 * This is a lot nicer than simply iterating through the map, because for that you need to use the entry list, which
	 * introduces local variables that aren't really necessary. Notice that, since the values V in the map are simply
	 * Integer objects, a simple constructor reference to NBTTagInt::new can be used instead of a lambda expression (the
	 * Integer is auto-unboxed to int).
	 *
	 * @param <K> The type of key stored in the given Map.
	 * @param <V> The type of value stored in the given Map.
	 * @param <L> The subtype of NBTBase that the keys (of type K) will be converted to.
	 * @param <W> The subtype of NBTBase that the values (of type V) will be converted to.
	 * @param map The Map to be stored.
	 * @param keyFunction A Function that converts the keys in the map to NBT objects that can be stored.
	 * @param valueFunction A Function that converts the values in the map to NBT objects that can be stored.
	 * @param keyTagName The tag name to use for the key tags.
	 * @param valueTagName The tag name to use for the value tags.
	 * @return An NBTTagList that represents the given Map.
	 */
	public static <K, V, L extends Tag, W extends Tag> ListTag mapToNBT(Map<K, V> map,
                                                                        Function<K, L> keyFunction, Function<V, W> valueFunction, String keyTagName, String valueTagName){

		ListTag tagList = new ListTag();

		for(Map.Entry<K, V> entry : map.entrySet()){
			CompoundTag mapping = new CompoundTag();
			NBTExtras.storeTagSafely(mapping, keyTagName, keyFunction.apply(entry.getKey()));
			NBTExtras.storeTagSafely(mapping, valueTagName, valueFunction.apply(entry.getValue()));
			tagList.add(mapping);
		}

		return tagList;
	}

	/**
	 * See {@link NBTExtras#mapToNBT(Map, Function, Function, String, String)}; this version is for when the
	 * names of the individual key/value tags are unimportant (they default to "key" and "value" respectively).
	 */
	public static <K, V, L extends Tag, W extends Tag> ListTag mapToNBT(Map<K, V> map,
                                                                        Function<K, L> keyFunction, Function<V, W> valueFunction){
		return mapToNBT(map, keyFunction, valueFunction, "key", "value");
	}

	/**
	 * Generic method that reads a Map from an NBTTagList, given two functions that convert the key and value tag types
	 * into the key and value types in the returned map. The given NBTTagList remains unchanged after calling this
	 * method.
	 *
	 * @param <K> The type of key stored in the returned Map.
	 * @param <V> The type of value stored in the returned Map.
	 * @param <L> The subtype of NBTBase that the keys are stored as.
	 * @param <W> The subtype of NBTBase that the values are stored as.
	 * @param tagList The NBTTagList to be converted. This <b>must</b> be a list of compound tags.
	 * @param keyFunction A Function that converts the generic NBTBase tags in the list to keys of type K for the map.
	 * @param valueFunction A Function that converts the generic NBTBase tags in the list to values of type V for the
	 *        map.
	 * @param keyTagName The tag name used for the key tags.
	 * @param valueTagName The tag name used for the value tags.
	 * @return A Map containing the keys and values stored in the given NBTTagList. Can be empty, but not null.
	 * @throws ClassCastException If the tags are not of the expected type.
	 * @see NBTExtras#mapToNBT(Map, Function, Function, String, String)
	 */
	@SuppressWarnings("unchecked") // Intentional, because throwing an exception is appropriate here.
	public static <K, V, L extends Tag, W extends Tag> Map<K, V> NBTToMap(ListTag tagList,
                                                                          Function<L, K> keyFunction, Function<W, V> valueFunction, String keyTagName, String valueTagName){

		Map<K, V> map = new HashMap<>();

		for(int i = 0; i < tagList.size(); i++){
			CompoundTag mapping = tagList.getCompound(i);
			Tag keyTag = mapping.get(keyTagName);
			Tag valueTag = mapping.get(valueTagName);
			K key = null;
			try{
				key = keyFunction.apply((L)keyTag);
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading map from NBT: unexpected tag type " + getTagTypeName(keyTag.getId()), e);
			}
			V value = null;
			try{
				value = valueFunction.apply((W)valueTag);
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading map from NBT: unexpected tag type " + getTagTypeName(valueTag.getId()),
						e);
			}
			map.put(key, value);
		}

		return map;
	}

	/**
	 * See {@link NBTExtras#NBTToMap(ListTag, Function, Function, String, String)}; this version is for when
	 * the names of the individual key/value tags are unimportant (they default to "key" and "value" respectively).
	 */
	public static <K, V, L extends Tag, W extends Tag> Map<K, V> NBTToMap(ListTag tagList,
                                                                          Function<L, K> keyFunction, Function<W, V> valueFunction){
		return NBTToMap(tagList, keyFunction, valueFunction, "key", "value");
	}

	/**
	 * Stores the given {@link Collection} to an {@link ListTag} and returns it, converting the elements in the
	 * collection to NBT tags (subclasses of {@link Tag}) according to the supplied mapper function.
	 *
	 * @param <E> The type of element stored in the given collection.
	 * @param <T> The NBT tag type that the elements will be converted to.
	 * @param list The collection to be stored.
	 * @param mapper A function that converts the elements in the collection to NBT objects that can be stored.
	 * @return An {@code NBTTagList} that represents the given collection.
	 */
	public static <E, T extends Tag> ListTag listToNBT(Collection<E> list, Function<E, T> mapper){

		ListTag tagList = new ListTag();
		// If the collection is ordered, it will preserve the order, even though we don't know what type it is yet.
		for(E element : list){
			tagList.add(mapper.apply(element));
		}

		return tagList;
	}

	/**
	 * Reads a {@link Collection} from the given {@link ListTag}, given a function that converts the element tag
	 * types to the element types in the returned collection. The given {@code NBTTagList} remains unchanged after
	 * calling this method. Unless the target variable for this method is of type {@code Collection}, you will need to
	 * create a new collection containing the elements in the returned collection via that collection's constructor (e.g.
	 * {@code new HashSet<E>(collection)}).
	 * <p></p>
	 * <i>Although this method returns a Collection rather than any of its subtypes, it uses
	 * an ArrayList internally to guarantee the order of the elements in the returned collection is the same as the
	 * order in which they were stored.</i>
	 *
	 * @param <E> The type of element stored in the returned Collection.
	 * @param <T> The subtype of NBTBase that the elements are stored as.
	 * @param tagList The NBTTagList to be converted.
	 * @param function A Function that converts the generic NBTBase tags in the list to elements for the collection.
	 *        Chances are you will need to cast the NBTBase tag to whichever NBT tag type you are expecting in order to
	 *        access the appropriate getter method.
	 * @return A Collection containing the elements stored in the given NBTTagList. Can be empty, but not null.
	 * @throws ClassCastException If the tags are not of the expected type.
	 */
	@SuppressWarnings("unchecked") // Intentional, because throwing an exception is appropriate here.
	public static <E, T extends Tag> Collection<E> NBTToList(ListTag tagList, Function<T, E> function){
		// Uses an ArrayList to guarantee iteration order, and also to permit duplicate elements (which are
		// perfectly reasonable in this context).
		Collection<E> list = new ArrayList<>();
		// The original tag list should remain unchanged, hence the copy.
		ListTag tagList2 = tagList.copy();

		while(!tagList2.isEmpty()){
			Tag tag = tagList2.remove(0);
			// Why oh why is NBTTagList not parametrised? It even has a tagType field, so it must know!
			try{
				list.add(function.apply((T)tag));
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading list from NBT: unexpected tag type " + getTagTypeName(tag.getId()), e);
			}
		}

		return list;

	}

	/**
	 * Removes the UUID with the given key from the given NBT tag, if any. Why this doesn't exist in vanilla I have
	 * no idea.
	 * <p></p>
	 * <i>Usage note: this method complements {@link CompoundTag#setUniqueId(String, UUID)} and
	 * {@link CompoundTag#getUUID(String)}, which store UUIDs by appending "Most" and "Least" to the given
	 * key to store the most and least significant UUID bits respectively. It will not work for the UUID methods in
	 * {@link NbtUtils}, which store the long values under "M" and "L" in their own compound tag.</i>
	 */
	public static void removeUniqueId(CompoundTag tag, String key){
		tag.remove(key + "Most");
		tag.remove(key + "Least");
	}

	/**
	 * Stores the given NBT tag inside the given NBT tag compound using the given key. Under normal circumstances, this
	 * is equivalent to {@link CompoundTag#setTag(String, Tag)}, but this method performs safety checks to
	 * prevent circular references. If storing the given tag would cause a circular reference, the tag is not stored
	 * and an error is printed to the console.
	 * @param compound The {@link CompoundTag} in which to store the tag.
	 * @param key The key to store the tag under.
	 * @param tag The tag to store.
	 */
	// This is a catch-all fix for issue #299.
	public static void storeTagSafely(CompoundTag compound, String key, Tag tag){

		if(compound == tag || deepContains(tag, compound)){
			Wizardry.logger.error("Cannot store tag of type {} under key '{}' as it would result in a circular reference! Please report this (including your full log) to wizardry's issue tracker.",
					getTagTypeName(tag.getId()), key);
		}else{
			compound.put(key, tag);
		}
	}

	/**
	 * Recursively searches within the first NBT tag for the second NBT tag. This handles both compound and list tags.
	 * @param toSearch The NBT tag to search inside. If this is not a compound or list tag, this method will always
	 *                 return false.
	 * @param searchFor The NBT tag to search for.
	 * @return True if the second tag appears anywhere within the NBT tree contained within the first tag, false if not.
	 */
	public static boolean deepContains(Tag toSearch, Tag searchFor){

		if(toSearch instanceof CompoundTag){

			for(String subKey : ((CompoundTag)toSearch).getAllKeys()){
				Tag subTag = ((CompoundTag)toSearch).get(subKey);
				if(subTag == searchFor || deepContains(subTag, searchFor)) return true;
			}

		}else if(toSearch instanceof ListTag){
			for(Tag subTag : (ListTag)toSearch){
				if(subTag == searchFor || deepContains(subTag, searchFor)) return true;
			}
		}

		return false;
	}

    public static String getTagTypeName(int p_193581_0_) {
        switch (p_193581_0_) {
            case 0:
                return "TAG_End";
            case 1:
                return "TAG_Byte";
            case 2:
                return "TAG_Short";
            case 3:
                return "TAG_Int";
            case 4:
                return "TAG_Long";
            case 5:
                return "TAG_Float";
            case 6:
                return "TAG_Double";
            case 7:
                return "TAG_Byte_Array";
            case 8:
                return "TAG_String";
            case 9:
                return "TAG_List";
            case 10:
                return "TAG_Compound";
            case 11:
                return "TAG_Int_Array";
            case 12:
                return "TAG_Long_Array";
            case 99:
                return "Any Numeric Tag";
            default:
                return "UNKNOWN";
        }
    }
}
