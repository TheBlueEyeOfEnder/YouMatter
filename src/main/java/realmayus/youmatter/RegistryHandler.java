package realmayus.youmatter;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import realmayus.youmatter.creator.CreatorBlock;
import realmayus.youmatter.creator.CreatorContainer;
import realmayus.youmatter.creator.CreatorTile;
import realmayus.youmatter.encoder.EncoderBlock;
import realmayus.youmatter.encoder.EncoderContainer;
import realmayus.youmatter.encoder.EncoderTile;
import realmayus.youmatter.items.*;
import realmayus.youmatter.replicator.ReplicatorBlock;
import realmayus.youmatter.replicator.ReplicatorContainer;
import realmayus.youmatter.replicator.ReplicatorTile;
import realmayus.youmatter.scanner.ScannerBlock;
import realmayus.youmatter.scanner.ScannerContainer;
import realmayus.youmatter.scanner.ScannerTile;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new ScannerBlock().setRegistryName(YouMatter.MODID, "scanner"));
        event.getRegistry().register(new EncoderBlock().setRegistryName(YouMatter.MODID, "encoder"));
        event.getRegistry().register(new CreatorBlock().setRegistryName(YouMatter.MODID, "creator"));
        event.getRegistry().register(new ReplicatorBlock().setRegistryName(YouMatter.MODID, "replicator"));
    }

    @SubscribeEvent
    public static void registerTileEntites(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().register(BlockEntityType.Builder.of(ScannerTile::new, ObjectHolders.SCANNER_BLOCK).build(null).setRegistryName(YouMatter.MODID, "scanner"));
        event.getRegistry().register(BlockEntityType.Builder.of(EncoderTile::new, ObjectHolders.ENCODER_BLOCK).build(null).setRegistryName(YouMatter.MODID, "encoder"));
        event.getRegistry().register(BlockEntityType.Builder.of(CreatorTile::new, ObjectHolders.CREATOR_BLOCK).build(null).setRegistryName(YouMatter.MODID, "creator"));
        event.getRegistry().register(BlockEntityType.Builder.of(ReplicatorTile::new, ObjectHolders.REPLICATOR_BLOCK).build(null).setRegistryName(YouMatter.MODID, "replicator"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new BlockItem(ObjectHolders.SCANNER_BLOCK, new Item.Properties().tab(YouMatter.ITEM_GROUP)).setRegistryName(YouMatter.MODID, "scanner"));
        event.getRegistry().register(new BlockItem(ObjectHolders.ENCODER_BLOCK, new Item.Properties().tab(YouMatter.ITEM_GROUP)).setRegistryName(YouMatter.MODID, "encoder"));
        event.getRegistry().register(new BlockItem(ObjectHolders.CREATOR_BLOCK, new Item.Properties().tab(YouMatter.ITEM_GROUP)).setRegistryName(YouMatter.MODID, "creator"));
        event.getRegistry().register(new BlockItem(ObjectHolders.REPLICATOR_BLOCK, new Item.Properties().tab(YouMatter.ITEM_GROUP)).setRegistryName(YouMatter.MODID, "replicator"));
        event.getRegistry().register(new ThumbdriveItem().setRegistryName(YouMatter.MODID, "thumb_drive"));
        event.getRegistry().register(new BlackHoleItem().setRegistryName(YouMatter.MODID, "black_hole"));
        event.getRegistry().register(new MachineCasingItem().setRegistryName(YouMatter.MODID, "machine_casing"));
        event.getRegistry().register(new ComputeModuleItem().setRegistryName(YouMatter.MODID, "compute_module"));
        event.getRegistry().register(new TransistorItem().setRegistryName(YouMatter.MODID, "transistor"));
        event.getRegistry().register(new TransistorRawItem().setRegistryName(YouMatter.MODID, "transistor_raw"));
    }

    @SubscribeEvent
    public static void registerContainerTypes(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new ScannerContainer(windowId, inv.player.level, data.readBlockPos(), inv, inv.player)).setRegistryName(YouMatter.MODID + ":scanner"));
        event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new EncoderContainer(windowId, inv.player.level, data.readBlockPos(), inv, inv.player)).setRegistryName(YouMatter.MODID + ":encoder"));
        event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new CreatorContainer(windowId, inv.player.level, data.readBlockPos(), inv, inv.player)).setRegistryName(YouMatter.MODID + ":creator"));
        event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new ReplicatorContainer(windowId, inv.player.level, data.readBlockPos(), inv, inv.player)).setRegistryName(YouMatter.MODID + ":replicator"));

    }

}
