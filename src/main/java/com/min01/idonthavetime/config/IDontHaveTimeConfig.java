package com.min01.idonthavetime.config;

import java.io.File;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;

public class IDontHaveTimeConfig
{
    private static ForgeConfigSpec.Builder BUILDER;
    public static ForgeConfigSpec CONFIG;

	public static ForgeConfigSpec.ConfigValue<? extends Integer> accelerateRadius;
    
    public static void loadConfig(ForgeConfigSpec config, String path) 
    {
        CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
    
    static 
    {
    	BUILDER = new ForgeConfigSpec.Builder();
    	IDontHaveTimeConfig.init(IDontHaveTimeConfig.BUILDER);
    	CONFIG = IDontHaveTimeConfig.BUILDER.build();
    }
	
    public static void init(ForgeConfigSpec.Builder config) 
    {
    	config.push("Settings");
    	IDontHaveTimeConfig.accelerateRadius = config.comment("accelerate radius for area mode of time accelerator item").define("accelerateRadius", 5);
        config.pop();
    }
}
