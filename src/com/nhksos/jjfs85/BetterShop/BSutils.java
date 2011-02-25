package com.nhksos.jjfs85.BetterShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijikokun.bukkit.iConomy.Account;
import com.nijikokun.bukkit.iConomy.iConomy;

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

	final static boolean credit(CommandSender player, float amount)
			throws Exception {
		Account account = iConomy.Bank.getAccount(((Player)player).getName());
		double balance = account.getBalance();
		account.setBalance(balance + amount);
		account.save();
		return true;
	}

	final static boolean debit(CommandSender player, int amount)
			throws Exception {
		Account account = iConomy.Bank.getAccount(((Player)player).getName());
		double balance = account.getBalance();
		account.setBalance(balance - amount);
		account.save();
		return true;
	}

	@SuppressWarnings("static-access")
	static boolean hasPermission(CommandSender player, String node,
			boolean notify) {
		try {
			if (BetterShop.Permissions.Security.has((Player) player, node)) {
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
