package net.tiagofar78.prestigetags.tags;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import net.tiagofar78.prestigetags.managers.ConfigManager;

public class MagnataTag extends PrestigeTag {
	
	@Override
	public void registerTag() {
		// TODO add a scheduler to keep updating every 10m
	}
	
	@Override
	public void updateTagHolder() {		
		String magnataName = getMagnata();
		
		ConfigManager config = ConfigManager.getInstance();
		
		if (!config.shouldUpdateMagnataForSameHolder() && config.getPreviousMagnataName().equals(magnataName)) {
			return;
		}
		
		List<String> commands = config.getMagnataUpdateCommands();
		
		Server server = Bukkit.getServer();
		for (String command : commands) {
			server.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", magnataName));
		}
	}
	
	private String getMagnata() {
		return null;
	}

}
