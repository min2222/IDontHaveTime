package com.min01.idonthavetime.event;

import com.min01.idonthavetime.IDontHaveTime;
import com.min01.idonthavetime.item.IDontHaveTimeItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IDontHaveTime.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler 
{
	@SubscribeEvent
	public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event)
	{
		if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
		{
			event.accept(IDontHaveTimeItems.TIME_ACCELERATOR.get());
		}
	}
}
