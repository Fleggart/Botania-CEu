/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p/>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.puredaisy;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.recipe.RecipePureDaisy;
import vazkii.botania.common.core.handler.ConfigHandler;

public class PureDaisyRecipeWrapper implements IRecipeWrapper {

	private List<ItemStack> inputs = ImmutableList.of();
	private ItemStack output = ItemStack.EMPTY;
	private FluidStack fluidInput = null;
	private FluidStack fluidOutput = null;
	private int time;

	public PureDaisyRecipeWrapper(RecipePureDaisy recipe) {
		if (recipe.getInput() instanceof String) {
			inputs = ImmutableList.copyOf(OreDictionary.getOres((String) recipe.getInput()));
		} else if (recipe.getInput() instanceof Block || recipe.getInput() instanceof IBlockState) {
			IBlockState state = recipe.getInput() instanceof IBlockState ? (IBlockState) recipe.getInput()
					: ((Block) recipe.getInput()).getDefaultState();
			Block b = state.getBlock();

			if (FluidRegistry.lookupFluidForBlock(b) != null) {
				fluidInput = new FluidStack(FluidRegistry.lookupFluidForBlock(b), 1000);
			} else {
				inputs = ImmutableList.of(new ItemStack(b, 1, b.getMetaFromState(state)));
			}
		}

		Block outBlock = recipe.getOutputState().getBlock();
		if (FluidRegistry.lookupFluidForBlock(outBlock) != null) {
			fluidOutput = new FluidStack(FluidRegistry.lookupFluidForBlock(outBlock), 1000);
		} else {
			output = new ItemStack(outBlock, 1, outBlock.getMetaFromState(recipe.getOutputState()));
		}
		time = recipe.getTime();
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, inputs);

		if (fluidInput != null) {
			ingredients.setInput(VanillaTypes.FLUID, fluidInput);
		}

		if (!output.isEmpty()) {
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}

		if (fluidOutput != null) {
			ingredients.setOutput(VanillaTypes.FLUID, fluidOutput);
		}
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		if (ConfigHandler.showManaNumbers) {
			// Time is in ticks needed to finish recipe, and ticks are spread randomly
			// between 8 adjacent blocks, so it needs to be multiplied by 8/20, or 2/5
			minecraft.fontRenderer.drawString(Integer.toString(time * 2 / 5) + " sec", 1, 36, 0);
		}
	}

}
