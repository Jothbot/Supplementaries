package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ClockBlock extends Block implements IWaterLoggable {
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(1D, 0D, 1D, 0D, 1D, 0.0625D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0D, 0D, 0D, 1D, 1D, 0.9375D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0D, 0D, 1D, 0.9375D, 1D, 0D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(1D, 0D, 0D, 0.0625D, 1D, 1D);

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public static final IntegerProperty HOUR = CommonUtil.HOUR;
    public static final BooleanProperty TILE = CommonUtil.TILE; // rendered by tile?
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ClockBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false).with(FACING, Direction.NORTH).with(TILE, false).with(POWER, 0));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        if (!worldIn.isRemote()) {
            int time = ((int) (worldIn.getDayTime()+6000) % 24000);
            int h = time / 1000;
            int m = (int) (((time % 1000f) / 1000f) * 60);
            String a = time < 12000 ? " AM" : " PM";
            player.sendStatusMessage(new StringTextComponent(h + ":" + ((m<10)?"0":"") + m+ a), true);
        }
        return ActionResultType.SUCCESS;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;;
        return this.getDefaultState().with(WATERLOGGED, flag).with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    /*
     * public int getWeakPower(BlockState blockState, IBlockReader blockAccess,
     * BlockPos pos, Direction side) { return blockState.get(POWER); }
     */

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case NORTH :
            default :
                return SHAPE_NORTH;
            case SOUTH :
                return SHAPE_SOUTH;
            case EAST :
                return SHAPE_EAST;
            case WEST :
                return SHAPE_WEST;
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ClockBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof ClockBlockTile) {
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    public boolean canProvidePower(BlockState state) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER,HOUR,FACING,TILE,WATERLOGGED);
    }

    @Override
    public boolean hasComparatorInputOverride(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        return blockState.get(POWER);
    }

    public static int getHour(BlockState state) {
        return state.get(HOUR);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Deprecated
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int time = (int) (world.getDayTime() % 24000);
        int power = MathHelper.clamp(MathHelper.floor(time / 1500D), 0, 15);
        int hour = MathHelper.clamp(MathHelper.floor(time / 1000D), 0, 24);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ClockBlockTile){
            ((ClockBlockTile) te).setInitialRoll(hour);

        }
        world.setBlockState(pos, state.with(POWER, power).with(HOUR, hour), 2);
    }
    //TODO: make this cleaner. move to tile
    public static void updatePower(BlockState bs, World world, BlockPos pos) {
        if (!world.isRemote){
            // 0-24000
            int time = (int) (world.getDayTime() % 24000);
            int power = MathHelper.clamp(MathHelper.floor(time / 1500D), 0, 15);
            int hour = MathHelper.clamp(MathHelper.floor(time / 1000D), 0, 24);
            if (bs.get(HOUR) != hour){
                ResourceLocation res;
                if (hour % 2 == 0) {
                    res = CommonUtil.TICK_1;
                } else {
                    res = CommonUtil.TICK_2;
                }

                world.playSound(null, pos, ForgeRegistries.SOUND_EVENTS.getValue(res),
                        SoundCategory.BLOCKS, (float) .3, 1.2f);

            }
            world.setBlockState(pos, bs.with(POWER, power).with(HOUR, hour), 3);
        }
    }
}
