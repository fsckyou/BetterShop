package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.FTPErrorReporter;
import com.jascotty2.Item.ItemDB;
//import com.jascotty2.MinecraftIM.ChatMessageHandler;
import com.jascotty2.MinecraftIM.MinecraftIM;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Bank;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import me.taylorkelly.help.Help;
import org.bukkit.plugin.Plugin;

/*
 * BetterShop for Bukkit
 */
public class BetterShop extends JavaPlugin { // implements ChatMessageHandler

    public final static String lastUpdatedStr = "3/30/11 09:30 -0500"; // "MM/dd/yy HH:mm Z"
    public final static int lastUpdated_gracetime = 20; // how many minutes off before out of date
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String name = "BetterShop";
    // todo: make these private
    public static BSConfig config =  null;
    public static BSPriceList pricelist = null;
    public static BSItemStock stock = null;
    public static BSTransactionLog transactions = null;
    public static BSCommand bscommand = new BSCommand();
    static Permissions Permissions = null;
    static iConomy iConomy = null;
    static Bank iBank = null;
    private final Listener Listener = new Listener(this);
    //private static boolean isLoaded = true;
    public static PluginDescriptionFile pdfFile;// = this.getDescription();
    static MinecraftIM messenger = null;
    static String lastCommand = "";

    private class Listener extends ServerListener {

        BetterShop shop;

        public Listener(BetterShop plugin) {
            shop = plugin;
        }

