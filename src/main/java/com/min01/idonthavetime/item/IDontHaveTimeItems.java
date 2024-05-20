package com.min01.idonthavetime.item;

import com.min01.idonthavetime.IDontHaveTime;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IDontHaveTimeItems 
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IDontHaveTime.MODID);
	
	public static final RegistryObject<Item> TIME_ACCELERATOR = ITEMS.register("time_accelerator", () -> new TimeAcceleratorItem());
}
