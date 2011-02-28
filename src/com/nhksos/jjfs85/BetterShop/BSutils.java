package com.nhksos.jjfs85.BetterShop;

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.system.*;

public class BSutils {
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger("Minecraft");

	static boolean anonymousCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender,
					"Cannot execute that command, I don't know who you are!");
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("static-access")
	final static boolean credit(CommandSender player, double amount)
			throws Exception {
		String name = ((Player) player).getName();
		Account account = BetterShop.iConomy.getBank().getAccount(name);
		double balance = account.getBalance();
		account.setBalance(balance + amount);
		account.save();
		return true;
	}

	@SuppressWarnings("static-access")
	final static boolean debit(CommandSender player, double amount)
			throws Exception {
		String name = ((Player) player).getName();
		Account account = BetterShop.iConomy.getBank().getAccount(name);
		double balance = account.getBalance();
		if (balance < amount)
			return false;
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
		}
		catch (ClassCastException e1) {
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (notify == true)
			PermDeny(player, node);
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
