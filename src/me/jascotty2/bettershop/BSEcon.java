/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: handler for economy events
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.jascotty2.bettershop;

import com.nijikokun.register_1_3.payment.Method;
import com.nijikokun.register_1_3.payment.Methods;
import java.util.Map.Entry;
import me.jascotty2.bettershop.enums.EconMethod;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginManager;

/**
 * @author jacob
 */
public class BSEcon extends ServerListener {

	protected static Method economyMethod = null;
	protected static Methods _econMethods = new Methods();
	protected static String methodName = null;
	// iconomy seems to throw alot of errors...
	// this is to only display one
	static boolean _pastBalanceErr = false;
	static BetterShop plugin;
	PluginManager pm;
	
	public BSEcon(BetterShop plugin){
		BSEcon.plugin = plugin;
		pm = plugin.getServer().getPluginManager();
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are using
		if (_econMethods != null && Methods.hasMethod() && Methods.checkDisabled(event.getPlugin())) {
			economyMethod = null;
			methodName = null;
			Methods.reset();
			BetterShopLogger.Log(" Economy Plugin was disabled.");
		}
	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if (!Methods.hasMethod()) {
			if (Methods.setMethod(pm) && Methods.hasMethod()){
				economyMethod = Methods.getMethod();
				methodName = economyMethod.getName() + " v" + economyMethod.getVersion();
				BetterShopLogger.Log("Using " + methodName + " for economy");
			}
		}
	}

	public static boolean active() {
		return BetterShop.config.econ != EconMethod.AUTO || economyMethod != null;
	}

	public static String getMethodName() {
		if(BetterShop.config.econ == EconMethod.AUTO) return methodName;
		if(BetterShop.config.econ == EconMethod.BULTIN) return "BettershopEcon";
		return "Experience";
	}

	public static boolean hasAccount(Player pl) {
		return pl != null && (BetterShop.config.econ != EconMethod.AUTO ||
				(economyMethod != null && economyMethod.hasAccount(pl.getName())));
	}

	public static boolean canAfford(Player pl, double amt) {
		if(BetterShop.config.econ != EconMethod.AUTO) {
			return pl != null ? getBalance(pl) >= amt : false;
		}
		return pl != null ? getBalance(pl.getName()) >= amt : false;
	}

	public static double getBalance(Player pl) {
		if (pl == null) {
			return 0;
		} else if(BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if(BetterShop.config.econ == EconMethod.EXP) {
			return pl.getExp();
		} else if(BetterShop.config.econ == EconMethod.TOTAL) {
			return pl.getTotalExperience();
		}
		return pl == null ? 0 : getBalance(pl.getName());
	}

	public static double getBalance(String playerName) {
		if (playerName == null) {
			return 0;
		} else if(BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if(BetterShop.config.econ == EconMethod.EXP) {
			Player p = plugin.getServer().getPlayerExact(playerName);
			return p == null ? 0 : p.getExp();
		} else if(BetterShop.config.econ == EconMethod.TOTAL) {
			Player p = plugin.getServer().getPlayerExact(playerName);
			return p == null ? 0 : p.getTotalExperience();
		}
		try {
			if (economyMethod != null && economyMethod.hasAccount(playerName)) {
				return economyMethod.getAccount(playerName).balance();
			}
		} catch (Exception e) {
			if (!_pastBalanceErr) {
				BetterShopLogger.Severe("Error looking up player balance \n"
						+ "(this error will only show once)", e, false);
				_pastBalanceErr = true;
			}
		}
		return 0;
	}

	public static void addMoney(Player pl, double amt) {
		if(BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if(BetterShop.config.econ == EconMethod.EXP) {
			pl.setExp(pl.getExp() + (float)amt);
		} else if(BetterShop.config.econ == EconMethod.TOTAL) {
			pl.setTotalExperience(pl.getTotalExperience() + (int)amt);
		} else {
			addMoney(pl.getName(), amt);
		}
	}

	public static void addMoney(String playerName, double amt) {
		if(BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if(BetterShop.config.econ == EconMethod.EXP) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if(pl != null) pl.setExp(pl.getExp() + (float)amt);
		} else if(BetterShop.config.econ == EconMethod.TOTAL) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if(pl != null) pl.setTotalExperience(pl.getTotalExperience() + (int)amt);
		} else if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).add(amt);
		}
	}

	public static void subtractMoney(Player pl, double amt) {
		if(pl != null){
			if(BetterShop.config.econ == EconMethod.BULTIN) {
				throw new UnsupportedOperationException("Bultin Not supported yet.");
			} else if(BetterShop.config.econ == EconMethod.EXP) {
				if(pl.getExp() > (int)amt)
					pl.setExp(pl.getExp() - (float)amt);
				else
					pl.setExp(0);
			} else if(BetterShop.config.econ == EconMethod.TOTAL) {
				if(pl.getTotalExperience() > (int)amt)
					pl.setTotalExperience(pl.getTotalExperience() - (int)amt);
				else
					pl.setTotalExperience(0);
			} else {
				subtractMoney(pl.getName(), amt);
			}
		}
	}

