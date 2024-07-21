package realmayus.youmatter;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import realmayus.youmatter.encoder.EncoderBlock;
import realmayus.youmatter.encoder.EncoderBlockEntity;
import realmayus.youmatter.encoder.EncoderMenu;
import realmayus.youmatter.items.BlackHoleItem;
import realmayus.youmatter.items.ComputeModuleItem;
import realmayus.youmatter.items.MachineCasingItem;
import realmayus.youmatter.items.ThumbdriveItem;
import realmayus.youmatter.items.TransistorItem;
import realmayus.youmatter.items.TransistorRawItem;
import realmayus.youmatter.scanner.ScannerBlock;
import realmayus.youmatter.scanner.ScannerBlockEntity;
import realmayus.youmatter.scanner.ScannerMenu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.core.component.DataComponentType.builder;

public class ModContent {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, YouMatter.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, YouMatter.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, YouMatter.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, YouMatter.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, YouMatter.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, YouMatter.MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(YouMatter.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> ITEMS_STORED_DATA = DATA_COMPONENTS.register(
            "items_stored", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC).build());

    public static final DeferredHolder<Block, ScannerBlock> SCANNER_BLOCK = BLOCKS.register("scanner", ScannerBlock::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ScannerMenu>> SCANNER_MENU = MENU_TYPES.register("scanner", () -> IMenuTypeExtension.create((windowId, inv, data) -> new ScannerMenu(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScannerBlockEntity>> SCANNER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("scanner", () -> BlockEntityType.Builder.of(ScannerBlockEntity::new, SCANNER_BLOCK.get()).build(null));
    public static final DeferredHolder<Item, BlockItem> SCANNER_BLOCK_ITEM = ITEMS.register("scanner", () -> new BlockItem(SCANNER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<Block, EncoderBlock> ENCODER_BLOCK = BLOCKS.register("encoder", EncoderBlock::new);
    public static final DeferredHolder<MenuType<?>, MenuType<EncoderMenu>> ENCODER_MENU = MENU_TYPES.register("encoder", () -> IMenuTypeExtension.create((windowId, inv, data) -> new EncoderMenu(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EncoderBlockEntity>> ENCODER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("encoder", () -> BlockEntityType.Builder.of(EncoderBlockEntity::new, ENCODER_BLOCK.get()).build(null));
    public static final DeferredHolder<Item, BlockItem> ENCODER_BLOCK_ITEM = ITEMS.register("encoder", () -> new BlockItem(ENCODER_BLOCK.get(), new Item.Properties()));

   /*  public static final DeferredHolder<Block, CreatorBlock> CREATOR_BLOCK = BLOCKS.register("creator", CreatorBlock::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CreatorMenu>> CREATOR_MENU = MENU_TYPES.register("creator", () -> IMenuTypeExtension.create((windowId, inv, data) -> new CreatorMenu(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreatorBlockEntity>> CREATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("creator", () -> BlockEntityType.Builder.of(CreatorBlockEntity::new, CREATOR_BLOCK.get()).build(null));
    public static final DeferredHolder<Item, BlockItem> CREATOR_BLOCK_ITEM = ITEMS.register("creator", () -> new BlockItem(CREATOR_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<Block, ReplicatorBlock> REPLICATOR_BLOCK = BLOCKS.register("replicator", ReplicatorBlock::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ReplicatorMenu>> REPLICATOR_MENU = MENU_TYPES.register("replicator", () -> IMenuTypeExtension.create((windowId, inv, data) -> new ReplicatorMenu(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReplicatorBlockEntity>> REPLICATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("replicator", () -> BlockEntityType.Builder.of(ReplicatorBlockEntity::new, REPLICATOR_BLOCK.get()).build(null));
    public static final DeferredHolder<Item, BlockItem> REPLICATOR_BLOCK_ITEM = ITEMS.register("replicator", () -> new BlockItem(REPLICATOR_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<FluidType, FluidType> STABILIZER_TYPE = FLUID_TYPES.register("stabilizer", StabilizerFluidType::new);
    public static final DeferredHolder<Fluid, FlowingFluid> STABILIZER = FLUIDS.register("stabilizer", () -> new BaseFlowingFluid.Source(ModContent.STABILIZER_PROPERIES));
    public static final DeferredHolder<Fluid, FlowingFluid> STABILIZER_FLOWING = FLUIDS.register("stabilizer_flowing", () -> new BaseFlowingFluid.Flowing(ModContent.STABILIZER_PROPERIES));
    public static final DeferredHolder<Block, StabilizerFluidBlock> STABILIZER_FLUID_BLOCK = BLOCKS.register("stabilizer_fluid_block", () -> new StabilizerFluidBlock(STABILIZER, BlockBehaviour.Properties.of().noCollission().strength(1.0F).noLootTable()));
    public static final DeferredHolder<Item, BucketItem> STABILIZER_BUCKET = ITEMS.register("stabilizer_bucket", () -> new BucketItem(ModContent.STABILIZER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final BaseFlowingFluid.Properties STABILIZER_PROPERIES = new BaseFlowingFluid.Properties(STABILIZER_TYPE, STABILIZER, STABILIZER_FLOWING).bucket(STABILIZER_BUCKET).block(STABILIZER_FLUID_BLOCK);

    public static final DeferredHolder<FluidType, FluidType> UMATTER_TYPE = FLUID_TYPES.register("umatter", UMatterFluidType::new);
    public static final DeferredHolder<Fluid, FlowingFluid> UMATTER = FLUIDS.register("umatter", () -> new BaseFlowingFluid.Source(ModContent.UMATTER_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> UMATTER_FLOWING = FLUIDS.register("umatter_flowing", () -> new BaseFlowingFluid.Flowing(ModContent.UMATTER_PROPERTIES));
    public static final DeferredHolder<Block, UMatterFluidBlock> UMATTER_FLUID_BLOCK = BLOCKS.register("umatter_fluid_block", () -> new UMatterFluidBlock(UMATTER, BlockBehaviour.Properties.of().noCollission().strength(1.0F).noLootTable()));
    public static final DeferredHolder<Item, BucketItem> UMATTER_BUCKET = ITEMS.register("umatter_bucket", () -> new BucketItem(UMATTER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final BaseFlowingFluid.Properties UMATTER_PROPERTIES = new BaseFlowingFluid.Properties(UMATTER_TYPE, UMATTER, UMATTER_FLOWING).bucket(UMATTER_BUCKET).block(UMATTER_FLUID_BLOCK); */

    public static final DeferredHolder<Item, BlackHoleItem> BLACK_HOLE_ITEM = ITEMS.register("black_hole", BlackHoleItem::new);
    public static final DeferredHolder<Item, ThumbdriveItem> THUMBDRIVE_ITEM = ITEMS.register("thumb_drive", ThumbdriveItem::new);
    public static final DeferredHolder<Item, MachineCasingItem> MACHINE_CASING_ITEM = ITEMS.register("machine_casing", MachineCasingItem::new);
    public static final DeferredHolder<Item, ComputeModuleItem> COMPUTE_MODULE_ITEM = ITEMS.register("compute_module", ComputeModuleItem::new);
    public static final DeferredHolder<Item, TransistorItem> TRANSISTOR_ITEM = ITEMS.register("transistor", TransistorItem::new);
    public static final DeferredHolder<Item, TransistorRawItem> TRANSISTOR_RAW_ITEM = ITEMS.register("transistor_raw", TransistorRawItem::new);

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
    }
}
