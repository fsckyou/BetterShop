/**
 * 
 */
package com.quantumrex.DynaPrice;

import org.bukkit.scheduler.BukkitScheduler;

import com.nhksos.jjfs85.BetterShop.BetterShop;

/**
 * @author rwenner
 *
 */
public class DynaPrice {
	BetterShop owner;
	BukkitScheduler manager;
	DataMiner spy;

	public DynaPrice(BetterShop plugin){
		owner = plugin;
		manager = owner.getServer().getScheduler();
		spy = new DataMiner(this);
	}
	
	public void start(){
		spy.taskId = manager.scheduleAsyncRepeatingTask(owner, spy, 0L, 1200L);
	}
	
	public void stop(){
		manager.cancelTask(spy.taskId);
	}
}