        @Override
        public void onPluginEnabled(PluginEvent event) {
            //Log(event.getPlugin().getDescription().getName());
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                BetterShop.iConomy = (iConomy) event.getPlugin();
                iBank = iConomy.getBank();
                //config.currency = iBank.getCurrency();
                Log("Attached to iConomy.");
            } else if (event.getPlugin().getDescription().getName().equals("Permissions")) {
                BetterShop.Permissions = (Permissions) event.getPlugin();
                Log("Attached to Permissions or something close enough to it");
            } else if (event.getPlugin().getDescription().getName().equals("Help")) {
                shop.registerHelp();
            } else if (event.getPlugin().getDescription().getName().equals("MinecraftIM")) {
                messenger = (MinecraftIM) event.getPlugin();
                //messenger.registerMessageHandler(shop);
                Log("linked to MinecraftIM");
            }
        }
    }

    private void registerHelp() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Help");
        if (test != null) {
            Help helpPlugin = ((Help) test);
            helpPlugin.registerCommand("shoplist [page]", "List shop prices", this, !config.hideHelp, "BetterShop.user.list");
            helpPlugin.registerCommand("shopitems", "compact listing of items in shop", this, "BetterShop.user.list");
            helpPlugin.registerCommand("shopkits [page]", "show listing of kits in shop", this, "BetterShop.user.list");
            helpPlugin.registerCommand("shopbuy [item] <amount>", "Buy items from the shop", this, !config.hideHelp, "BetterShop.user.buy");
            helpPlugin.registerCommand("shopbuyall [item]", "Buy all that you can hold/afford", this, "BetterShop.user.buy");
            helpPlugin.registerCommand("shopbuystack [item] <amount>", "Buy stacks of items", this, "BetterShop.user.buy");
            helpPlugin.registerCommand("shopbuyagain", "repeat last purchase action", this, "BetterShop.user.buy");
            helpPlugin.registerCommand("shopsell [item] <amount>", "Sell items to the shop", this, !config.hideHelp, "BetterShop.user.sell");
            helpPlugin.registerCommand("shopsellstack [item] <amount>", "Sell stacks of items", this, "BetterShop.user.sell");
            helpPlugin.registerCommand("shopsellall <inv> <item..>", "Sell all of your items", this, "BetterShop.user.sell");
            helpPlugin.registerCommand("shopsellagain", "Repeat last sell action", this, "BetterShop.user.sell");
            helpPlugin.registerCommand("shopcheck [item]", "Check prices of item[s]", this, !config.hideHelp, "BetterShop.user.check");
            helpPlugin.registerCommand("shophelp [command]", "show help on commands", this, !config.hideHelp, "BetterShop.user.help");
            helpPlugin.registerCommand("shopadd [item] [$buy] <$sell>", "Add/Update an item", this, !config.hideHelp, "BetterShop.admin.add");
            helpPlugin.registerCommand("shopremove [item]", "Remove an item from the shop", this, !config.hideHelp, "BetterShop.admin.remove");
            helpPlugin.registerCommand("shopload", "Reload the Configuration & PriceList DB", this, !config.hideHelp, "BetterShop.admin.load");

            helpPlugin.registerCommand("shop restock", "manually restock (if enabled)", this, "BetterShop.admin.restock");
            helpPlugin.registerCommand("shop ver[sion]", "Show Version # and if is current", this, "BetterShop.admin.info");
            helpPlugin.registerCommand("shop backup", "backup current pricelist", this, "BetterShop.admin.backup");
            helpPlugin.registerCommand("shop import [file]", "import a file into the pricelist", this, "BetterShop.admin.backup");
            helpPlugin.registerCommand("shop restore [file]", "restore pricelist from backup", this, "BetterShop.admin.backup");


            Log("'Help' support enabled.");
        } //else Log("Help not yet found.");
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvent(
                Event.Type.PLUGIN_ENABLE, Listener, Priority.Monitor, this);
    }

    private void hookDepends() {
        Plugin test = getServer().getPluginManager().getPlugin("iConomy");
        if (test != null) {//this.getServer().getPluginManager().isPluginEnabled("iConomy")) {
            iConomy = (iConomy) test;//this.getServer().getPluginManager().getPlugin("iConomy");
            iBank = iConomy.getBank();
            //config.currency = iBank.getCurrency();
            Log("Attached to iConomy.");
        } else {
            Log(Level.WARNING, "iConomy not yet found...", false);
        }
        test = getServer().getPluginManager().getPlugin("Permissions");
        if (test != null) {//this.getServer().getPluginManager().isPluginEnabled("Permissions")) {
            Permissions = (Permissions) test;//this.getServer().getPluginManager().getPlugin("Permissions");
            Log("Attached to Permissions.");
        }
        test = getServer().getPluginManager().getPlugin("MinecraftIM");
        if (test != null) {//this.getServer().getPluginManager().isPluginEnabled("MinecraftIM")) {
            messenger = (MinecraftIM) test;//this.getServer().getPluginManager().getPlugin("MinecraftIM");
            Log("linked to MinecraftIM");
        }
        //Log(Level.INFO, "Permissions not yet found...");
    }

    public void onEnable() {
        pdfFile = this.getDescription();
        logger.log(Level.INFO, String.format("Loading %s version %s ...", pdfFile.getName(), pdfFile.getVersion()));

        // ready items db (needed for pricelist, sorting in config, item lookup, ...)
        try {
            ItemDB.load(BSConfig.pluginFolder);
            //Log("Itemsdb loaded");
        } catch (Exception e) {
            Log(Level.SEVERE, "cannot load items db: closing plugin", e, false);
            this.setEnabled(false);
            return;
        }

        if (config == null) {
            config = new BSConfig();
            //Log("config loaded");
            if (config.checkUpdates) {
                Updater.check();
            }
        } else {
            config.load();
        }
        pricelist = new BSPriceList();
        transactions = new BSTransactionLog();
        stock = new BSItemStock();

        if (!pricelist.load()) {
            Log(Level.SEVERE, "cannot load pricelist: " + pricelist.pricelistName(), false);
            // todo: add handlers for if not loaded?
            this.setEnabled(false);
            return;
        } else if (config.logUserTransactions && !transactions.load()) {
            Log(Level.SEVERE, "cannot load transaction log: " + transactions.databaseName(), false);
            //this.setEnabled(false);
            //return;
        } else if (config.useItemStock && !stock.load()) {
            Log(Level.SEVERE, "cannot load stock database", false);
            stock = null;
        }

        hookDepends();
        registerEvents();
        registerHelp();
        //isLoaded = true;

        // Just output some info so we can check all is well
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!",
                new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }

    public void onDisable() {
        // NOTE: All registered events are automatically unregistered when a
        // plugin is disabled
        try {
            pricelist.close();
        } catch (Exception ex) {
            Log(Level.SEVERE, ex, false);
        }

        transactions = null;
        stock = null;
        pricelist = null;
        messenger = null;
        //config = null;

        logger.info("BetterShop now unloaded");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        lastCommand = (sender instanceof Player ? "game:" : "console:")
                + commandName + " " + BSCommand.argStr(args);

        // i don't like seeing these messages all the time..
        //Log(((Player) sender).getName() + " used command " + command.getName());

        if ((BetterShop.iConomy == null)) {
            BSutils.sendMessage(sender, "\u00A74 BetterShop is missing a dependency. Check the console.");
            Log(Level.SEVERE, "Missing: iConomy", false);
            return true;
        }

        if (stock != null && config.useItemStock) {
            stock.checkStockRestock();
        }

        if (commandName.equals("shop")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("list")) {
                    commandName = "shoplist";
                } else if (args[0].equalsIgnoreCase("help")) {
                    commandName = "shophelp";
                } else if (args[0].equalsIgnoreCase("buy")) {
                    commandName = "shopbuy";
                } else if (args[0].equalsIgnoreCase("sell")) {
                    commandName = "shopsell";
                } else if (args[0].equalsIgnoreCase("add")) {
                    commandName = "shopadd";
                } else if (args[0].equalsIgnoreCase("remove")) {
                    commandName = "shopremove";
                } else if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("reload")) {
                    commandName = "shopload";
                } else if (args[0].equalsIgnoreCase("check")) {
                    commandName = "shopcheck";
                } else if (args[0].equalsIgnoreCase("sellall")) {
                    commandName = "shopsellall";
                } else if (args[0].equalsIgnoreCase("sellstack")) {
                    commandName = "shopsellstack";
                } else if (args[0].equalsIgnoreCase("buystack")) {
                    commandName = "shopbuystack";
                } else if (args[0].equalsIgnoreCase("buyall")) {
                    commandName = "shopbuyall";
                } else if (args[0].equalsIgnoreCase("sellagain")) {
                    commandName = "shopsellagain";
                } else if (args[0].equalsIgnoreCase("buyagain")) {
                    commandName = "shopbuyagain";
                } else if (args[0].equalsIgnoreCase("listkits")) {
                    commandName = "shoplistkits";
                } else if (args[0].equalsIgnoreCase("restock")) {
                    if (BSutils.hasPermission(sender, "BetterShop.admin.restock", true)) {
                        stock.Restock(true);
                        sender.sendMessage("Stock set to initial values");
                    }
                } else if (args[0].equalsIgnoreCase("backup")) {
                    if (BSutils.hasPermission(sender, "BetterShop.admin.backup", true)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("_yyyy_MM_dd_HH-mm-ss");
                        String backFname = BSConfig.pluginFolder.getPath() + File.separatorChar
                                + config.tableName + formatter.format(new java.util.Date()) + ".csv";
                        try {
                            if (pricelist.saveFile(new File(backFname))) {
                                sender.sendMessage("Backup saved as " + backFname);
                            }
                        } catch (IOException ex) {
                            Log(Level.SEVERE, "Failed to save backup file " + backFname, ex);
                            sender.sendMessage("\u00A74Failed to save backup file " + backFname);
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("import")) {
                    return bscommand.importDB(sender, args);
                } else if (args[0].equalsIgnoreCase("restore")) {
                    return bscommand.restoreDB(sender, args);
                } else if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
                    // allow admin.info or developers access to plugin status (so if i find a bug i can see if it's current)
                    if (BSutils.hasPermission(sender, "BetterShop.admin.info", false)
                            || (sender instanceof Player && (((Player) sender).getDisplayName().equals("jascotty2")
                            || ((Player) sender).getDisplayName().equals("jjfs85")))) {
                        BSutils.sendMessage(sender, "version " + pdfFile.getVersion());
                        if (Updater.isUpToDate()) {
                            BSutils.sendMessage(sender, "Version is up-to-date");
                        } else {
                            BSutils.sendMessage(sender, "Newer Version Avaliable");
                        }
                        return true;
                    }
                } else {
                    return false;
                }
                // now remove [0]
                if (args.length > 1) {
                    String newArgs[] = new String[args.length - 1];
                    for (int i = 1; i < args.length; ++i) {
                        newArgs[i - 1] = args[i];
                    }
                    args = newArgs;
                } else {
                    args = new String[0];
                }

            } else {
                return false;
            }
        }

        // check if using history
        if (commandName.equals("shopbuyagain") || commandName.equals("shopsellagain")) {
            if (args.length > 0 || BSutils.anonymousCheck(sender)) {
                return false;
            }
            if (commandName.equals("shopbuyagain")) {
                String action = bscommand.userbuyHistory.get(((Player) sender).getDisplayName());
                if (action == null) {
                    BSutils.sendMessage(sender, "You have no recent buying history");
                    return true;
                } else {
                    // trim command & put into args
                    String cm[] = action.split(" ");
                    commandName = cm[0];
                    args = new String[cm.length - 1];
                    for (int i = 1; i < cm.length; ++i) {
                        args[i - 1] = cm[i];
                    }
                }
            } else {// if (commandName.equals("shopsellagain")) {
                String action = bscommand.usersellHistory.get(((Player) sender).getDisplayName());
                if (action == null) {
                    BSutils.sendMessage(sender, "You have no recent sell history");
                    return true;
                } else {
                    // trim command & put into args
                    String cm[] = action.split(" ");
                    commandName = cm[0];
                    args = new String[cm.length - 1];
                    for (int i = 1; i < cm.length; ++i) {
                        args[i - 1] = cm[i];
                    }
                }
            }
            //System.out.println("new command: " + commandName);
            //System.out.println(BSCommand.argStr(args));
        }

        if (commandName.equals("shoplist")) {
            return bscommand.list(sender, args);
        } else if (commandName.equals("shopitems")) {
            return bscommand.listitems(sender, args);
        } else if (commandName.equals("shophelp")) {
            return bscommand.help(sender, args);
        } else if (commandName.equals("shopbuy")) {
            return bscommand.buy(sender, args);
        } else if (commandName.equals("shopbuyall")) {
            ArrayList<String> arg = new ArrayList<String>();
            arg.addAll(Arrays.asList(args));
            arg.add("all");
            return bscommand.buy(sender, arg.toArray(new String[0]));
        } else if (commandName.equals("shopbuystack")) {
            return bscommand.buystack(sender, args);
        } else if (commandName.equals("shopsell")) {
            return bscommand.sell(sender, args);
        } else if (commandName.equals("shopsellall")) {
            return bscommand.sellall(sender, args);
        } else if (commandName.equals("shopsellstack")) {
            return bscommand.sellstack(sender, args);
        } else if (commandName.equals("shopadd")) {
            return bscommand.add(sender, args);
        } else if (commandName.equals("shopremove")) {
            return bscommand.remove(sender, args);
        } else if (commandName.equals("shopload")) {
            return bscommand.load(sender);
        } else if (commandName.equals("shopcheck")) {
            return bscommand.check(sender, args);
        } else if (commandName.equals("shoplistkits")) {
            return bscommand.listkits(sender, args);
        }

        return false;
    }

    public static void Log(String txt) {
        if (messenger != null && config.sendAllLog) {
            messenger.sendNotify(String.format("[%s] %s", name, txt));
        }
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    public static void Log(String txt, Object params) {
        if (messenger != null && config.sendAllLog) {
            messenger.sendNotify(String.format("[%s] %s", name, txt));
        }
        logger.log(Level.INFO, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, String txt) {
        Log(loglevel, txt, true);
    }

    public static void Log(Level loglevel, String txt, boolean sendReport) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
        if (sendReport && config != null) {
            if (loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                sendErrorReport(txt, null);
            }
            if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                messenger.sendNotify(String.format("[%s] %s", name, txt == null ? "" : txt));
            }
        }
    }

    public static void Log(Level loglevel, String txt, Exception params) {
        Log(loglevel, txt, params, true);
    }

    public static void Log(Level loglevel, String txt, Exception params, boolean sendReport) {
        if (txt == null) {
            Log(loglevel, params);
        } else {
            logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), (Exception) params);
            if (sendReport && config != null) {
                if (loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                    sendErrorReport(txt, params);
                }
                if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                    messenger.sendNotify(String.format("[%s] %s%n%s%n%s", name, txt, params.getMessage(), getStackStr(params)));
                }
            }
        }
    }

    public static void Log(Level loglevel, Exception err) {
        Log(loglevel, err, true);
    }

    public static void Log(Level loglevel, Exception err, boolean sendReport) {
        logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
        if (sendReport && config != null) {
            if (loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                sendErrorReport(null, err);
            }
            if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                messenger.sendNotify(String.format("[%s] %s%n%s%n%s", name, err == null ? "? unknown exception ?" : err.getMessage(), getStackStr(err)));
            }
        }
    }

    public static String getStackStr(Exception err) {
        if (err == null) {
            return "";
        }
        String stack = "";
        StackTraceElement[] st = err.getCause().getStackTrace();
        for (StackTraceElement e : st) {
            stack += e.toString() + "\n";
        }
        return stack;
    }
    static Date sentErrors[] = new Date[5];
    static long minSendWait = 600; // min time before a send expires

    static void sendErrorReport(String txt, Exception err) {
        boolean allow = false;
        long now = (new Date()).getTime();
        for (int i = 0; i < sentErrors.length; ++i) {
            if (sentErrors[i] == null || (now - sentErrors[i].getTime()) / 1000 >= minSendWait) {
                sentErrors[i] = new Date();
                allow = true;
                break;
            }
        }
        if (allow) {
            int pcount = -1;
            if (pricelist != null) {
                try {
                    pcount = pricelist.getItems(true).length;
                } catch (Exception ex) {
                }
            }

            String fname = FTPErrorReporter.SendNewText(
                    "BetterShop Error Report at " + (new Date()).toString() + "\n"
                    + "Bukkit: " + Updater.getBukkitVersion() + "\n"
                    + "Version: " + pdfFile.getVersion() + "  (" + lastUpdatedStr + ")\n"
                    + "iConomy: " + (iConomy != null ? ((Plugin) iConomy).getDescription().getVersion() : "none") + "\n"
                    + "Permissions: " + (Permissions != null ? "true" : "false") + "\n"
                    + "Last executed command: " + lastCommand + "\n"
                    + (config != null ? config.condensedSettings() : "-") + "," + (pcount >= 0 ? pcount : "-") + "\n"
                    + "Message: " + (txt != null ? txt : "") + "\n"
                    + (err != null ? getStackStr(err) : "") + "\n");
            if (fname.length() > 0) {
                System.out.println("report sent. id: " + fname);
            }
        } //else {  System.out.println("sending too fast.."); }
    }
}
