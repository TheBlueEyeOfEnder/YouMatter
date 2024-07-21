package realmayus.youmatter;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import realmayus.youmatter.encoder.EncoderScreen;
import realmayus.youmatter.scanner.ScannerScreen;

import java.util.List;

// The value here should match an entry in the META-INF/neoforge.neoforge.mods.toml file
@Mod(YouMatter.MODID)
// @Mod.EventBusSubscriber(modid = "youmatter")
public class YouMatter {
    public static final String MODID = "youmatter";
    public static final Logger logger = LogManager.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(ModContent.THUMBDRIVE_ITEM.get()))
            .title(Component.literal("YouMatter")) //todo localize
            .displayItems((displayParameters, output) -> {
                output.acceptAll(List.of(
                    /*    new ItemStack(ModContent.SCANNER_BLOCK.get()),
                        new ItemStack(ModContent.ENCODER_BLOCK.get()),
                        new ItemStack(ModContent.CREATOR_BLOCK.get()),
                        new ItemStack(ModContent.REPLICATOR_BLOCK.get()), */
                        new ItemStack(ModContent.MACHINE_CASING_ITEM.get()),
                        new ItemStack(ModContent.BLACK_HOLE_ITEM.get()),
                        new ItemStack(ModContent.COMPUTE_MODULE_ITEM.get()),
                        new ItemStack(ModContent.TRANSISTOR_RAW_ITEM.get()),
                        new ItemStack(ModContent.TRANSISTOR_ITEM.get()),
                        new ItemStack(ModContent.THUMBDRIVE_ITEM.get())));
                       /* new ItemStack(ModContent.UMATTER_BUCKET.get()),
                        new ItemStack(ModContent.STABILIZER_BUCKET.get()))); */
            }).build());

    public YouMatter(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, YMConfig.CONFIG_SPEC);
        ModContent.init(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerScreens);
       // modEventBus.addListener(this::registerPayloads);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.SCANNER_MENU.get(), ScannerScreen::new);
        event.register(ModContent.ENCODER_MENU.get(), EncoderScreen::new);
        /* event.register(ModContent.REPLICATOR_MENU.get(), ReplicatorScreen::new);
        event.register(ModContent.CREATOR_MENU.get(), CreatorScreen::new); */
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModContent.SCANNER_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModContent.SCANNER_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModContent.ENCODER_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModContent.ENCODER_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());
       /* event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModContent.REPLICATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModContent.REPLICATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModContent.REPLICATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getFluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModContent.CREATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModContent.CREATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModContent.CREATOR_BLOCK_ENTITY.get(), (o, direction) -> o.getFluidHandler());
    }

   @SubscribeEvent
    public void registerPayloads(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("youmatter");
        registrar.play(PacketSettingsCreator.ID, PacketSettingsCreator::new, handler ->
               handler.server(PacketHandler.CreatorSettings.getInstance()::handle));
        registrar.play(PacketSettingsReplicator.ID, PacketSettingsReplicator::new, handler ->
               handler.server(PacketHandler.ReplicatorSettings.getInstance()::handle));
        registrar.play(PacketShowNext.ID, PacketShowNext::new, handler ->
               handler.server(PacketHandler.ShowNext.getInstance()::handle));
        registrar.play(PacketShowPrevious.ID, PacketShowPrevious::new, handler ->
               handler.server(PacketHandler.ShowPrevious.getInstance()::handle));
    } */
    }
}