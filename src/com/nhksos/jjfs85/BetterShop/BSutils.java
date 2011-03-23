package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Item.Item;
import com.jascotty2.Item.ItemStockEntry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.system.*;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class BSutils {

    static boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "Cannot execute that command, I don't know who you are!");
            return true;
        } else {
            return false;
        }
    }

    static boolean credit(CommandSender player, double amount) {
        if (amount <= 0) {
            return amount == 0;
        }
        Account account = BetterShop.iConomy.getBank().getAccount(((Player) player).getName());
        double preAmt = account.getBalance();
        account.add(amount);
        if (account.getBalance() == preAmt) {
            // something seems to be wrong with iConomy: reload it
            BetterShop.Log(Level.SEVERE, "Failed to credit player: attempting iConomy reload");
            if (reloadIConomy(player.getServer())) {
                account.add(amount);
                if (account.getBalance() != preAmt) {
                    return true;
                }
            }
            BetterShop.Log(Level.SEVERE, "Failed.");
        }
        return true;
    }

    static boolean debit(CommandSender player, double amount) {
        if (amount <= 0) {
            return amount == 0;
        }
        Account account = BetterShop.iConomy.getBank().getAccount(((Player) player).getName());
        double preAmt = account.getBalance();
        // don't allow account to go negative
        if (preAmt < amount) {
            return false;
        }
        account.subtract(amount);
        if (account.getBalance() == preAmt) {
            // something seems to be wrong with iConomy: reload it
            BetterShop.Log(Level.SEVERE, "Failed to debit player: attempting iConomy reload");
            if (reloadIConomy(player.getServer())) {
                account.subtract(amount);
                if (account.getBalance() != preAmt) {
                    return true;
                }
            }
            BetterShop.Log(Level.SEVERE, "Failed.");
        }
        return true;
    }

    static boolean reloadIConomy(Server serv) {
        try {
            PluginManager m = serv.getPluginManager();
            Plugin icon = m.getPlugin("iConomy");
            if (icon != null) {
                m.disablePlugin(icon);
                m.enablePlugin(icon);

                return true;
            }
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, "Error");
            BetterShop.Log(Level.SEVERE, ex);
        }
        return false;
    }

    static boolean hasPermission(CommandSender player, String node) {
        return hasPermission(player, node, false);
    }

    static boolean hasPermission(CommandSender player, String node, boolean notify) {
        if (BetterShop.Permissions == null) {
            // only ops have access to .admin
            if ((node == null || node.length() < 16)
                    || (!node.substring(0, 16).equalsIgnoreCase("BetterShop.admin") || player.isOp())) {
                return true;
            }
        } else if (player instanceof Player) {
            if (BetterShop.Permissions.Security.has((Player) player, node)) {
                return true;
            }
        } else { // is ConsoleSender
            return true;
        }
        if (notify == true) {
            PermDeny(player, node);
        }
        return false;
    }

    static void PermDeny(CommandSender player, String node) {
        BSutils.sendMessage(player, String.format(BetterShop.config.getString("permdeny").replace("<perm>", "%1$s"), node));
    }

    static void sendMessage(CommandSender player, String s) {
        if (player != null) {
            player.sendMessage(BetterShop.config.getString("prefix") + s);
        }
    }

    static void sendMessage(CommandSender player, String s, boolean isPublic) {
        if (player != null) {
            if (isPublic) {
                broadcastMessage(player, s);
            } else {
                player.sendMessage(BetterShop.config.getString("prefix") + s);
            }
        }
    }

    static void broadcastMessage(CommandSender player, String s) {
        if (player != null) {
            player.getServer().broadcastMessage(BetterShop.config.getString("prefix") + s);
        }
        BetterShop.Log("(public announcement) " + s.replaceAll("\\\u00A7.", ""));
    }

    static void broadcastMessage(CommandSender player, String s, boolean includePlayer) {
        if (player != null) {
            if (includePlayer) {
                broadcastMessage(player, s);
            } else {
                String name = player instanceof Player ? ((Player) player).getDisplayName() : "";
                for (Player p : player.getServer().getOnlinePlayers()) {
                    if (!p.getDisplayName().equals(name)) {
                        //player.getServer().broadcastMessage(BetterShop.config.getString("prefix") + s);
                        p.sendMessage(BetterShop.config.getString("prefix") + s);
                    }
                }
                BetterShop.Log("(public announcement) " + s.replaceAll("\\\u00A7.", ""));
            }
        }
    }

    /*
     * all items in the user's inventory
     * does not run check for tools, so tools with damage are returned seperately
     */
    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv) {
        ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

        ItemStack[] its = player.getInventory().getContents();
        for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
            if (its[i].getAmount() > 0) {
                ItemStockEntry find = new ItemStockEntry(its[i]);
                int pos = inv.indexOf(find);
                if (pos >= 0) {
                    inv.get(pos).AddAmount(its[i].getAmount());
                } else {
                    inv.add(find);
                }
            }
        }

        return inv;
    }

    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, Item toFind) {
        if(toFind==null){
            return getTotalInventory(player, onlyInv);
        }// else
        return getTotalInventory(player, onlyInv, new Item[]{toFind});
    }

    public static ArrayList<ItemStockEntry> getTotalInventory(Player player, boolean onlyInv, Item[] toFind) {
        if(toFind==null || toFind.length==0){
            return getTotalInventory(player, onlyInv);
        }
        ArrayList<ItemStockEntry> inv = new ArrayList<ItemStockEntry>();

        ItemStack[] its = player.getInventory().getContents();
        for (int i = (onlyInv ? 9 : 0); i <= 35; ++i) {
            if (its[i].getAmount() > 0) {
                for (Item it : toFind) {
                    if (it != null && it.equals(its[i])) {
                        ItemStockEntry find = new ItemStockEntry(its[i]);
                        int pos = inv.indexOf(find);
                        if (pos >= 0) {
                            inv.get(pos).AddAmount(its[i].getAmount());
                        } else {
                            inv.add(find);
                        }
                        break;
                    }
                }
            }
        }
        return inv;
    }
    
}
