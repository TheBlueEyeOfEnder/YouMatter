package realmayus.youmatter.items;

import net.minecraft.world.item.Item;
import realmayus.youmatter.ModContent;

import java.util.HashSet;

public class ThumbdriveItem extends Item {
    public ThumbdriveItem() {
        super(new Properties().stacksTo(1).component(ModContent.ITEMS_STORED_DATA.get(), new HashSet<Item>()));
    }
}
