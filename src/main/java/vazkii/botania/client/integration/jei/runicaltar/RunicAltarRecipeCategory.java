/**
 * This class was created by <williewillus>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p/>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.runicaltar;

import java.awt.Point;
import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibMisc;

public class RunicAltarRecipeCategory implements IRecipeCategory<RunicAltarRecipeWrapper> {

	public static final String UID = "botania.runicAltar";
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;

	public RunicAltarRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(114, 106);
		localizedName = I18n.format("botania.nei.runicAltar");
		overlay = guiHelper.createDrawable(new ResourceLocation("botania", "textures/gui/petalOverlay.png"),
				17, 11, 114, 82);
	}

	@Nonnull
	@Override
	public String getUid() {
		return UID;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		overlay.draw(minecraft, 0, 4);
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RunicAltarRecipeWrapper recipeWrapper,
			@Nonnull IIngredients ingredients) {

		int index = 1, inputCount = ingredients.getInputs(VanillaTypes.ITEM).size();
		boolean configurePos = true;
		if (ConfigHandler.addCatalystsToJEI) {
			index++;
			inputCount--;
			configurePos = false;
			recipeLayout.getItemStacks().init(0, true, 47, 49);
			recipeLayout.getItemStacks().init(1, true, 55, 33);
			recipeLayout.getItemStacks().init(2, true, 39, 33);
			recipeLayout.getItemStacks().set(1, new ItemStack(ModItems.twigWand));
		} else {
			recipeLayout.getItemStacks().init(0, true, 47, 44);
		}
		recipeLayout.getItemStacks().set(0, new ItemStack(ModBlocks.runeAltar));

		double angleBetweenEach = 360.0 / inputCount;
		Point point = new Point(47, 12), center = new Point(47, 44);

		for (List<ItemStack> o : ingredients.getInputs(VanillaTypes.ITEM)) {
			if (configurePos) {
				recipeLayout.getItemStacks().init(index, true, point.x, point.y);
				point = rotatePointAbout(point, center, angleBetweenEach);
			}
			configurePos = true;
			recipeLayout.getItemStacks().set(index, o);
			index += 1;
		}

		recipeLayout.getItemStacks().init(index, false, 86, 11);
		recipeLayout.getItemStacks().set(index, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

	}

	private Point rotatePointAbout(Point in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new Point((int) newX, (int) newY);
	}

	@Nonnull
	@Override
	public String getModName() {
		return LibMisc.MOD_NAME;
	}

}
