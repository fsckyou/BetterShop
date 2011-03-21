package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Item.ItemDB;
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

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Bank;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/*
 * BetterShop for Bukkit
 * 
 * @author jjfs85
 */
public class BetterShop extends JavaPlugin {
    public final static String lastUpdatedStr = "3/20/11 23:00 -0500"; // "MM/dd/yy HH:mm Z"
    public final static int lastUpdated_gracetime = 20; // how many minutes off before out of date
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String name = "BetterShop";
    public static BSConfig config = null;//final new BSConfig();
    public static BSPriceList pricelist = null;//new BSPriceList();
    public static BSTransactionLog transactions = null;//new BSTransactionLog();
    public static BSCommand bscommand = null;//new BSCommand();
    static Permissions Permissions = null;
    static iConomy iConomy = null;
    static Bank iBank = null;
    private final Listener Listener = new Listener();
    //private static boolean isLoaded = true;
    public static PluginDescriptionFile pdfFile;// = this.getDescription();

    private class Listener extends ServerListener {

        public Listener() {
        }

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

    private void hookDepends() {
        if (this.getServer().getPluginManager().isPluginEnabled("iConomy")) {
            iConomy = (iConomy) this.getServer().getPluginManager().getPlugin("iConomy");
            iBank = iConomy.getBank();
            config.currency = iBank.getCurrency();
            Log("Attached to iConomy.");
        } else {
            Log(Level.WARNING, "iConomy not yet found...");
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Permissions")) {
            Permissions = (Permissions) this.getServer().getPluginManager().getPlugin("Permissions");
            Log("Attached to Permissions.");
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
            Log(Level.SEVERE, e);
            Log(Level.SEVERE, "cannot load items db: closing plugin");
            this.setEnabled(false);
            return;
        }

        if (config == null) {
            config = new BSConfig();
            //Log("config loaded");
            if(config.checkUpdates){
                Updater.check();
            }
            pricelist = new BSPriceList();
            transactions = new BSTransactionLog();
            bscommand = new BSCommand();
        } else {
            config.load();
            pricelist = new BSPriceList();
            transactions = new BSTransactionLog();
        }

        if (!pricelist.load()) {
            Log(Level.SEVERE, "cannot load pricelist: " + pricelist.pricelistName());
            this.setEnabled(false);
            return;
        } else if (config.logUserTransactions && !transactions.load()) {
            Log(Level.SEVERE, "cannot load transaction log: " + transactions.databaseName());
            //this.setEnabled(false);
            //return;
        }

        hookDepends();
        registerEvents();
        //isLoaded = true;

        // Just output some info so we can check
        // all is well
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!",
                new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }

    public void onDisable() {
        try {
            // NOTE: All registered events are automatically unregistered when a
            // plugin is disabled
            pricelist.close();
        } catch (IOException ex) {
            Log(Level.SEVERE, ex);
        } catch (Exception ex) {
            Log(Level.SEVERE, ex);
        }
        //isLoaded = false;
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
            BSutils.sendMessage(sender, "\u00A74 BetterShop is missing a dependency. Check the console.");
            Log(Level.SEVERE, "Missing: iConomy");
            return true;
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
                } else if (args[0].equalsIgnoreCase("backup")) {
                    if (BSutils.hasPermission(sender, "BetterShop.admin.load", true)){
                        SimpleDateFormat formatter = new SimpleDateFormat("_yyyy_MM_dd_HH-mm-ss");
                        String backFname = BSConfig.pluginFolder.getPath() + File.separatorChar + 
                                config.tableName + formatter.format(new java.util.Date()) + ".csv";
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
                } else if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
                    // allow admin.info or developers access to plugin status (so if i find a bug i can see if it's current)
                    if (BSutils.hasPermission(sender, "BetterShop.admin.info", false)
                            || (sender instanceof Player && (((Player) sender).getDisplayName().equals("jascotty2")
                            || ((Player) sender).getDisplayName().equals("jjfs85")))) {
                        BSutils.sendMessage(sender, "version " + pdfFile.getVersion());
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
                    BSutils.sendMessage(sender, "You have no recent sell history");
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
            return bscommand.help(sender);
        } else if (commandName.equals("shopbuy")) {
            return bscommand.buy(sender, args);
        }  else if (commandName.equals("shopbuyall")) {
            ArrayList<String> arg = new ArrayList<String>();
            arg.addAll(Arrays.asList(args));
            arg.add("all");
            return bscommand.buy(sender, arg.toArray(new String[0]));
        }else if (commandName.equals("shopbuystack")) {
            return bscommand.buystack(sender, args);
        } else if (commandName.equals("shopsell")) {
            return bscommand.sell(sender, args);
        } else if (commandName.equals("shopsellall")) {
            return bscommand.sellall(sender, args);
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

    public static void Log(Level loglevel, String txt, Exception params) {
        if (txt == null) {
            Log(loglevel, params);
        } else {
            logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), (Exception) params);
        }
    }

    public static void Log(Level loglevel, String txt, Object[] params) {
        logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
    }

    public static void Log(Level loglevel, Exception err) {
        logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
    }
}
