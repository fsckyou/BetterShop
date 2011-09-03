/**
 * Programmer: Jacob Scott
 * Program Name: BSEcon
 * Description: handler for econ events
 * Date: Apr 11, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.nijikokun.register_21.payment.Method;
import com.nijikokun.register_21.payment.Methods;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/**
 * @author jacob
 */
public class BSEcon extends ServerListener {

	protected static Method economyMethod = null;
	protected static Methods _econMethods = new Methods();
	protected static String methodName = null;

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are using
		if (_econMethods != null && _econMethods.hasMethod() && _econMethods.checkDisabled(event.getPlugin())) {
			economyMethod = null;
			methodName = null;
			BetterShop.Log(" Economy Plugin was disabled.");
		}
	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if (!_econMethods.hasMethod()) {
			if (_econMethods.setMethod(event.getPlugin())) {
				economyMethod = _econMethods.getMethod();
				methodName = economyMethod.getName() + " v" + economyMethod.getVersion();
				BetterShop.Log("Using " + methodName + " for economy");
			}
		}
	}

	public static boolean active() {
		return economyMethod != null;
	}

	public static String getMethodName() {
		return methodName;
	}

	public static boolean hasAccount(Player pl){
		return pl != null && economyMethod != null && economyMethod.hasAccount(pl.getName());
	}

    public static boolean canAfford(Player pl, double amt) {
		return pl != null ? getBalance(pl.getName()) >= amt : false;
    }

	public static double getBalance(Player pl) {
		return getBalance(pl.getName());
	}

	public static double getBalance(String playerName) {
		if (economyMethod != null && economyMethod.hasAccount(playerName)) {
			return economyMethod.getAccount(playerName).balance();
		}
		return 0;
	}

	public static void addMoney(Player pl, double amt) {
		addMoney(pl.getName(), amt);
	}

	public static void addMoney(String playerName, double amt) {
		if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).add(amt);
		}
	}

	public static void subtractMoney(Player pl, double amt) {
		subtractMoney(pl.getName(), amt);
	}

	public static void subtractMoney(String playerName, double amt) {
		if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).subtract(amt);
		}
	}

	public static String format(double amt) {
		if (economyMethod != null) {
			return economyMethod.format(amt);
		}
		return String.format("%.2f", amt);
	}
} // end class BSEcon

