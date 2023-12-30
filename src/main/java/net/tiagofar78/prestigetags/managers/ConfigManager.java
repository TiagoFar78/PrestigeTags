package net.tiagofar78.prestigetags.managers;

import java.util.List;

public class ConfigManager {
	
	private static ConfigManager manager = new ConfigManager();
	
	public static ConfigManager getInstance() {
		return manager;
	}
	
	public int getMagnataUpdateTimeSeconds() {
		return 0;
	}
	
	public boolean shouldUpdateMagnataForSameHolder() {
		return false;
	}
	
	public String getPreviousMagnataName() {
		return null;
	}
	
	public void setNewMagnata(String name) {
		
	}
	
	public List<String> getMagnataUpdateCommands() {
		return null;
	}

}
