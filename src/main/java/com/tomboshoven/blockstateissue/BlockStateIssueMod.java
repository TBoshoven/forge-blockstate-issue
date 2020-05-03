package com.tomboshoven.blockstateissue;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod("blockstateissue")
public class BlockStateIssueMod {
    private static final Logger LOGGER = LogManager.getLogger();

    private static Block PLAIN_ISSUE_BLOCK = new PlainIssueBlock(Block.Properties.from(Blocks.DIRT)).setRegistryName("blockstateissue", "issue");
    private static Block REMOVAL_ISSUE_BLOCK = new ManualRemovalIssueBlock(Block.Properties.from(Blocks.DIRT)).setRegistryName("blockstateissue", "removal_issue");
    private static TileEntityType<LoggingTileEntity> LOGGING_TE = TileEntityType.Builder.create(LoggingTileEntity::new, PLAIN_ISSUE_BLOCK, REMOVAL_ISSUE_BLOCK).build(null);

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().registerAll(PLAIN_ISSUE_BLOCK, REMOVAL_ISSUE_BLOCK);
        }

        @SubscribeEvent
        public static void onTERegistry(final RegistryEvent.Register<TileEntityType<?>> teRegistryEvent) {
            LOGGING_TE.setRegistryName("blockstateissue", "issue");
            teRegistryEvent.getRegistry().register(LOGGING_TE);
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            itemRegistryEvent.getRegistry().register(new BlockItem(PLAIN_ISSUE_BLOCK, new Item.Properties()).setRegistryName("blockstateissue", "issue"));
            itemRegistryEvent.getRegistry().register(new BlockItem(REMOVAL_ISSUE_BLOCK, new Item.Properties()).setRegistryName("blockstateissue", "removal_issue"));
        }
    }

    public static class LoggingTileEntity extends TileEntity {
        public LoggingTileEntity() {
            super(LOGGING_TE);
            LOGGER.info("Created TE {}", hashCode());
        }

        @Override
        public void remove() {
            super.remove();
            LOGGER.info("Removed TE {}", hashCode());
        }
    }

    public static class PlainIssueBlock extends Block {
        final static BooleanProperty HAS_TE = BooleanProperty.create("has_te");

        public PlainIssueBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
            builder.add(HAS_TE);
        }

        @Override
        public boolean hasTileEntity(BlockState state) {
            return state.get(HAS_TE);
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new LoggingTileEntity();
        }

        @Override
        public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            worldIn.setBlockState(pos, state.with(HAS_TE, !state.get(HAS_TE)));
            return true;
        }
    }

    public static class ManualRemovalIssueBlock extends PlainIssueBlock {
        final static BooleanProperty HAS_TE = BooleanProperty.create("has_te");

        public ManualRemovalIssueBlock(Properties properties) {
            super(properties);
        }

        @Override
        public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            if (state.get(HAS_TE) && !newState.get(HAS_TE)) {
                worldIn.removeTileEntity(pos);
            }
        }
    }
}
