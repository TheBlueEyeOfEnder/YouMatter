package realmayus.youmatter.encoder;

import com.ibm.icu.impl.ValidIdentifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.ItemStackHandler;
import realmayus.youmatter.util.MyEnergyStorage;

import java.util.ArrayList;
import java.util.List;

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
                        if(!ItemStack.of(nbtTagCompound).isEmpty()) {
                            queueBuilder.add(ItemStack.of(nbtTagCompound));
                        }
                    }
                }
                queue = queueBuilder;
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("progress", getProgress());
        compound.putInt("energy", getEnergy());
        if (inventory != null) {
            compound.put("inventory", inventory.get().serializeNBT());
        }
        ListTag tempCompoundList = new ListTag();
        for (ItemStack is : queue) {
            if (!is.isEmpty()) {
                tempCompoundList.add(is.save(new CompoundTag()));
            }
        }
        compound.put("queue", tempCompoundList);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
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
            ItemStack processIS = queue.get(queue.size() - 1);
            if (processIS != ItemStack.EMPTY) {
                if (inventory != null) {
                    if (inventory.get().getStackInSlot(1).getItem() instanceof ThumbdriveItem) {
                        if (myEnergyStorage.get().getEnergyStored() <= 0) {
                            return;
                        }
                        for (Direction direction : Direction.values()) {
                            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(direction), null);
                            if (progress < 100) {
                                if (getEnergy() >= YMConfig.CONFIG.energyEncoder.get()) {
                                    CompoundTag nbt = inventory.get().getStackInSlot(1).getTag();
                                    if (nbt != null) {
                                        if (nbt.contains("stored_items")) {
                                            ListTag list = nbt.getList("stored_items", Tag.TAG_STRING);
                                            if (list.size() < 8) {
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
                                }
                            } else {
                                DataComponents nbt = inventory.get().getStackInSlot(1).getTag();
                                if (nbt != null) {
                                    if (nbt.contains("stored_items")) {
                                        ListTag list = nbt.getList("stored_items", Tag.TAG_STRING);
                                        if (list.size() < 8) {
                                            list.add(StringTag.valueOf(RegistryUtil.getRegistryName(processIS.getItem()) + ""));
                                            nbt.put("stored_items", list);
                                        }
                                    } else {
                                        ListTag list = new ListTag();
                                        list.add(StringTag.valueOf(RegistryUtil.getRegistryName(processIS.getItem()) + ""));
                                        nbt.put("stored_items", list);
                                    }
                                } else {
                                    nbt = new CompoundTag();
                                    ListTag list = new ListTag();
                                    list.add(StringTag.valueOf(RegistryUtil.getRegistryName(processIS.getItem()) + ""));
                                    nbt.put("stored_items", list);
                                    inventory.get().getStackInSlot(1).setTag(nbt);
                                }
                                queue.remove(processIS);
                                progress = 0;
                            }
                        }
                    }
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
