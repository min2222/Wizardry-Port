package electroblob.wizardry.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;

/** Structure template processor that allows multiple processors to be run in order. */
public class MultiTemplateProcessor implements ITemplateProcessor {

	private final ITemplateProcessor[] processors;
	private final boolean stopWhenNull;

	/**
	 * Creates a new {@code MultiTemplateProcessor} which applies the given processors in order.
	 * @param stopWhenNull True to skip any remaining processors in the sequence if one of them returns null, false to
	 *                     process them all regardless. If this is false, you should ensure all the given processors
	 *                     accept null {@link net.minecraft.world.gen.structure.template.Template.BlockInfo} arguments.
	 * @param processors The processors to be run, in order (i.e. the first one given will be applied first).
	 */
	public MultiTemplateProcessor(boolean stopWhenNull, ITemplateProcessor... processors){
		this.processors = processors;
		this.stopWhenNull = stopWhenNull;
	}

	@Nullable
	@Override
	public Template.BlockInfo processBlock(Level world, BlockPos pos, Template.BlockInfo info){

		for(ITemplateProcessor processor : processors){
			info = processor.processBlock(world, pos, info);
			if(stopWhenNull && info == null) break;
		}

		return info;
	}
}