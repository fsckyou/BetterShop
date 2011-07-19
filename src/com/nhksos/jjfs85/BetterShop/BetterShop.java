package com.nhksos.jjfs85.BetterShop;

import com.earth2me.essentials.Essentials;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.jascotty2.Item.CreatureItem.EntityListen;
import com.jascotty2.Item.JItemDB;
import com.jascotty2.util.Str;

// iConomy 4.x
//import com.nijiko.coelho.iConomy.iConomy;
//import com.nijiko.coelho.iConomy.system.Bank;
// iConomy 5.x
//import com.iConomy.iConomy;

import com.nijikokun.bukkit.Permissions.Permissions;
import me.taylorkelly.help.Help;
import com.jascotty2.MinecraftIM.MinecraftIM;
import com.jascotty2.bukkit.ServerInfo;
import cosine.boseconomy.BOSEconomy;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Server;
import org.bukkit.event.server.PluginDisableEvent;

/*
 * BetterShop for Bukkit
 */
public class BetterShop extends JavaPlugin {

    public final static String lastUpdatedStr = "07/03/11 12:15 -0500"; // "MM/dd/yy HH:mm Z"
    public final static int lastUpdated_gracetime = 20; // how many minutes off before out of date
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String name = "BetterShop";
    protected static BSConfig config = new BSConfig();
    protected static BSPriceList pricelist = null;
    protected static BSItemStock stock = null;
    protected static BSTransactionLog transactions = null;
    protected static BSCommand bscommand = new BSCommand();
    protected static BSSignShop signShop = null;
    protected static Permissions Permissions = null;
    protected static com.iConomy.iConomy iConomy = null;
    protected static com.nijiko.coelho.iConomy.iConomy legacyIConomy = null;
    //protected static boolean legacyIConomy = true;
    //protected static Bank iBank = null;
    protected static BOSEconomy economy = null;
    protected static Essentials essentials = null;
    private PluginListener pListener = null;
    //private static boolean isLoaded = true;
    public static PluginDescriptionFile pdfFile;// = this.getDescription();
    protected static MinecraftIM messenger = null;
    protected static String lastCommand = "";
    // for animal/monster purchases
    public final EntityListen entityListener = new EntityListen();

    private class PluginListener extends ServerListener {

        BetterShop shop;

        public PluginListener(BetterShop plugin) {
            shop = plugin;
        }

        @Override
        public void onPluginEnable(PluginEnableEvent event) {
            if (event.getPlugin().isEnabled()) { // double-checking enabled
                if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                    if (BetterShop.iConomy == null && BetterShop.legacyIConomy == null) {
                        try {
                            BetterShop.legacyIConomy = (com.nijiko.coelho.iConomy.iConomy) event.getPlugin();
                        } catch (NoClassDefFoundError e) {
                            BetterShop.iConomy = (com.iConomy.iConomy) event.getPlugin();
                        }
                        Log("Attached to iConomy.");
                    }
                    config.setCurrency();
                } else if (event.getPlugin().getDescription().getName().equals("BOSEconomy")) {
                    if (BetterShop.economy == null) {
                        BetterShop.economy = (BOSEconomy) event.getPlugin();
                        Log("Attached to BOSEconomy");
                    }
                    config.setCurrency();
                } else if (event.getPlugin().getDescription().getName().equals("Essentials")) {
                    if (BetterShop.essentials == null) {
                        BetterShop.essentials = (Essentials) event.getPlugin();
                        Log("Attached to Essentials");
                    }
                    if (BetterShop.iConomy == null && BetterShop.legacyIConomy == null) {
                        config.setCurrency();
                    }
                } else if (event.getPlugin().getDescription().getName().equals("Permissions")) {
                    if (BetterShop.Permissions == null) {
                        BetterShop.Permissions = (Permissions) event.getPlugin();
                        Log("Attached to Permissions or something close enough to it");
                    }
                } else if (event.getPlugin().getDescription().getName().equals("Help")) {
                    shop.registerHelp();
                } else if (event.getPlugin().getDescription().getName().equals("MinecraftIM")) {
                    if (BetterShop.messenger == null) {
                        messenger = (MinecraftIM) event.getPlugin();
                        Log("linked to MinecraftIM");
                    }
                }
            }
        }

