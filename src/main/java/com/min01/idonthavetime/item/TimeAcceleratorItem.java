package com.min01.idonthavetime.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult.Type;

public class TimeAcceleratorItem extends Item
{
	protected int secondsToSkip = 500;
	protected int areaRadius = 5;
	public static final String TICKRATE = "Tickrate";
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
		if(!level.isClientSide && this.accelerateRandomTick(state.getBlock(), level, pos))
		{
			return InteractionResult.SUCCESS;
		}
	    return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) 
	{
	    ItemStack stack = p_41433_.getMainHandItem();
	    CompoundTag tag = stack.getTag();
	    if(p_41433_.pick(p_41433_.getAttackRange(), 0, true).getType() == Type.MISS)
	    {
	    	this.checkIfHasNbt(p_41433_, stack);
		    if(p_41433_.isShiftKeyDown())
		    {
		    	if(tag.getInt(TICKRATE) > 11) 
		    	{
		    		tag.putInt(TICKRATE, tag.getInt(TICKRATE) - 10);
		    		this.secondsToSkip = tag.getInt(TICKRATE);
		    	}
				if(p_41433_ instanceof ServerPlayer serverPlayer)
				{
					serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("time_acceleator.changed_acceleration_second", this.secondsToSkip)));
				}
		    }
		    else if(tag.getInt(TICKRATE) < 491)
		    {
		    	tag.putInt(TICKRATE, tag.getInt(TICKRATE) + 10);
		    	this.secondsToSkip = tag.getInt(TICKRATE);
				if(p_41433_ instanceof ServerPlayer serverPlayer)
				{
					serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("time_acceleator.changed_acceleration_second", this.secondsToSkip)));
				}
		    }
	    }
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
		if(!(p_41400_ instanceof Player) && !p_41400_.getPersistentData().contains(TICKRATE_MODIFIED))
		{
			MobEffectInstance instance = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 510, 100, false, false, false);
			p_41400_.addEffect(instance);
			for(int i = 0; i < this.secondsToSkip * 10; i++)
			{
				p_41400_.tick(); 
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) 
	{
		if(this.getAccelerationMode(p_41404_) == AccelerationMode.AREA)
		{
			List<LivingEntity> list = p_41405_.getEntitiesOfClass(LivingEntity.class, p_41406_.getBoundingBox().inflate(this.areaRadius, 0, this.areaRadius));
			list.removeIf(t -> t instanceof Player || t == p_41406_ || t instanceof Monster || t instanceof Enemy);
			list.forEach(t -> 
			{
				MobEffectInstance instance = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 510, 100, false, false, false);
				t.addEffect(instance);
				for(int i = 0; i < this.secondsToSkip; i++)
				{
					t.tick();
				}
			});
			
			int x = Mth.floor(p_41406_.getX());
            int z = Mth.floor(p_41406_.getZ());

            for(int i = -this.areaRadius; i <= this.areaRadius; ++i)
            {
            	for(int i2 = -this.areaRadius; i2 <= this.areaRadius; ++i2)
            	{
        			BlockPos blockPos = new BlockPos(x + i, p_41406_.getY(), z + i2);
        			BlockState state = p_41405_.getBlockState(blockPos);
        			this.accelerateBlockEntity(state, p_41405_, blockPos);
        			if(!p_41405_.isClientSide)
        			{
            			this.accelerateRandomTick(state.getBlock(), p_41405_, blockPos);
        			}
            	}
            }
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
			serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("time_acceleator.changed_acceleration_mode", type.name)));
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
	
	@SuppressWarnings("deprecation")
	public boolean accelerateRandomTick(Block block, Level level, BlockPos pos)
	{
		if(block.isRandomlyTicking(level.getBlockState(pos)))
		{
			for(int i = 0; i < this.secondsToSkip * 10; i++)
			{
				block.randomTick(level.getBlockState(pos), (ServerLevel) level, pos, level.random);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean accelerateBlockEntity(BlockState state, Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockState.hasBlockEntity() && blockEntity != null && !blockEntity.isRemoved())
		{
			BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) blockEntity.getBlockState().getTicker(level, blockEntity.getType());
			if(ticker != null)
			{
				for(int i = 0; i < this.secondsToSkip * 10; i++)
				{
					ticker.tick(level, pos, blockEntity.getBlockState(), blockEntity); 
				}
				return true;
			}
		}
		return false;
	}
	
	public void checkIfHasNbt(Player player, ItemStack stack) 
	{
		if(!stack.hasTag())
		{
			CompoundTag tag = new CompoundTag();
			tag.putInt(TICKRATE, 10);
			stack.setTag(tag);
		}
		else
		{
			this.secondsToSkip = stack.getTag().getInt(TICKRATE);
		}
	}
	
	@Override
	public void appendHoverText(ItemStack p_41421_, Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_)
	{
		p_41423_.add(Component.translatable("item.idonthavetime.time_acceleator.desc1"));
		p_41423_.add(Component.translatable("item.idonthavetime.time_acceleator.desc2"));
		p_41423_.add(Component.translatable("item.idonthavetime.time_acceleator.desc3"));
	}
}
