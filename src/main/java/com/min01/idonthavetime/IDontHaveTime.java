package com.min01.idonthavetime;

import com.min01.idonthavetime.config.IDontHaveTimeConfig;
import com.min01.idonthavetime.item.IDontHaveTimeItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(IDontHaveTime.MODID)
public class IDontHaveTime 
{
	public static final String MODID = "idonthavetime";
	
	public IDontHaveTime() 
	{
	    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
	    IDontHaveTimeItems.ITEMS.register(modBus);
	    
	    IDontHaveTimeConfig.loadConfig(IDontHaveTimeConfig.CONFIG, FMLPaths.CONFIGDIR.get().resolve("i-dont-have-time.toml").toString());
	}
}