        @Override
        public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                BetterShop.iConomy = null;
                BetterShop.legacyIConomy = null;
                Log(Level.WARNING, "iConomy has been disabled!");
            } else if (event.getPlugin().getDescription().getName().equals("BOSEconomy")) {
                BetterShop.economy = null;
                Log(Level.WARNING, "BOSEconomy has been disabled");
            } else if (event.getPlugin().getDescription().getName().equals("Permissions")) {
                BetterShop.Permissions = null;
                Log("Permissions support disabled");
            } else if (event.getPlugin().getDescription().getName().equals("Essentials")) {
                BetterShop.essentials = null;
                Log("Essentials support disabled");
            } else if (event.getPlugin().getDescription().getName().equals("MinecraftIM")) {
                messenger = null;
                Log("MinecraftIM link disabled");
            } else if (event.getPlugin().getDescription().getName().equals("Help")) {
                helpEnabled = false;
            }
        }
    }
    private boolean helpEnabled = false; //no reason to check twice :)

    private void registerHelp() {
        if (!helpEnabled) {
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
                helpPlugin.registerCommand("shop update", "manually update bettershop to newest version", this, "OP");

                helpEnabled = true;
                Log("'Help' support enabled.");
            } //else Log("Help not yet found.");
        }
    }

    private void hookDepends() {
        Plugin test = getServer().getPluginManager().getPlugin("iConomy");
        if (test != null) {
            try {
                legacyIConomy = (com.nijiko.coelho.iConomy.iConomy) test;
                if (com.nijiko.coelho.iConomy.iConomy.getBukkitServer() != null) {
                    config.setCurrency();
                }
            } catch (NoClassDefFoundError e) {
                iConomy = (com.iConomy.iConomy) test;
                if (iConomy.getBukkitServer() != null) {
                    config.setCurrency();
                }
            }
            Log("Attached to iConomy.");
        } else {
            test = getServer().getPluginManager().getPlugin("BOSEconomy");
            if (test != null) {
                economy = (BOSEconomy) test;
                if (economy.getAccountManager() != null) {
                    config.setCurrency();
                }
                Log("Attached to BOSEconomy");
            } else {
                test = getServer().getPluginManager().getPlugin("Essentials");
                if (test != null) {
                    essentials = (Essentials) test;
                    if (Essentials.getStatic() != null) {
                        config.setCurrency();
                    }
                    Log("Attached to Essentials");
                } else {
                    Log(Level.WARNING, "economy plugin not yet found...", false);
                }
            }
        }
        test = getServer().getPluginManager().getPlugin("Permissions");
        if (test != null) {
            Permissions = (Permissions) test;
            Log("Attached to Permissions.");
        }
        test = getServer().getPluginManager().getPlugin("MinecraftIM");
        if (test != null) {
            messenger = (MinecraftIM) test;
            Log("linked to MinecraftIM");
        }
    }

    public void onEnable() {
        pdfFile = this.getDescription();
        logger.log(Level.INFO, String.format("Loading %s version %s ...", pdfFile.getName(), pdfFile.getVersion()));
        config.extractDefaults();
        // ready items db (needed for pricelist, sorting in config, item lookup, ...)
        try {
            JItemDB.load(BSConfig.itemDBFile);
            //Log("Itemsdb loaded");
        } catch (Exception e) {
            Log(Level.SEVERE, "cannot load items db: closing plugin", e, false);
            this.setEnabled(false);
            return;
        }
        config.load();
        if (config.checkUpdates) {
            if (config.autoUpdate) {
                Log("Checking for updates...");
                if (!Updater.isUpToDate(true)) {
                    Log("Downloading & Installing Update");
                    ServerReload sreload = new ServerReload(getServer());
                    if (Updater.downloadUpdate()) {
                        Log("Update Downloaded: Restarting Server..");
                        //this.setEnabled(false);
                        //this.getServer().dispatchCommand((CommandSender) new CommanderSenderImpl(this), "stop");
                        //this.getServer().dispatchCommand(new AdminCommandSender(this), "stop");

                        try {
                            //(new ServerReload(getServer())).start(500);
                            sreload.start(500);
                        } catch (Exception e) { // just in case...
                            this.getServer().reload();
                        }
                        return;
                    }
                }
            } else {
                Updater.check();
            }
        }
        pricelist = new BSPriceList();
        transactions = new BSTransactionLog();
        stock = new BSItemStock();
        signShop = new BSSignShop(this);

        if (!pricelist.load()) {
            Log(Level.SEVERE, "cannot load pricelist: " + pricelist.pricelistName(), false);
            // todo: add handlers for if not loaded?
            this.setEnabled(false);
            return;
        } else if (!transactions.load()) {
            Log(Level.SEVERE, "cannot load transaction log", false);
            //this.setEnabled(false);
            //return;
        }
        if (config.useItemStock && !stock.load()) {
            Log(Level.SEVERE, "cannot load stock database", false);
            stock = null;
        }
        if (config.signShopEnabled && !signShop.load()) {
            Log(Level.SEVERE, "cannot load sign shop database", false);

        }
        if (config.signShopEnabled && config.tntSignDestroyProtection) {
            signShop.startProtecting();
        }

        pListener = new PluginListener(this);

        hookDepends();
        registerHelp();
        //isLoaded = true;

        // for monster purchasing
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Event.Priority.Normal, this);
        // for sign events
        pm.registerEvent(Event.Type.PLAYER_INTERACT, signShop, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, signShop.signDestroy, Event.Priority.Normal, this);
        //pm.registerEvent(Event.Type.BLOCK_DAMAGE, signShop.signDestroy, Priority.Normal, this);
        //pm.registerEvent(Event.Type.BLOCK_CANBUILD, signShop.buildStopper, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, signShop.signDestroy, Event.Priority.Normal, this);

        // monitor plugins - if any are enabled/disabled by a plugin manager
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pListener, Event.Priority.Monitor, this);

        // Just output some info so we can check all is well
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!",
                new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }

    public void onDisable() {
        // NOTE: All registered events are automatically unregistered when a plugin is disabled
        lastCommand = "(disabling)";
        config.closeCommandLog();

        if (pricelist != null) {
            try {
                pricelist.close();
            } catch (Exception ex) {
                Log(Level.SEVERE, ex, false);
            }
        }
        if (signShop != null) {
            signShop.save();
            signShop.stopProtecting();
        }

        transactions = null;
        stock = null;
        pricelist = null;
        messenger = null;
        //config = null;
        signShop = null;

        logger.info("BetterShop now unloaded");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase(),
                argStr = Str.argStr(args);

        if (config.logCommands) {
            config.logCommand(sender instanceof Player ? ((Player) sender).getName() : "(console)",
                    commandLabel + " " + argStr);
        }

        try {
            lastCommand = (sender instanceof Player ? "player:" : "console:")
                    + commandName + " " + argStr;

            if (iConomy == null && legacyIConomy == null && economy == null && essentials == null
                    && (commandName.contains("buy") || commandName.contains("sell")
                    || argStr.contains("buy") || argStr.contains("sell"))) {
                BSutils.sendMessage(sender, "\u00A74 BetterShop is missing a dependency. Check the console.");
                Log(Level.SEVERE, "Missing: iConomy or BOSEconomy", false);
                return true;
            }

            if (stock != null && config.useItemStock) {
                stock.checkStockRestock();
            }

            if (commandName.equals("shop")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("list")) {
                        commandName = "shoplist";
                    } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                        commandName = "shophelp";
                    } else if (args[0].equalsIgnoreCase("alias") || args[0].equalsIgnoreCase("a")) {
                        commandName = "shopalias";
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
                        if (BSutils.hasPermission(sender, BSutils.BetterShopPermission.ADMIN_RESTOCK, true)) {
                            stock.Restock(true);
                            sender.sendMessage("Stock set to initial values");
                        }
                    } else if (args[0].equalsIgnoreCase("backup")) {
                        if (BSutils.hasPermission(sender, BSutils.BetterShopPermission.ADMIN_BACKUP, true)) {
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
                    } else if (args[0].equalsIgnoreCase("update")) {
                        if (sender.isOp()) {
                            Log("Downloading & Installing Update");
                            BSutils.sendMessage(sender, "Downloading & Installing Update");
                            ServerReload sreload = new ServerReload(getServer());
                            if (Updater.downloadUpdate()) {
                                Log("Update Downloaded: Restarting Server..");
                                BSutils.sendMessage(sender, "Download Successful.. reloading server");
                                //this.setEnabled(false);
                                //this.getServer().dispatchCommand((CommandSender) new CommanderSenderImpl(this), "stop");
                                //this.getServer().dispatchCommand(new AdminCommandSender(this), "stop");

                                //this.getServer().reload();
                                sreload.start(500);
                            }
                        } else {
                            BSutils.sendMessage(sender, "Only an OP can update the shop plugin");
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
                        // allow admin.info or developers access to plugin status (so if i find a bug i can see if it's current)
                        if (BSutils.hasPermission(sender, BSutils.BetterShopPermission.ADMIN_INFO, false)
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
            if (!config.useGlobalCommandShop()
                    && Str.isIn(commandName, new String[]{
                        "shopbuy", "shopbuyall", "shopbuystack",
                        "shopsell", "shopsellall", "shopsellstack", /*"shoplist", "shopitems", "shopcheck", "shoplistkits",
                    "shopadd", "shopremove"*/})) {

                BSutils.sendMessage(sender, "Shop is disabled from here");
                return true;
            }

            if (commandName.equals("shoplist")) {
                return bscommand.list(sender, args);
            } else if (commandName.equals("shopitems")) {
                return bscommand.listitems(sender, args);
            } else if (commandName.equals("shophelp")) {
                return bscommand.help(sender, args);
            } else if (commandName.equals("shopalias")) {
                return bscommand.listAlias(sender, args);
            } else if (commandName.equals("shopbuy")) {
//                if (args.length > 0 && args[0].equalsIgnoreCase("rain")) {
//                    ((Player) sender).getWorld().setStorm(true);
//                } else if (args.length > 0 && args[0].equalsIgnoreCase("thunder")) {
//                    ((Player) sender).getWorld().setThundering(true);
//                } else if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
//                    ((Player) sender).getWorld().setStorm(false);
//                    ((Player) sender).getWorld().setThundering(false);
//                } else if (args.length > 0 && args[0].equalsIgnoreCase("lightning")) {
//                    ((Player) sender).getWorld().strikeLightning(((Player) sender).getCompassTarget());
//                } else {
                    return bscommand.buy(sender, args);
//                }
//                return true;
            } else if (commandName.equals("shopbuyall")) {
                ArrayList<String> arg = new ArrayList<String>();
                arg.add("all");
                arg.addAll(Arrays.asList(args));
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
        } catch (Exception e) {
            BSutils.sendMessage(sender, "Unexpected Error!");
            Log(Level.SEVERE, e);
        }
        return true;
    }

    protected static void Log(String txt) {
        if (messenger != null && config.sendAllLog) {
            messenger.sendNotify(String.format("[%s] %s", name, txt));
        }
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    protected static void Log(String txt, Object params) {
        if (messenger != null && config.sendAllLog) {
            messenger.sendNotify(String.format("[%s] %s", name, txt));
        }
        logger.log(Level.INFO, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    protected static void Log(Level loglevel, String txt) {
        Log(loglevel, txt, true);
    }

    protected static void Log(Level loglevel, String txt, boolean sendReport) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
        if (config != null) {
            if (sendReport && loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                sendErrorReport(txt, null);
            }
            if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                messenger.sendNotify(String.format("[%s] %s", name, txt == null ? "" : txt));
            }
        }
    }

    protected static void Log(Level loglevel, String txt, Exception params) {
        Log(loglevel, txt, params, true);
    }

    protected static void Log(Level loglevel, String txt, Exception params, boolean sendReport) {
        if (txt == null) {
            Log(loglevel, params);
        } else {
            logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), (Exception) params);
            if (config != null) {
                if (sendReport && loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                    sendErrorReport(txt, params);
                }
                if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                    messenger.sendNotify(String.format("[%s] %s%n%s", name, txt, params.getMessage(), Str.getStackStr(params)));
                }
            }
        }
    }

    protected static void Log(Level loglevel, Exception err) {
        Log(loglevel, err, true);
    }

    protected static void Log(Level loglevel, Exception err, boolean sendReport) {
        logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
        if (config != null) {
            if (sendReport && loglevel.intValue() > Level.WARNING.intValue() && config.sendErrorReports) {
                sendErrorReport(null, err);
            }
            if (messenger != null && loglevel.intValue() > Level.INFO.intValue() && config.sendLogOnError) {
                messenger.sendNotify(String.format("[%s] %s%n%s", name, err == null ? "? unknown exception ?" : err.getMessage(), Str.getStackStr(err)));
            }
        }
    }
    static Date sentErrors[] = new Date[3];
    static final long minSendWait = 3600; // min time before a send expires (seconds)

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
                    + "SUID: " + ServerInfo.serverUID(config != null ? !config.unMaskErrorID : true, BSConfig.MAX_CUSTMSG_LEN) + "\n"
                    + (config != null ? (config.customErrorMessage.length() > 0 ? config.customErrorMessage + "\n" : "") : "")
                    + "Machine: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") /* + "," + System.getProperty("user.dir")*/ + "\n"
                    + "Bukkit: " + ServerInfo.getBukkitVersion(true) + "\n"
                    + "Version: " + pdfFile.getVersion() + "  (" + lastUpdatedStr + ")\n"
                    + "Econ: " + econPlugin() + "\n"
                    + "Permissions: " + (Permissions != null ? "true" : "false") + "\n"
                    + "Last executed command: " + lastCommand + "\n"
                    + (config != null ? config.condensedSettings() : "-") + "," + (pcount >= 0 ? pcount : "-") + "\n"
                    + "Message: " + (txt != null ? txt : err.getMessage() != null && err.getMessage().length() > 0 ? err.getMessage() : "") + "\n"
                    + (err.getLocalizedMessage() != null && err.getLocalizedMessage().length() > 0
                    && (err.getMessage() == null || !err.getMessage().equals(err.getLocalizedMessage())) ? err.getLocalizedMessage() + "\n" : "")
                    + Str.getStackStr(err) + "\n");
            if (fname != null && fname.length() > 0) {
                System.out.println("report sent. id: " + fname);
            } else {
                System.out.println("Error report unable to send.. is the server online & BetterShop up-to-date?");
                System.out.println("(if yes, then the error tracker is likely temporarily offline)");
            }
        } //else {  System.out.println("sending too fast.."); }
    }

    static String econPlugin() {
        if (iConomy != null) {
            return "iConomy " + ((Plugin) iConomy).getDescription().getVersion();
        } else if (legacyIConomy != null) {
            return "iConomy " + ((Plugin) legacyIConomy).getDescription().getVersion();
        } else if (economy != null) {
            return "BOSEconomy " + ((Plugin) economy).getDescription().getVersion();
        } else if (essentials != null) {
            return "Essentials " + ((Plugin) essentials).getDescription().getVersion();
        } else {
            return "none";
        }
    }

    protected class ServerReload extends TimerTask {

        Server reload = null;

        public ServerReload(Server s) {
            reload = s;
        }

        public void start(long wait) {
            (new Timer()).schedule(this, wait);
        }

        @Override
        public void run() {
            if (reload != null) {
                reload.reload();
            }
        }
    }
    /*
    private static class AdminCommandSender implements CommandSender {

    Plugin pl;

    public AdminCommandSender(Plugin here) {
    pl = here;
    }

    public boolean isOp() {
    return true;
    }

    public void sendMessage(String string) {
    pl.getServer().getLogger().info(string);
    }

    public Server getServer() {
    return pl.getServer();
    }
    }//*/
}