	public static void subtractMoney(String playerName, double amt) {
		if(BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if(BetterShop.config.econ == EconMethod.EXP) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if(pl != null) {
				if(pl.getExp() > (int)amt)
					pl.setExp(pl.getExp() - (float)amt);
				else
					pl.setExp(0);
			}
		} else if(BetterShop.config.econ == EconMethod.TOTAL) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if(pl != null) {
				if(pl.getTotalExperience() > (int)amt)
					pl.setTotalExperience(pl.getTotalExperience() - (int)amt);
				else
					pl.setTotalExperience(0);
			}
		} else if (economyMethod != null) {
			if (!economyMethod.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			economyMethod.getAccount(playerName).subtract(amt);
		}
	}

	public static double getPlayerDiscount(Player p) {
		if (p != null && !BSPermissions.has(p, "BetterShop.discount.none")) {
			for (Entry<String, Double> g : BetterShop.getSettings().groups.entrySet()) {
				if (BSPermissions.has(p, "BetterShop.discount." + g.getKey())) {
					return g.getValue();
				}
			}
		}
		return 0;
	}

	public static boolean credit(Player player, double amount) {
		if (amount <= 0) {
			return amount == 0 || debit(player, -amount);
		}
		if (BSEcon.active()) {
			try {
				if (bankTransaction(player.getName(), amount)) {
					return true;
				}
			} catch (Exception ex) {
				BetterShopLogger.Severe("Failed to credit player", ex, false);
				return true;
			}
			BetterShopLogger.Severe("Failed to credit player", false);
			// something seems to be wrong with iConomy: reload it
//			BetterShopLogger.Log(Level.SEVERE, "Failed to credit player: attempting iConomy reload", false);
//			if (reloadIConomy(player.getServer())) {
//				try {
//					if (bankTransaction(player.getName(), amount)) {
//						return true;
//					}
//				} catch (Exception ex) {
//				}
//			}
//			BetterShopLogger.Log(Level.SEVERE, "iConomy reload failed to resolve issue.", false);
		} else {
			BetterShopLogger.Severe("Failed to credit player: no economy plugin", false);
			return false;
		}
		return true;
	}

	public static boolean debit(Player player, double amount) {
		if (amount <= 0) {
			return amount == 0 || credit(player, -amount);
		} else if(getBalance(player) < amount){
			return false;
		}
		if (BSEcon.active()) {
			try {
				if (bankTransaction(player.getName(), -amount)) {
					return true;
				}
			} catch (Exception ex) {
				BetterShopLogger.Severe("Failed to debit player", ex, false);
				return true;
			}
			BetterShopLogger.Severe("Failed to debit player", false);

			// something seems to be wrong with iConomy: reload it
//			BetterShopLogger.Log(Level.SEVERE, "Failed to debit player: attempting iConomy reload", false);
//			if (reloadIConomy(player.getServer())) {
//				try {
//					if (bankTransaction(player.getName(), -amount)) {
//						return true;
//					}
//				} catch (Exception ex) {
//				}
//			}
//			BetterShopLogger.Log(Level.SEVERE, "iConomy reload failed to resolve issue.", false);
		} else {
			BetterShopLogger.Severe("Failed to debit player: no economy plugin", false);
			return false;
		}
		return true;
	}

	private static boolean bankTransaction(String player, double amount) {
		// don't allow account to go negative
		double preAmt = BSEcon.getBalance(player);
		if (amount > 0 || preAmt >= -amount) {
			BSEcon.addMoney(player, amount);
			if (BetterShop.config.econ== EconMethod.AUTO
					&& BetterShop.getSettings().BOSBank != null
					&& BSEcon.economyMethod.hasBanks()
					&& BSEcon.economyMethod.hasBank(BetterShop.getSettings().BOSBank)) {
				BSEcon.addMoney(BetterShop.getSettings().BOSBank, -amount);
			}
			return BSEcon.getBalance(player) != preAmt;
		}
		return false;
	}

	public static String format(double amt) {
		try {
			if (economyMethod != null) {
				return economyMethod.format(amt);
			}
			return String.format("%.2f", amt) + " "
				+ (amt > 1 || amt < 1 ? BetterShop.getSettings().pluralCurrency
				: BetterShop.getSettings().defaultCurrency);
		} catch (Exception ex) {
			BetterShopLogger.Warning("Error Formatting Currency", ex, false);
		}
		return String.format("%.2f", amt);
	}

//
//	static boolean reloadIConomy(Server serv) {
//		try {
//			PluginManager m = serv.getPluginManager();
//			Plugin icon = m.getPlugin("iConomy");
//			if (icon != null) {
//				m.disablePlugin(icon);
//				m.enablePlugin(icon);
//
//				return true;
//			}
//		} catch (Exception ex) {
//			BetterShopLogger.Log(Level.SEVERE, "Error reloading iConomy", ex);
//		}
//		return false;
//	}

} // end class BSEcon

