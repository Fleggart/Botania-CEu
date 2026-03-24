/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p/>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.petalapothecary;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.InventoryHelper;

public class PetalApothecaryRecipeWrapper implements IRecipeWrapper, ITooltipCallback<ItemStack> {

	private final List<List<ItemStack>> input;
	private final ItemStack output;

	public PetalApothecaryRecipeWrapper(RecipePetals recipe) {
		ImmutableList.Builder<List<ItemStack>> builder = ImmutableList.builder();
		if (ConfigHandler.addCatalystsToJEI) {
			List<ItemStack> petalCatalysts = ConfigHandler.petalApothecaryCatalystsSet.stream()
					.map(InventoryHelper::destringifyStack).collect(Collectors.toList());
			if (petalCatalysts.isEmpty()) {
				// Not 100% correct, but whatever
				petalCatalysts.add(new ItemStack(Items.WHEAT_SEEDS));
			}
			builder.add(petalCatalysts);
		}
		for (Object o : recipe.getInputs()) {
			if (o instanceof ItemStack) {
				builder.add(ImmutableList.of((ItemStack) o));
			}
			if (o instanceof String) {
				builder.add(OreDictionary.getOres((String) o));
			}
		}
		input = builder.build();
		output = recipe.getOutput();
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, input);
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}

	@Override
	public void onTooltip(int i, boolean isInput, ItemStack stack, List<String> tooltip) {
		if (ConfigHandler.addCatalystsToJEI && isInput && i == 2 && ConfigHandler.petalApothecaryCatalystsSet.isEmpty()) {
			tooltip.add(I18n.format("botania.nei.petalApothecaryAnySeeds"));
		}
	}

}
