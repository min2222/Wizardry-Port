package electroblob.wizardry.spell;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockCrystalOre;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.RelativeFacing;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.phys.Vec3;

public class Divination extends Spell {

	private static final float NUDGE_SPEED = 0.2f;

	public Divination(){
		super("divination", SpellActions.THRUST, false);
		addProperties(RANGE);
	}

	/** A set of constants representing the different 'signal strengths' for the divination spell. In practical
	 * terms, this means different chat readouts and effects. */
	protected enum Strength {

		NOTHING("nothing", -1),
		WEAK("weak", 0),
		MODERATE("moderate", 0.25f),
		STRONG("strong", 0.5f),
		VERY_STRONG("very_strong", 0.75f);

		String key;
		float minWeight;

		Strength(String key, float minWeight){
			this.key = key;
			this.minWeight = minWeight;
		}

		protected static Strength forWeight(float weight){
			return Arrays.stream(values()).filter(s -> s.minWeight < weight).max(Comparator.naturalOrder()).orElse(NOTHING);
		}
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		List<BlockPos> sphere = BlockUtils.getBlockSphere(caster.blockPosition(), range);

		sphere.removeIf(b -> {
			Block block = world.getBlockState(b).getBlock();
			return !(block instanceof DropExperienceBlock
								|| block instanceof RedStoneOreBlock
								|| block instanceof BlockCrystalOre
								|| Settings.containsMetaBlock(Wizardry.settings.divinationOreWhitelist, world.getBlockState(b)));
		});

		Strength strength = Strength.NOTHING;

		Direction direction = Direction.DOWN; // Doesn't matter what this is

		if(!sphere.isEmpty()){

			// Sorts the positions based on weight (see below), in ascending order
			sphere.sort(Comparator.comparingDouble(b -> calculateWeight(world, caster, b, range, modifiers)));

			// The weights are sorted in ascending order, so this must be the largest
			BlockPos target = sphere.get(sphere.size() - 1);

			direction = Direction.getNearest((float)(target.getX() + 0.5 - caster.getX()),
					(float)(target.getY() + 0.5 - (caster.getY() + caster.getEyeHeight())),
					(float)(target.getZ() + 0.5 - caster.getZ()));

			strength = Strength.forWeight(calculateWeight(world, caster, target, range, modifiers));
		}

		if(!world.isClientSide){
			caster.displayClientMessage(Component.translatable("spell." + this.getUnlocalisedName() + "."
					+ strength.key, Component.translatable("spell." + this.getUnlocalisedName() + "."
					+ RelativeFacing.relativise(direction, caster).name)), false);
		}else{
			switch(strength){
				case NOTHING: break;
				case WEAK: break;
				case MODERATE:
					spawnHintParticles(world, caster, 3, direction);
					break;
				case STRONG:
					spawnHintParticles(world, caster, 8, direction);
					break;
				case VERY_STRONG:
					spawnHintParticles(world, caster, 12, direction);
					caster.push(direction.getStepX() * NUDGE_SPEED, direction.getStepY() * NUDGE_SPEED,
							direction.getStepZ() * NUDGE_SPEED);
					break;
			}
		}

		return true;
	}

	private static void spawnHintParticles(Level world, Entity caster, int count, Direction direction){

		Vec3 vec = Vec3.atCenterOf(caster.blockPosition().relative(Direction.UP).relative(direction, 2));

		for(int i=0; i<count; i++){
			ParticleBuilder.create(ParticleBuilder.Type.FLASH, world.random, vec.x, vec.y, vec.z, 0.7, false)
					.time(20 + world.random.nextInt(5)).clr(0.6f + world.random.nextFloat() * 0.4f,
					0.6f + world.random.nextFloat() * 0.4f, 0.6f + world.random.nextFloat() * 0.4f).scale(0.3f).spawn(world);
		}
	}

	protected static float calculateWeight(Level world, Player caster, BlockPos pos, double range, SpellModifiers modifiers){

		Block block = world.getBlockState(pos).getBlock();
		// On a non-sorcery wand, the value of the ore has no effect on its weight
		float weightModifier = modifiers.get(SpellModifiers.POTENCY) - 1;

		// xp is a decent way of determining the 'value' of a block
		// There is a degree of randomness associated with it though...
        float xp = block.getExpDrop(world.getBlockState(pos), world, world.random, pos, 0, 0);
		// For some reason smelting gives a lot less than mining, hence the multiplying by 4
        Optional<SmeltingRecipe> optional = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack(block)), world);
        if (optional.isPresent()) {
            if (xp == 0) xp = 4 * optional.get().getExperience();
        }

		// By my (rather rough) calculations, this should mean that using a master sorcerer wand, an iron ore block
		// and a diamond ore block just under twice as far away as the iron ore should have about the same weight
		return (float)(1 - caster.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)/range + 0.2 * weightModifier * xp);
	}

}
