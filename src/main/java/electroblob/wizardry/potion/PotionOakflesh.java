package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionOakflesh extends PotionMagicEffect {

	public PotionOakflesh(MobEffectCategory category, int liquidColour) {
		super(category, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/oakflesh.png"));
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				"98b4ba66-7c50-4a4c-9f3f-40bcb37313b5", -0.1f, EntityUtils.Operations.MULTIPLY_CUMULATIVE);
		this.addAttributeModifier(Attributes.MAX_HEALTH,
				"ed9d0423-60f4-4998-bd8d-dc7c33bd45b8", 0.2f, EntityUtils.Operations.MULTIPLY_FLAT);
		this.addAttributeModifier(Attributes.ARMOR,
				"0b607c3f-fb14-43d7-96b5-1c1b6f6da242", 3.0f, EntityUtils.Operations.ADD);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
