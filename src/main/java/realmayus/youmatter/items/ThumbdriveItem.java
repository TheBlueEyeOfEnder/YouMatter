package realmayus.youmatter.items;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import realmayus.youmatter.ModContent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThumbdriveItem extends Item {
    public ThumbdriveItem() {
        super(new Properties().stacksTo(1).component(ModContent.ITEMS_STORED_DATA.get(), new HashSet<Item>()));
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if (stack.get(ModContent.ITEMS_STORED_DATA.get()) == null) {
            stack.set(ModContent.ITEMS_STORED_DATA.get(), new HashSet<Item>());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Set<Item> itemsStored = stack.get(ModContent.ITEMS_STORED_DATA.get());
        if (itemsStored != null)
            if (!itemsStored.isEmpty()) {
                tooltip.add(Component.literal(I18n.get("youmatter.tooltip.dataStored")));
                tooltip.add(Component.literal(I18n.get("youmatter.tooltip.remainingSpace", itemsStored.size(), 8)));
            } else {
                tooltip.add(Component.literal(I18n.get("youmatter.tooltip.noDataStored")));
            }
    }
}
