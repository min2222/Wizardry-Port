package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityZombieSpawner;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ZombieApocalypse extends SpellConstruct<EntityZombieSpawner> {

	public static final String MINION_SPAWN_INTERVAL = "minion_spawn_interval";

	private static final int SPAWNER_HEIGHT = 8;
	private static final int MIN_SPAWNER_HEIGHT = 3;

	public ZombieApocalypse(){
		super("zombie_apocalypse", SpellActions.POINT_UP, EntityZombieSpawner::new, false);
		addProperties(SpellMinion.MINION_LIFETIME, MINION_SPAWN_INTERVAL);
		this.soundValues(1.3f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return createContinuousSpellSounds();
	}

	@Override
	protected boolean spawnConstruct(Level world, double x, double y, double z, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){

		Integer ceiling = BlockUtils.getNearestSurface(world, new BlockPos(x, y + MIN_SPAWNER_HEIGHT, z),
				Direction.UP, SPAWNER_HEIGHT - MIN_SPAWNER_HEIGHT, false, BlockUtils.SurfaceCriteria.COLLIDABLE.flip());

		if(ceiling == null) y += SPAWNER_HEIGHT;
		else y = ceiling - 0.5;

		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityZombieSpawner construct, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){
		construct.spawnHusks = caster instanceof Player && ItemArtefact.isArtefactActive((Player)caster, WizardryItems.charm_minion_variants);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y + SPAWNER_HEIGHT, z, 0, (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
	}
}
