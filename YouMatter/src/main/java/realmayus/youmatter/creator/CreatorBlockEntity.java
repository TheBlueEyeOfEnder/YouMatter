package realmayus.youmatter.creator;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import realmayus.youmatter.ModContent;
import realmayus.youmatter.YMConfig;
import realmayus.youmatter.replicator.ReplicatorBlockEntity;
import realmayus.youmatter.util.GeneralUtils;
import realmayus.youmatter.util.MyEnergyStorage;
import realmayus.youmatter.util.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CreatorBlockEntity extends BlockEntity implements MenuProvider {

    public CreatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CREATOR_BLOCK_ENTITY.get(), pos, state);
    }

    private static final int MAX_UMATTER = 11000;
    private static final int MAX_STABILIZER = 11000;

    private boolean isActivated = true;

    boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public Lazy<ItemStackHandler> inventory = Lazy.of(() -> new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            CreatorBlockEntity.this.setChanged();
        }
    });

    private FluidTank uTank = new FluidTank(MAX_UMATTER) {
        @Override
        protected void onContentsChanged() {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            setChanged();
        }
    };

    private FluidTank sTank = new FluidTank(MAX_STABILIZER) {
        @Override
        protected void onContentsChanged() {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            setChanged();
        }
    };

    FluidTank getUTank() {
        return uTank;
    }

    FluidTank getSTank() {
        return sTank;
    }

    private Lazy<IFluidHandler> fluidHandler = Lazy.of(() -> new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank == 0) {
                return uTank.getFluid();
            } else if (tank == 1) {
                return sTank.getFluid();
            }
            return null;
        }

        @Override
        public int getTankCapacity(int tank) {
            if (tank == 0) {
                return MAX_UMATTER;
            } else if (tank == 1) {
                return MAX_STABILIZER;
            }
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            if (tank == 0) {
                return false;
            } else if (tank == 1 && stack.is(Tags.STABILIZER)) {
                return true;
            }
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.is(Tags.STABILIZER)) {
                if (MAX_STABILIZER - getSTank().getFluidAmount() < resource.getAmount()) {
                    return sTank.fill(new FluidStack(resource.getFluid(), MAX_STABILIZER), action);
                } else {
                    return sTank.fill(resource, action);
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid().equals(ModContent.UMATTER.get())) {
                if (uTank.getFluidAmount() < resource.getAmount()) {
                    uTank.drain(uTank.getFluid(), action);
                    return uTank.getFluid();
                } else {
                    uTank.drain(resource, action);
                    return resource;
                }
            }
            return null;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (uTank.getFluid().getFluid() != null) {
                return uTank.drain(uTank.getFluid(), action);
            } else {
                return null;
            }
        }
    });

    public int getEnergy() {
        return myEnergyStorage.get().getEnergyStored();
    }

    public void setEnergy(int energy) {
        myEnergyStorage.get().setEnergy(energy);
    }

    private Lazy<MyEnergyStorage> myEnergyStorage = Lazy.of(() -> new MyEnergyStorage(this, 1000000, Integer.MAX_VALUE));

    @Override
    public void setRemoved() {
        super.setRemoved();
        level.invalidateCapabilities(worldPosition);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("uTank")) {
            CompoundTag tagUTank = compound.getCompound("uTank");
            uTank.readFromNBT(tagUTank);
        }
        if (compound.contains("sTank")) {
            CompoundTag tagSTank = compound.getCompound("sTank");
            sTank.readFromNBT(tagSTank);
        }
        if (compound.contains("energy")) {
            setEnergy(compound.getInt("energy"));
        }
        if (compound.contains("isActivated")) {
            isActivated = compound.getBoolean("isActivated");
        }
        if (compound.contains("inventory")) {
            inventory.get().deserializeNBT((CompoundTag) compound.get("inventory"));
        }
    }


    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        CompoundTag tagSTank = new CompoundTag();
        CompoundTag tagUTank = new CompoundTag();
        sTank.writeToNBT(tagSTank);
        uTank.writeToNBT(tagUTank);
        compound.put("uTank", tagUTank);
        compound.put("sTank", tagSTank);
        compound.putInt("energy", getEnergy());
        compound.putBoolean("isActivated", isActivated);
        if (compound.contains("inventory")) {
            inventory.get().deserializeNBT((CompoundTag) compound.get("inventory"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private int currentPartTick = 0;

    public static void tick(Level level, BlockPos pos, BlockState state, CreatorBlockEntity be) {
        be.tick(level, pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (currentPartTick == 40) { // 2 sec
            for (Direction direction : Direction.values()) {
                if (myEnergyStorage.get().getEnergyStored() <= 0) {
                    return;
                }
                IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(direction), null);
                if (isActivated()) {
                    if (getEnergy() >= 0.3f * 1000000 && sTank.getFluidAmount() >= 125) { // if energy more than 30 % of max energy
                        if (uTank.getFluidAmount() + YMConfig.CONFIG.productionPerTick.get() <= MAX_UMATTER) {
                            if (energyStorage != null) {
                                sTank.drain(125, IFluidHandler.FluidAction.EXECUTE);
                                uTank.fill(new FluidStack(ModContent.UMATTER.get(), YMConfig.CONFIG.productionPerTick.get()), IFluidHandler.FluidAction.EXECUTE);
                                myEnergyStorage.get().extractEnergy(Math.round(getEnergy() / 3f), false);
                            }
                        }
                    }
                }
            }
            //Auto-outputting U-Matter
            Object[] neighborTE = getNeighborTileEntity(pos);
            if (neighborTE != null) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, (BlockPos) neighborTE[0], (Direction) neighborTE[1]);
                if (h != null) {
                    if (uTank.getFluidAmount() >= 500) { // set a maximum output of 500 mB (every two seconds)
                        uTank.drain(h.fill(new FluidStack(ModContent.UMATTER.get(), 500), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    } else {
                        uTank.drain(h.fill(new FluidStack(ModContent.UMATTER.get(), uTank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
            currentPartTick = 0;
            } else if ((currentPartTick % 5) == 0) { // every five ticks
                if (inventory != null)
                    if (!(inventory.get().getStackInSlot(3).isEmpty()) && GeneralUtils.canAddItemToSlot(inventory.get().getStackInSlot(4), inventory.get().getStackInSlot(3), false)) {
                        ItemStack item = inventory.get().getStackInSlot(3);
                        if (item.getItem() instanceof BucketItem) {
                            if (getUTank().getFluidAmount() >= 1000) {
                                getUTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                                inventory.get().setStackInSlot(3, ItemStack.EMPTY);
                                inventory.get().insertItem(4, new ItemStack(ModContent.UMATTER_BUCKET.get(), 1), false);
                            }
                        } else {
                            IFluidHandlerItem h = item.getCapability(Capabilities.FluidHandler.ITEM);
                            if (h != null) {
                                if (h.getFluidInTank(0).getFluid().isSame(ModContent.UMATTER.get()) || h.getFluidInTank(0).isEmpty()) {
                                    if (h.getTankCapacity(0) - h.getFluidInTank(0).getAmount() < getUTank().getFluidAmount()) { //fluid in S-Tank is more than what fits in the item's tank
                                        getUTank().drain(h.fill(new FluidStack(ModContent.UMATTER.get(), h.getTankCapacity(0) - h.getFluidInTank(0).getAmount()), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                    } else { //S-Tank's fluid fits perfectly in item's tank
                                        getUTank().drain(h.fill(getUTank().getFluid(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                            inventory.get().setStackInSlot(3, ItemStack.EMPTY);
                            inventory.get().insertItem(4, item, false);
                        }
                    }
                if (!inventory.get().getStackInSlot(1).isEmpty()) {
                    ItemStack item = inventory.get().getStackInSlot(1);
                    if (item.getItem() instanceof BucketItem && GeneralUtils.canAddItemToSlot(inventory.get().getStackInSlot(2), new ItemStack(Items.BUCKET, 1), false)) {
                        IFluidHandlerItem h = item.getCapability(Capabilities.FluidHandler.ITEM);
                        if (h != null) {
                            if (!h.getFluidInTank(0).isEmpty() && (h.getFluidInTank(0).is(Tags.STABILIZER))) {
                                if (MAX_STABILIZER - getSTank().getFluidAmount() >= 1000) {
                                    getSTank().fill(new FluidStack(h.getFluidInTank(0).getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                                    inventory.get().setStackInSlot(1, ItemStack.EMPTY);
                                    inventory.get().insertItem(2, new ItemStack(Items.BUCKET, 1), false);
                                }
                            }
                        }
                    } else if (GeneralUtils.canAddItemToSlot(inventory.get().getStackInSlot(2), inventory.get().getStackInSlot(1), false)) {
                        IFluidHandlerItem h = item.getCapability(Capabilities.FluidHandler.ITEM);
                        if (h != null) {
                            if (h.getFluidInTank(0).is(Tags.STABILIZER)) {
                                if (h.getFluidInTank(0).getAmount() > MAX_STABILIZER - getSTank().getFluidAmount()) { //given fluid is more than what fits in the S-Tank
                                    getSTank().fill(h.drain(MAX_STABILIZER - getSTank().getFluidAmount(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                } else { //given fluid fits perfectly in S-Tank
                                    getSTank().fill(h.drain(h.getFluidInTank(0).getAmount(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                }
                            }
                        }
                        inventory.get().setStackInSlot(1, ItemStack.EMPTY);
                        inventory.get().insertItem(2, item, false);
                    }
                }
                currentPartTick++;
            } else {
                currentPartTick++;
            }
        }

    private Object[] getNeighborTileEntity(BlockPos creatorPos) {
        HashMap<BlockPos, Direction> foundPos = new HashMap<>();
        for (Direction facing : Direction.values()) {
            BlockPos offsetPos = creatorPos.relative(facing);
            BlockEntity offsetBe = level.getBlockEntity(offsetPos);

            if (offsetBe != null) {
                IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, offsetPos, facing);
                if (fluidHandler != null) {
                    foundPos.put(offsetPos, facing);
                }
            }
        }

        // Prioritize Replicator
        for (Map.Entry<BlockPos, Direction> entry : foundPos.entrySet()) {
            if (level.getBlockEntity(entry.getKey()) instanceof ReplicatorBlockEntity replicator) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, replicator.getBlockPos(), entry.getValue());
                if (h != null && h.fill(new FluidStack(ModContent.UMATTER.get(), 500), IFluidHandler.FluidAction.SIMULATE) > 0) {
                    //Replicator can take fluid
                    return new Object[]{entry.getKey(), entry.getValue()}; // position, facing
                }
            }
        }

        // Replicator not found / can't take fluid, now trying other blocks
        for (Map.Entry<BlockPos, Direction> entry : foundPos.entrySet()) {
            BlockEntity be = level.getBlockEntity(entry.getKey());
            if (be != null) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), entry.getValue());
                if (h != null && h.fill(new FluidStack(ModContent.UMATTER.get(), 500), IFluidHandler.FluidAction.SIMULATE) > 0) {
                    //Tile can take fluid
                    return new Object[]{entry.getKey(), entry.getValue()}; // position, facing
                }
            }
        }

        // found nothing
        return null;
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable(ModContent.CREATOR_BLOCK.get().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        return new CreatorMenu(windowID, level, worldPosition, playerInventory, playerEntity);

    }

    public ItemStackHandler getItemHandler() {
        return inventory.get();
    }

    public IEnergyStorage getEnergyHandler() {
        return myEnergyStorage.get();
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler.get();
    }
}
