package com.nhksos.jjfs85.BetterShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BSutils {

	static boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender,
					"Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	final static boolean credit(CommandSender player, int amount)
			throws Exception {
		int balance = 0;
		Object db = BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.iConomy").getField("db")
				.get(null);
		balance = (Integer) BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"get_balance", String.class).invoke(db,
				((Player) player).getName());
		BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"set_balance", String.class, Integer.TYPE).invoke(db,
				((Player) player).getName(), balance + amount);
		return true;
	}

	final static boolean debit(CommandSender player, int amount)
			throws Exception {
		int balance = 0;
		Object db = BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.iConomy").getField("db")
				.get(null);
		balance = (Integer) BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"get_balance", String.class).invoke(db,
				((Player) player).getName());
		if (balance < amount)
			return false;
		BetterShop.class.getClassLoader().loadClass(
				"com.nijikokun.bukkit.iConomy.Database").getMethod(
				"set_balance", String.class, Integer.TYPE).invoke(db,
				((Player) player).getName(), balance - amount);
		return true;
	}

	static boolean hasPermission(CommandSender player, String node,
			boolean notify) {
		try {
			if (BetterShop.Permissions.has((Player) player, node)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (notify == true) PermDeny(player,node);
		return false;
	}

	final static void PermDeny(CommandSender player, String node) {
		BSutils.sendMessage(player, String.format(BetterShop.configfile
				.getString("permdeny").replace("<perm>", "%1$s"), node));
	}

	final static void sendMessage(CommandSender player, String s) {
		player.sendMessage(BetterShop.configfile.getString("prefix") + s);
	}

}
