package net.tiagofar78.prestigetags.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import net.tiagofar78.prestigetags.PrestigeTags;

public class ConfigManager {
	
	private static ConfigManager manager = new ConfigManager();
	
	public static ConfigManager getInstance() {
		return manager;
	}
	
	private int _magnataUpdateTime;
	private boolean _shouldUpdateMagnataForSameHolder;
	private String _previousMagnataName;
	private List<String> _magnataUpdateCommands;
	
	private ConfigManager() {
		YamlConfiguration config = PrestigeTags.getYamlConfiguration();
		
		_magnataUpdateTime = config.getInt("Magnata.UpdateTimeSeconds");
		_shouldUpdateMagnataForSameHolder = config.getBoolean("Magnata.ShouldUpdateForSameMagnata");
		_previousMagnataName = config.getString("Magnata.PreviousTagHolderName");
		_magnataUpdateCommands = config.getStringList("Magnata.SetMagnataCommands");
	}
	
	public int getMagnataUpdateTimeSeconds() {
		return _magnataUpdateTime;
	}
	
	public boolean shouldUpdateMagnataForSameHolder() {
		return _shouldUpdateMagnataForSameHolder;
	}
	
	public String getPreviousMagnataName() {
		return _previousMagnataName;
	}
	
	public void setNewMagnata(String name) {
		_previousMagnataName = name;
		
		YamlConfiguration config = PrestigeTags.getYamlConfiguration();
		config.set("Magnata.PreviousTagHolderName", name);
		
		PrestigeTags.saveConfiguration(config);
	}
	
	public List<String> getMagnataUpdateCommands() {
		List<String> clonedList = new ArrayList<String>();
		for (String element : _magnataUpdateCommands) {
			clonedList.add(element.substring(0));
		}
		
		return clonedList;
	}

}
