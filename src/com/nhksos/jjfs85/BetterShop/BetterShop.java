package com.nhksos.jjfs85.BetterShop;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Bank;
import com.nijikokun.bukkit.Permissions.Permissions;

/*
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {

    public final static Logger logger = Logger.getLogger("Minecraft");
    public static final String name = "BetterShop";
    public final static BSConfig config = new BSConfig();
    public static BSPriceList pricelist = new BSPriceList();
    public static BSTransactionLog transactions = new BSTransactionLog();
    public static BSCommand bscommand = new BSCommand();
    static Permissions Permissions = null;
    static iConomy iConomy = null;
    static Bank iBank = null;
    private final Listener Listener = new Listener();
    private static boolean isLoaded = true;

    private class Listener extends ServerListener {

        public Listener() {
        }

        @SuppressWarnings("static-access")
        @Override
        public void onPluginEnabled(PluginEvent event) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                BetterShop.iConomy = (iConomy) event.getPlugin();
                iBank = iConomy.getBank();
                config.currency = iBank.getCurrency();
                Log("Attached to iConomy.");
            }
            if (event.getPlugin().getDescription().getName().equals("Permissions")) {
                BetterShop.Permissions = (Permissions) event.getPlugin();
                Log("Attached to Permissions or something close enough to it");
            }
        }
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvent(
                Event.Type.PLUGIN_ENABLE, Listener, Priority.Monitor, this);
    }

    @SuppressWarnings("static-access")
    private void hookDepends() {
        if (this.getServer().getPluginManager().isPluginEnabled("iConomy")) {
            iConomy = (iConomy) this.getServer().getPluginManager().getPlugin("iConomy");
            iBank = iConomy.getBank();
            config.currency = iBank.getCurrency();
            Log("Attached to iConomy.");
        } else {
            logger.warning("iConomy not yet found...");
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Permissions")) {
            Permissions = (Permissions) this.getServer().getPluginManager().getPlugin("Permissions");
            Log("Attached to Permissions.");
        } else {
            Log(Level.WARNING, "Permissions not yet found...");
        }
    }

    public void onEnable() {

        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, String.format("Loading %s version %s ...", pdfFile.getName(), pdfFile.getVersion()));

        if(!isLoaded){
            pricelist.reload();
        }if(transactions ==null){
            transactions = new BSTransactionLog();
        }
        if (!pricelist.HasAccess()) {
            Log(Level.SEVERE, "cannot load " + pricelist.pricelistName());
            this.setEnabled(false);
            return;
        }

        // ready items.db
        try {
            itemDb.load(BSConfig.pluginFolder, "items.db");
        } catch (Exception e) {
            Log(Level.SEVERE, "cannot load items.db", e);
            this.setEnabled(false);
            return;
        }

        hookDepends();
        registerEvents();
        isLoaded=true;

        // Just output some info so we can check
        // all is well
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!",
                new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }

    public void onDisable() {

        // NOTE: All registered events are automatically unregistered when a
        // plugin is disabled

        pricelist.close();
        isLoaded=false;
        transactions = null;

        logger.info("BetterShop now unloaded");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        // i don't like seeing these messages all the time..
        //Log(((Player) sender).getName() + " used command " + command.getName());
        
        if ((BetterShop.iConomy == null)) {
            BSutils.sendMessage(sender, "&4 BetterShop is missing a dependency. Check the console.");
            Log(Level.SEVERE, "[BetterShop] Missing: iConomy");
            return true;
        }
        
        if (commandName.equals("shoplist")) {
            return bscommand.list(sender, args);
        } else if (commandName.equals("shophelp")) {
            return bscommand.help(sender);
        } else if (commandName.equals("shopbuy")) {
            return bscommand.buy(sender, args);
        } else if (commandName.equals("shopsell")) {
            return bscommand.sell(sender, args);
        } else if (commandName.equals("shopadd")) {
            return bscommand.add(sender, args);
        } else if (commandName.equals("shopremove")) {
            return bscommand.remove(sender, args);
        } else if (commandName.equals("shopload")) {
            return bscommand.load(sender);
        } else if (commandName.equals("shopcheck")) {
            return bscommand.check(sender, args);
        }
        return false;
    }
    
    public static void Log(String txt) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    public static void Log(String txt, Object params) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, String txt) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
    }

    public static void Log(Level loglevel, String txt, Object params) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, String txt, Object[] params) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }
}
