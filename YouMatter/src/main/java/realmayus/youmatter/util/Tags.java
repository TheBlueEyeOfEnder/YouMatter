package realmayus.youmatter.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import realmayus.youmatter.YouMatter;

public class Tags {
    public static final TagKey<Fluid> STABILIZER = create("stabilizer");

    public static TagKey<Fluid> create(String name) {
        return FluidTags.create(new ResourceLocation(YouMatter.MODID, name));
    }
}
