package net.tiagofar78.prestigetags;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.tiagofar78.prestigetags.tags.MagnataTag;
import net.tiagofar78.prestigetags.tags.PrestigeTag;

public class PrestigeTags extends JavaPlugin {
	
	private static final PrestigeTag[] TAGS = { new MagnataTag() };
	
	@Override
	public void onEnable() {		
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
		
		for (PrestigeTag tag : TAGS) {
			tag.registerTag();
		}
	}
	
	public static YamlConfiguration getYamlConfiguration() {
		return YamlConfiguration.loadConfiguration(configFile());
	}
	
	private static File configFile() {
		return new File(getPrestigeTags().getDataFolder(), "config.yml");
	}
	
	public static PrestigeTags getPrestigeTags() {
		return (PrestigeTags)Bukkit.getServer().getPluginManager().getPlugin("TF_PrestigeTags");
	}
	
	public static void saveConfiguration(YamlConfiguration config) {
		File configFile = configFile();
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
    
}
