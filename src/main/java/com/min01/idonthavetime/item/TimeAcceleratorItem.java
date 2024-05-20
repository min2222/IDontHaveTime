package com.min01.idonthavetime.item;

import java.util.List;

import com.min01.entitytimer.TimerUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class TimeAcceleratorItem extends Item
{
	protected int secondsToSkip = 60;
	protected int areaRadius = 10;
	public static final String TICKRATE_MODIFIED = "TickrateModified";
	
	public enum AccelerationMode
	{
		AREA("Area"),
		SINGLE("Single");
		
		public String name;
		
		private AccelerationMode(String name) 
		{
			this.name = name;
		}
	}
	
	public TimeAcceleratorItem() 
	{
		super(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
	}
	
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
	    BlockState state = level.getBlockState(pos);
		if(player.isShiftKeyDown() && this.accelerateBlockEntity(state, level, pos))
		{
			return InteractionResult.SUCCESS;
		}
	    return InteractionResult.PASS;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult useOn(UseOnContext p_41427_) 
	{
		Level level = p_41427_.getLevel();
	    BlockState state = level.getBlockState(p_41427_.getClickedPos());
	    Block block = state.getBlock();
	    if(!level.isClientSide)
	    {
	    	if(block.isRandomlyTicking(state))
	    	{
	    		for(int n = 0; n < this.secondsToSkip * 20; n++)
	    		{
	    			block.randomTick(state, (ServerLevel)level, p_41427_.getClickedPos(), level.random); 
	    		}
	    	} 
	    	return InteractionResult.SUCCESS;
	    }
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) 
	{
	    ItemStack stack = p_41433_.getItemInHand(p_41434_);
		return InteractionResultHolder.fail(stack);
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack p_41398_, Player p_41399_, LivingEntity p_41400_, InteractionHand p_41401_) 
	{
		Level level = p_41399_.level;
		if(level.isClientSide)
		{
			return InteractionResult.PASS;
		}
		if(!(p_41400_ instanceof Player))
		{
			MobEffectInstance instance = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 510, 100, false, false, false);
			p_41400_.addEffect(new MobEffectInstance(instance));
			TimerUtil.setTickrate(p_41400_, this.secondsToSkip * 20);
			if(!p_41400_.getPersistentData().contains(TICKRATE_MODIFIED))
			{
				p_41400_.getPersistentData().putBoolean(TICKRATE_MODIFIED, true);
			}
		}
		return super.interactLivingEntity(p_41398_, p_41399_, p_41400_, p_41401_);
	}
	
	@Override
	public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) 
	{
		if(this.getAccelerationMode(p_41404_) == AccelerationMode.AREA)
		{
			List<LivingEntity> list = p_41405_.getEntitiesOfClass(LivingEntity.class, p_41406_.getBoundingBox().inflate(this.areaRadius));
			list.removeIf(t -> t instanceof Player || t == p_41406_ || t instanceof Monster);
			list.forEach(t -> 
			{
				MobEffectInstance instance = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 510, 100, false, false, false);
				t.addEffect(new MobEffectInstance(instance));
				TimerUtil.setTickrate(t, this.secondsToSkip * 20);
				if(!t.getPersistentData().contains(TICKRATE_MODIFIED))
				{
					t.getPersistentData().putBoolean(TICKRATE_MODIFIED, true);
				}
			});
		}
	}
	
	public void setAccelerationMode(ItemStack stack, Player player, AccelerationMode type)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains(AccelerationMode.AREA.name) && type == AccelerationMode.SINGLE)
		{
			tag.remove(AccelerationMode.AREA.name);
		}
		if(tag.contains(AccelerationMode.SINGLE.name) && type == AccelerationMode.AREA)
		{
			tag.remove(AccelerationMode.SINGLE.name);
		}
		if(player instanceof ServerPlayer serverPlayer)
		{
			serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("time_acceleator.change_acceleration_type", type.name)));
		}
		tag.putBoolean(type.name, true);
	}
	
	public AccelerationMode getAccelerationMode(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains(AccelerationMode.AREA.name))
		{
			return AccelerationMode.AREA;
		}
		return AccelerationMode.SINGLE;
	}
	
	@SuppressWarnings("unchecked")
	public boolean accelerateBlockEntity(BlockState state, Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) blockEntity.getBlockState().getTicker(level, blockEntity.getType());
		if(!blockState.hasBlockEntity() || blockEntity == null || blockEntity.isRemoved() || ticker == null)
		{
			return false;
		}
		for(int i = 0; i < this.secondsToSkip * 20; i++)
		{
			ticker.tick(level, pos, blockEntity.getBlockState(), blockEntity); 
		}
		return true;
	}
}
