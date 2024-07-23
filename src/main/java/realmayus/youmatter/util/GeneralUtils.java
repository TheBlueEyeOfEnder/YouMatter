package realmayus.youmatter.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import realmayus.youmatter.YMConfig;

import java.util.ArrayList;
import java.util.List;

public class GeneralUtils {

    /**
     * Sorts all available recipes in lists of recipes that consist of lists of required items that consist of a list of available variants.
     * @param manager a RecipeManager object.
     * @return every recipe that is available in a handy list.
     */
    public static List<Recipe<?>> getMatchingRecipes(RegistryAccess registryAccess, RecipeManager manager, ItemStack is) { // List of Recipes > List of Required Items For that recipe > List of allowed ItemStacks as an ingredient (see OreDict)
        List<Recipe<?>> returnValue = new ArrayList<>();
        for(RecipeHolder<?> recipe : manager.getRecipes()) {
            if(ItemStack.isSameItem(recipe.value().getResultItem(registryAccess), is)) {
                returnValue.add(recipe.value());
            }
        }
        return returnValue;
    }

    public static int getUMatterValueRecursively(ItemStack is, RegistryAccess registryAccess, RecipeManager manager) {
        int totalUMatterValue = 0;

        // Get the recipes that match the item stack
        List<Recipe<?>> matchingRecipes = getMatchingRecipes(registryAccess, manager, is);

        // If there are no matching recipes, get the U-Matter value for the item
        if (matchingRecipes.isEmpty()) {
            totalUMatterValue += getUMatterAmountForItem(is.getItem());
        } else {
            // For each matching recipe, get the U-Matter value for each ingredient
            for (Recipe<?> recipe : matchingRecipes) {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    ItemStack[] stacks = ingredient.getItems();
                    totalUMatterValue += getUMatterAmountForItem(stacks);
                }
            }
        }

        return totalUMatterValue;
    }


    public static int getUMatterAmountForItem(Item item) {
        if(YMConfig.CONFIG.getOverride(RegistryUtil.getRegistryName(item).toString()) != null) {
            return Integer.parseInt((String)YMConfig.CONFIG.getOverride(RegistryUtil.getRegistryName(item).toString())[1]);
        } else {
            return YMConfig.CONFIG.defaultAmount.get();
        }
    }

    public static int getUMatterAmountForItem(ItemStack[] items) {
        for(ItemStack item : items) {
            if (hasCustomUMatterValue(item)) {
                return getUMatterAmountForItem(item.getItem());
            }
        }
        return YMConfig.CONFIG.defaultAmount.get();
    }

    public static boolean hasCustomUMatterValue(ItemStack item) {
        return YMConfig.CONFIG.getOverride(RegistryUtil.getRegistryName(item.getItem()).toString()) != null;
    }

    public static boolean hasCustomUMatterValue(ItemStack[] items) {
        for(ItemStack is : items) {
            if(YMConfig.CONFIG.getOverride(RegistryUtil.getRegistryName(is.getItem()).toString()) != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean canAddItemToSlot(ItemStack slotStack, ItemStack givenStack, boolean stackSizeMatters) {
        boolean flag = slotStack.isEmpty();
        if (!flag && ItemStack.isSameItem(givenStack, slotStack) /*&& ItemStack.areItemStackTagsEqual(slotStack, givenStack)*/) {
            return slotStack.getCount() + (stackSizeMatters ? 0 : givenStack.getCount()) <= givenStack.getMaxStackSize();
        } else {
            return flag;
        }
    }
}
