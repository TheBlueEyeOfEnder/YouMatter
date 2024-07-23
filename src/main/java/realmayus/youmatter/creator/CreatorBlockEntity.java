package realmayus.youmatter.creator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import realmayus.youmatter.ModContent;
import realmayus.youmatter.YMConfig;
import realmayus.youmatter.replicator.ReplicatorBlockEntity;
import realmayus.youmatter.util.GeneralUtils;
import realmayus.youmatter.util.MyEnergyStorage;
import realmayus.youmatter.util.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreatorBlockEntity extends BlockEntity implements MenuProvider {

    public CreatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CREATOR_BLOCK_ENTITY.get(), pos, state);
    }

    private static final int MAX_UMATTER = 64000;
    private static final int MAX_STABILIZER = 64000;

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

    public ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            CreatorBlockEntity.this.setChanged();
        }
    };

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

    private IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> uTank.getFluid();
                case 1 -> sTank.getFluid();
                default -> null;
            };
        }


        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> MAX_UMATTER;
                case 1 -> MAX_STABILIZER;
                default -> 0;
            };
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 1 && stack.getFluid().is(Tags.Fluids.STABILIZER);
        }

        @Override
        public int fill(FluidStack resource, @NotNull FluidAction action) {
            if (resource.getFluid().is(Tags.Fluids.STABILIZER)) {
                int fillAmount = Math.min(resource.getAmount(), MAX_STABILIZER - getSTank().getFluidAmount());
                return sTank.fill(new FluidStack(resource.getFluid(), fillAmount), action);
            }
            return 0;
        }


        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid().equals(ModContent.UMATTER.get())) {
                FluidStack toDrain = uTank.getFluidAmount() < resource.getAmount() ? uTank.getFluid() : resource;
                uTank.drain(toDrain, action);
                return toDrain;
            }
            return null;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return uTank.getFluid().getFluid() != null ? uTank.drain(uTank.getFluid(), action) : null;
        }
    };

    public int getEnergy() {
        return myEnergyStorage.getEnergyStored();
    }

    public void setEnergy(int energy) {
        myEnergyStorage.setEnergy(energy);
    }

    private final MyEnergyStorage myEnergyStorage = new MyEnergyStorage(this, 1000000, Integer.MAX_VALUE);

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
            inventory.deserializeNBT((CompoundTag) compound.get("inventory"));
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
            inventory.deserializeNBT((CompoundTag) compound.get("inventory"));
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
        if (currentPartTick != 40 && (currentPartTick % 5) != 0) {
            currentPartTick++;
            return;
        }

        if (currentPartTick == 40) { // 2 sec
            if (myEnergyStorage.getEnergyStored() <= 0) {
                currentPartTick = 0;
                return;
            }

            for (Direction direction: Direction.values()) {
                if (!isActivated()) continue;

                IEnergyStorage e = level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(direction), null);
                if (e == null) continue;

                if (getEnergy() < 0.3f * 1000000 || sTank.getFluidAmount() < 125) continue; // if energy less than 30 % of max energy

                if (uTank.getFluidAmount() + YMConfig.CONFIG.productionPerTick.get() > MAX_UMATTER) continue;

                sTank.drain(125, IFluidHandler.FluidAction.EXECUTE);
                uTank.fill(new FluidStack(ModContent.UMATTER.get(), YMConfig.CONFIG.productionPerTick.get()), IFluidHandler.FluidAction.EXECUTE);
                myEnergyStorage.extractEnergy(Math.round(getEnergy() / 3f), false);
            }

            //Auto-outputting U-Matter
            Object[] neighborTE = getNeighborTileEntity(pos);
            if (neighborTE != null) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, ((BlockPos) neighborTE[0]), (Direction) neighborTE[1]);
                if (h != null) {
                    int amountToDrain = Math.min(uTank.getFluidAmount(), 500); // set a maximum output of 500 mB (every two seconds)
                    uTank.drain(h.fill(new FluidStack(ModContent.UMATTER.get(), amountToDrain), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                }
            }
            currentPartTick = 0;
        } else if ((currentPartTick % 5) == 0) { // every five ticks
            ItemStack item = inventory.getStackInSlot(3);
            if (!(item.isEmpty()) && GeneralUtils.canAddItemToSlot(inventory.getStackInSlot(4), item, false)) {
                if (item.getItem() instanceof BucketItem && getUTank().getFluidAmount() >= 1000) {
                    getUTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    inventory.setStackInSlot(3, ItemStack.EMPTY);
                    inventory.insertItem(4, new ItemStack(ModContent.UMATTER_BUCKET.get(), 1), false);
                } else {
                    IFluidHandlerItem h = item.getCapability(Capabilities.FluidHandler.ITEM);
                    if (h != null && (h.getFluidInTank(0).getFluid().isSame(ModContent.UMATTER.get()) || h.getFluidInTank(0).isEmpty())) {
                        int amountToFill = Math.min(h.getTankCapacity(0) - h.getFluidInTank(0).getAmount(), getUTank().getFluidAmount());
                        getUTank().drain(h.fill(new FluidStack(ModContent.UMATTER.get(), amountToFill), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    }
                    inventory.setStackInSlot(3, ItemStack.EMPTY);
                    inventory.insertItem(4, item, false);
                }
            }
            item = inventory.getStackInSlot(1);
            if (!item.isEmpty()) {
                IFluidHandlerItem h = item.getCapability(Capabilities.FluidHandler.ITEM);
                if (h != null) {
                    if (item.getItem() instanceof BucketItem && GeneralUtils.canAddItemToSlot(inventory.getStackInSlot(2), new ItemStack(Items.BUCKET, 1), false)) {
                        if (h.getFluidInTank(0).getFluid().isSame(getSTank().getFluidInTank(0).getFluid()) || getSTank().isEmpty()) {
                            if (!h.getFluidInTank(0).isEmpty() && (h.getFluidInTank(0).getFluid().is(Tags.Fluids.STABILIZER))) {
                                if (MAX_STABILIZER - getSTank().getFluidAmount() >= 1000) {
                                    getSTank().fill(new FluidStack(h.getFluidInTank(0).getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                                    inventory.setStackInSlot(1, ItemStack.EMPTY);
                                    inventory.insertItem(2, new ItemStack(Items.BUCKET, 1), false);
                                }
                            }
                        }
                    } else if (GeneralUtils.canAddItemToSlot(inventory.getStackInSlot(2), item, false)) {
                        if (h.getFluidInTank(0).getFluid().isSame(getSTank().getFluidInTank(0).getFluid()) || getSTank().isEmpty()) {
                            if (h.getFluidInTank(0).getFluid().is(Tags.Fluids.STABILIZER)) {
                                int amountToDrain = Math.min(h.getFluidInTank(0).getAmount(), MAX_STABILIZER - getSTank().getFluidAmount());
                                getSTank().fill(h.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                }
                inventory.setStackInSlot(1, ItemStack.EMPTY);
                inventory.insertItem(2, item, false);
            }
            currentPartTick++;
        }
    }

    private Object[] getNeighborTileEntity(BlockPos creatorPos) {
        Object[] result = null;

        for (Direction facing: Direction.values()) {
            BlockPos offsetPos = creatorPos.relative(facing);
            BlockEntity offsetBe = level.getBlockEntity(offsetPos);

            if (offsetBe != null) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, offsetPos, facing);
                if (h != null && h.fill(new FluidStack(ModContent.UMATTER.get(), 500), IFluidHandler.FluidAction.SIMULATE) > 0) {
                    if (offsetBe instanceof ReplicatorBlockEntity) {
                        //Replicator can take fluid
                        return new Object[] {
                                offsetPos,
                                facing
                        }; // position, facing
                    } else if (result == null) {
                        //Tile can take fluid
                        result = new Object[] {
                                offsetPos,
                                facing
                        }; // position, facing
                    }
                }
            }
        }

        return result; // found nothing or return the first non-replicator that can take fluid
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
        return inventory;
    }

    public IEnergyStorage getEnergyHandler() {
        return myEnergyStorage;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
