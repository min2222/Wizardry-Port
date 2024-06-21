package electroblob.wizardry.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import electroblob.wizardry.Wizardry;
import net.minecraft.world.item.UseAnim;

/**
 * Defines and stores wizardry's custom {@link UseAnim}s.
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class SpellActions {

	private static final List<UseAnim> spellActions = new ArrayList<>();

	/** An {@link UseAnim} that causes the player to point in the direction they are looking. */
	public static final UseAnim POINT = createAction("point");
	/** An {@link UseAnim} that causes the player to point directly upwards. */
	public static final UseAnim POINT_UP = createAction("point_up");
	/** An {@link UseAnim} that causes the player to point down towards the ground. */
	public static final UseAnim POINT_DOWN = createAction("point_down");
	/** An {@link UseAnim} that causes the player to stretch both arms out and up slightly, as if summoning. */
	public static final UseAnim SUMMON = createAction("summon");
	/** An {@link UseAnim} that causes the player to hold their item vertically in front of them. */
	public static final UseAnim THRUST = createAction("thrust");
	/** An {@link UseAnim} that causes the player to point the item in use towards their other hand. */
	public static final UseAnim IMBUE = createAction("imbue");
	/** An {@link UseAnim} that causes the player to point towards their grappling target (see
	 * {@link electroblob.wizardry.spell.Grapple Grapple}). */
	public static final UseAnim GRAPPLE = createAction("grapple");

	private SpellActions(){} // No instances!

	private static UseAnim createAction(String name){
		return createAction(Wizardry.MODID, name);
	}

	/**
	 * Creates a new {@link UseAnim} with the given mod ID and name and adds it to the internal spell action list.
	 * Use this method to add extra spell actions for use with wizardry's player animator.
	 *
	 * @param modID The ID of the mod adding this action (avoids naming conflicts)
	 * @param name The name of the action (for Forge to use as the in-code name; should only contain characters that
	 *             can be used in Java identifiers)
	 * @return The resulting {@code EnumAction}
	 */
	public static UseAnim createAction(String modID, String name){
		// Using $ because this name will be used as the in-code name of the enum constant, and : wouldn't compile
		UseAnim action = UseAnim.CUSTOM;
		spellActions.add(action);
		return action;
	}

	/** Returns an unmodifiable list of all spell actions. */
	public static List<UseAnim> getSpellActions(){
		return Collections.unmodifiableList(spellActions);
	}

}
