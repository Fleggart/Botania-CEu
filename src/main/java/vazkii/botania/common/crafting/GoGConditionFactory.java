/**
 * This class was created by <wizzerinus>. It's distributed as
 * part of the Botania CEu Mod. Get the Source Code in github:
 * https://github.com/TeamDimensional/Botania-CEU
 *
 * Botania CEu is Open Source and distributed under the
 * Botania CEu License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import vazkii.botania.common.core.handler.ConfigHandler;

import java.util.function.BooleanSupplier;

public class GoGConditionFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        JsonElement element = json.get("value");
        // check if element is a string
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Garden of Glass crafting condition must be a primitive");
        }
        JsonPrimitive p = element.getAsJsonPrimitive();
        if (p.isBoolean()) {
            return () -> ConfigHandler.registerGogRecipes == p.getAsBoolean();
        }
        if (p.isString()) {
            String s = p.getAsString();
            if (s.equals("recipes"))
                return () -> ConfigHandler.registerGogRecipes;
            if (s.equals("resources"))
                return () -> ConfigHandler.registerGogRecipes && ConfigHandler.registerGogResources;
            throw new IllegalArgumentException("Garden of Glass crafting condition must be boolean, 'recipes' or 'resources'");
        }
        throw new IllegalArgumentException("Garden of Glass crafting condition must be boolean, 'recipes' or 'resources'");
    }
}
