package realmayus.youmatter.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import realmayus.youmatter.ModContent;

import java.util.HashSet;

public class ThumbdriveItem extends Item {
    public ThumbdriveItem() {
        super(new Properties().stacksTo(1).component(ModContent.ITEMS_STORED_DATA.get(), new HashSet<Item>()));
    }
    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if(stack.get(ModContent.ITEMS_STORED_DATA.get()) == null) {
            stack.set(ModContent.ITEMS_STORED_DATA.get(), new HashSet<Item>());
        }
    }
}
