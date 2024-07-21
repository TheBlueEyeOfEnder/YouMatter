package realmayus.youmatter.encoder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import realmayus.youmatter.ModContent;
import realmayus.youmatter.YMConfig;
import realmayus.youmatter.items.ThumbdriveItem;
import realmayus.youmatter.util.MyEnergyStorage;

import javax.annotation.Nullable;
import java.util.*;

public class EncoderBlockEntity extends BlockEntity implements MenuProvider {

    private List<ItemStack> queue = new ArrayList<>();

    public EncoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.ENCODER_BLOCK_ENTITY.get(), pos, state);
    }

    public Lazy<ItemStackHandler> inventory = Lazy.of(() -> new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            EncoderBlockEntity.this.setChanged();
        }
    });


    // Calling this method signals incoming data from a neighboring scanner
    public void ignite(ItemStack itemStack) {
        if(itemStack != ItemStack.EMPTY && itemStack != null) {
            queue.add(itemStack);
            setChanged();
        }
    }


    private int progress = 0;


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        setChanged();
    }

    public int getEnergy() {
        return myEnergyStorage.get().getEnergyStored();
    }

    public void setEnergy(int energy) {
        myEnergyStorage.get().setEnergy(energy);
    }

    private Lazy<MyEnergyStorage> myEnergyStorage = Lazy.of(() -> new MyEnergyStorage(this, 1000000, Integer.MAX_VALUE));

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        setProgress(compound.getInt("progress"));
        setEnergy(compound.getInt("energy"));
        if(compound.contains("inventory")) {
            inventory.get().deserializeNBT(provider, (CompoundTag) compound.get("inventory"));
        }
        if(compound.contains("queue")) {
            if (compound.get("queue") instanceof ListTag) {
                List<ItemStack> queueBuilder = new ArrayList<>();
                for(Tag base: compound.getList("queue", Tag.TAG_COMPOUND)) {
                    if (base instanceof CompoundTag nbtTagCompound) {
                        if(!ItemStack.parseOptional(provider, nbtTagCompound).isEmpty()) {
                            queueBuilder.add(ItemStack.parseOptional(provider, nbtTagCompound));
                        }
                    }
                }
                queue = queueBuilder;
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.putInt("progress", getProgress());
        compound.putInt("energy", getEnergy());
        if (inventory != null) {
            compound.put("inventory", inventory.get().serializeNBT(provider));
        }
        ListTag tempCompoundList = new ListTag();
        for (ItemStack is : queue) {
            if (!is.isEmpty()) {
                tempCompoundList.add(is.save(provider));
            }
        }
        compound.put("queue", tempCompoundList);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        level.invalidateCapabilities(worldPosition);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EncoderBlockEntity be) {
        be.tick(level, pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!queue.isEmpty()) {
            ItemStack processIS = queue.getLast();
            if (processIS != null) {
                if (inventory != null) {
                    if (inventory.get().getStackInSlot(1).getItem() instanceof ThumbdriveItem) {
                        if (myEnergyStorage.get().getEnergyStored() <= 0) {
                            return;
                        }
                        for (Direction direction : Direction.values()) {
                            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(direction), null);
                            if (progress < 100) {
                                if (getEnergy() >= YMConfig.CONFIG.energyEncoder.get()) {
                                    ItemContainerContents itemsStored = inventory.get().getStackInSlot(1).get(ModContent.ITEMS_STORED_DATA.get());
                                    if (itemsStored != null) {
                                        if (itemsStored.getSlots() < 8) {
                                            if (energyStorage != null) {
                                                progress = progress + 1;
                                                myEnergyStorage.get().extractEnergy(YMConfig.CONFIG.energyEncoder.get(), false);
                                            }
                                        }
                                    }
                                } else {
                                    if (energyStorage != null) {
                                        progress = progress + 1; //doesn't have data stored yet
                                        myEnergyStorage.get().extractEnergy(YMConfig.CONFIG.energyEncoder.get(), false);
                                    }
                                }
                            } else {
                                ItemContainerContents itemsStored = inventory.get().getStackInSlot(1).getOrDefault(ModContent.ITEMS_STORED_DATA.get(), ItemContainerContents.EMPTY);
                                if (itemsStored != null) {
                                    if (itemsStored.getSlots() < 8) {
                                        inventory.get().getStackInSlot(1).set(ModContent.ITEMS_STORED_DATA.get(), ItemContainerContents.fromItems(Collections.singletonList(processIS.copy())));
                                    }
                                } else {
                                    inventory.get().getStackInSlot(1).set(ModContent.ITEMS_STORED_DATA.get(), ItemContainerContents.fromItems(Collections.singletonList(processIS.copy())));
                                }
                            }
                        }
                    } else {
                        inventory.get().getStackInSlot(1).set(ModContent.ITEMS_STORED_DATA.get(), ItemContainerContents.EMPTY);
                    }
                    queue.remove(processIS);
                    progress = 0;
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(ModContent.ENCODER_BLOCK.get().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player player) {
        return new EncoderMenu(windowID, level, worldPosition, playerInventory, player);
    }

    public ItemStackHandler getItemHandler() {
        return inventory.get();
    }

    public IEnergyStorage getEnergyHandler() {
        return myEnergyStorage.get();
    }
}