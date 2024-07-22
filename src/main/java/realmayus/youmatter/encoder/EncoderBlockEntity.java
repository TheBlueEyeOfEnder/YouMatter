package realmayus.youmatter.encoder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
import realmayus.youmatter.util.RegistryUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EncoderBlockEntity extends BlockEntity implements MenuProvider {

    private List<ItemStack> queue = new ArrayList<>();

    public EncoderBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.ENCODER_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            EncoderBlockEntity.this.setChanged();
        }
    };


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
        return myEnergyStorage.getEnergyStored();
    }

    public void setEnergy(int energy) {
        myEnergyStorage.setEnergy(energy);
    }

    private MyEnergyStorage myEnergyStorage = new MyEnergyStorage(this, 1000000, Integer.MAX_VALUE);

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        setProgress(compound.getInt("progress"));
        setEnergy(compound.getInt("energy"));

        if(compound.contains("inventory")) {
            inventory.deserializeNBT((CompoundTag) compound.get("inventory"));
        }

        if(compound.contains("queue") && compound.get("queue") instanceof ListTag) {
            queue = compound.getList("queue", Tag.TAG_COMPOUND).stream()
                    .filter(base -> base instanceof CompoundTag)
                    .map(base -> ItemStack.of((CompoundTag) base))
                    .filter(stack -> !stack.isEmpty())
                    .collect(Collectors.toList());
        }
    }


    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("progress", getProgress());
        compound.putInt("energy", getEnergy());
        if (inventory != null) {
            compound.put("inventory", inventory.serializeNBT());
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
        if (queue.isEmpty() || inventory == null || myEnergyStorage.getEnergyStored() <= 0) {
            return;
        }

        ItemStack processIS = queue.get(queue.size() - 1);
        if (processIS == ItemStack.EMPTY) {
            return;
        }

        if (!(inventory.getStackInSlot(1).getItem() instanceof ThumbdriveItem)) {
            return;
        }

        for (Direction direction : Direction.values()) {
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(direction), null);
            if (energyStorage == null) {
                continue;
            }

            CompoundTag nbt = inventory.getStackInSlot(1).getTag();
            if (nbt == null) {
                nbt = new CompoundTag();
            }

            ListTag list = nbt.getList("stored_items", Tag.TAG_STRING);
            if (list == null) {
                list = new ListTag();
            }

            // Check if the item is already encoded on the thumb drive
            String itemRegistryName = RegistryUtil.getRegistryName(processIS.getItem()) + "";
            boolean isItemAlreadyEncoded = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.getString(i).equals(itemRegistryName)) {
                    isItemAlreadyEncoded = true;
                    break;
                }
            }

            if (isItemAlreadyEncoded) {
                // If the item is already encoded, remove it from the queue and return
                queue.remove(processIS);
                return;
            }

            if (progress < 100 && getEnergy() >= YMConfig.CONFIG.energyEncoder.get() && list.size() < 8) {
                progress++;
                myEnergyStorage.extractEnergy(YMConfig.CONFIG.energyEncoder.get(), false);
            } else if (progress >= 100) {
                list.add(StringTag.valueOf(itemRegistryName));
                nbt.put("stored_items", list);
                inventory.getStackInSlot(1).setTag(nbt);
                queue.remove(processIS);
                progress = 0;
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
        return inventory;
    }

    public IEnergyStorage getEnergyHandler() {
        return myEnergyStorage;
    }
}

