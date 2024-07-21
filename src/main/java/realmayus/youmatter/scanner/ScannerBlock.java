package realmayus.youmatter.scanner;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import realmayus.youmatter.ModContent;

import javax.annotation.Nullable;

public class ScannerBlock extends BaseEntityBlock {

    public ScannerBlock() {
        super(BlockBehaviour.Properties.of().strength(5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return this.codec();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScannerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModContent.SCANNER_BLOCK_ENTITY.get(), ScannerBlockEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = getMenuProvider(state, level, pos);
        if (menuProvider != null) {
            player.openMenu(menuProvider, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ScannerBlockEntity scanner ? scanner : null;
    }
}