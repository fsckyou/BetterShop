/**
 * 
 */
package com.quantumrex.DynaPrice;

import org.bukkit.Server;

import com.nhksos.jjfs85.BetterShop.BetterShop;
/**
 * @author rwenner
 *
 */
public class DataMiner implements Runnable {
	public int taskId;
	DynaPrice owner;
	Server main;
	
	int player_minutes;
	
	public DataMiner(DynaPrice plugin){
		player_minutes = 0;
		owner = plugin;
		main = owner.owner.getServer();
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		player_minutes += main.getOnlinePlayers().length;
		if (player_minutes > 60){
			player_minutes -= 60;
			// TODO hourly calculations
		}
	}

}
