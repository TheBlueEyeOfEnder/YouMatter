package realmayus.youmatter;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class YMConfig {

    public static final ModConfigSpec CONFIG_SPEC;
    public static final YMConfig CONFIG;

    public final ModConfigSpec.ConfigValue<List<? extends String>> filterItems;
    public final ModConfigSpec.BooleanValue filterMode;
    public final ModConfigSpec.ConfigValue<List<? extends String>> overrides;
    public final ModConfigSpec.BooleanValue useRecursion;
    public final ModConfigSpec.ConfigValue<Integer> defaultAmount;

    public final ModConfigSpec.ConfigValue<Integer> energyReplicator;
    public final ModConfigSpec.ConfigValue<Integer> energyEncoder;
    public final ModConfigSpec.ConfigValue<Integer> energyScanner;

    public final ModConfigSpec.ConfigValue<Integer> productionPerTick;

    static {
        Pair<YMConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(YMConfig::new);
        CONFIG_SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    YMConfig(ModConfigSpec.Builder builder) {
        filterMode = builder
                .comment("Use the filterItems list as blacklist (true) or as whitelist (false). Whitelist means, that you can only duplicate those items in that list. Blacklist is vice-versa.")
                .define("filterMode", true);
        filterItems = builder
                .comment("List of items that are being treated specially. See filterMode for further details. Format: \"modid:item\"")
                .defineList("filterItems", Lists.newArrayList("youmatter:umatter_bucket", "youmatter:stabilizer_bucket", "youmatter:black_hole"), e -> e instanceof String && ((String) e).contains(":"));
        overrides = builder
                .comment("Overrides: Set your desired required U-Matter values for each item. These do not apply when you e.g. have whitelist on but it doesn't include the desired override. Format: \"modid:item=amount\"")
                .defineList("overrides", Lists.newArrayList("minecraft:diamond=2500", "minecraft:nether_star=5000"), e -> e instanceof String && ((String) e).contains(":") && ((String) e).contains("="));
        useRecursion = builder
                .comment("When this option is enabled, recursively calculate the final U-Matter value for an item by summing the U-Matter values of all items in its crafting recipe.")
                .comment("Warning: This feature is experimental!")
                .define("useRecursion", false);
        defaultAmount = builder
                .comment("The default amount that is required to duplicate an item if it is not overridden.")
                .define("defaultAmount", 1000);
        energyReplicator = builder
                .comment("The energy consumption of the replicator per tick. Default: 2048")
                .define("energyReplicator", 2048);
        energyEncoder = builder
                .comment("The energy consumption of the encoder per tick. Default: 512")
                .define("energyEncoder", 512);
        energyScanner = builder
                .comment("The energy consumption of the scanner per tick. Default: 512")
                .define("energyScanner", 512);
        productionPerTick = builder
                .comment("Determines how much U-Matter [in mB] the creator produces every work cycle. Energy is withdrawn like this: if energy more than 30% of max energy, consume 30% and add [whatever value below] of U-Matter to the tank. Default is 1mB/work cycle. Don't increase this too much due to balancing issues.")
                .define("productionPerTick", 1);
    }

    public Object[] getOverride(String registryName) {
        for(String s : overrides.get()) {
            String foundName = s.substring(0, s.indexOf('='));
            String foundValue = s.substring(s.indexOf('=')).substring(1);
            if (foundName.equalsIgnoreCase(registryName)) {
                return new Object[]{foundName, foundValue};
            }
        }
        return null;
    }
}
