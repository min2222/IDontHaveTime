package com.min01.idonthavetime.event;

import com.min01.idonthavetime.IDontHaveTime;
import com.min01.idonthavetime.item.TimeAcceleratorItem;
import com.min01.idonthavetime.item.TimeAcceleratorItem.AccelerationMode;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IDontHaveTime.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlerForge 
{
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		ItemStack stack = event.getItemStack();
		Player player = event.getEntity();
		if(stack.getItem() instanceof TimeAcceleratorItem item && player.isShiftKeyDown())
		{
			if(item.getAccelerationMode(stack) == AccelerationMode.SINGLE)
			{
				item.setAccelerationMode(stack, player, AccelerationMode.AREA);
			}
			else
			{
				item.setAccelerationMode(stack, player, AccelerationMode.SINGLE);
			}
			event.setCanceled(true);
		}
	}
}
