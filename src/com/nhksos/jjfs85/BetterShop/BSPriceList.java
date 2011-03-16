package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.CheckInput;
import com.jascotty2.Item.Item;
import com.jascotty2.PriceList;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.logging.Level;

public class BSPriceList extends PriceList {

    public BSPriceList() {
        // load the pricelist.
        sortOrder = BetterShop.config.sortOrder;
        // load();
    }

    public final boolean load() {
        if (BetterShop.config.useMySQL()) {
            try {
                //System.out.println("attempting MySQL");
                if (loadMySQL(BetterShop.config.sql_database,
                        BetterShop.config.tableName,
                        BetterShop.config.sql_username,
                        BetterShop.config.sql_password,
                        BetterShop.config.sql_hostName,
                        BetterShop.config.sql_portNum)) {
                    BetterShop.Log(Level.INFO, "MySQL database " + pricelistName() + " loaded.");
                    return true;
                }
            } catch (SQLException ex) {
                BetterShop.Log(Level.SEVERE, ex);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BetterShop.Log(Level.SEVERE, "Failed to connedt to MySQL database " +
                    BetterShop.config.sql_database);
            
        } else {
            try {
                //System.out.println("attempting FlatFile: " + BSConfig.pluginFolder.getPath() + File.separatorChar + BetterShop.config.tableName + ".csv");
                if (loadFile(new File(BSConfig.pluginFolder.getPath() + File.separatorChar +
                        BetterShop.config.tableName + ".csv"))) {
                    BetterShop.Log(Level.INFO, BetterShop.config.tableName + ".csv loaded.");
                    return true;
                }
            } catch (IOException ex) {
                BetterShop.Log(Level.SEVERE, ex);
            } catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, ex);
            }
            BetterShop.Log(Level.SEVERE, "Failed to load pricelist database " + BetterShop.config.tableName + ".csv " +
                    BetterShop.config.sql_database);
        }
        return false;
    }

    public boolean setPrice(String item, String b, String s) {
        try {
            double bp = CheckInput.GetDouble(b, -1);
            double sp = CheckInput.GetDouble(s, -1);
            return setPrice(Item.findItem(item), bp < 0 ? -1 : bp, sp < 0 ? -1 : sp);
        } catch (SQLException ex) {
            BetterShop.Log(Level.SEVERE, ex);
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        return false;
    }

    public LinkedList<String> GetShopListPage(int pageNum, boolean isPlayer, boolean showIllegal) {
        try {
            return GetShopListPage(pageNum, isPlayer,
                    BetterShop.config.pagesize,
                    BetterShop.config.getString("listing"),
                    BetterShop.config.getString("listhead"),
                    BetterShop.config.getString("listtail"), showIllegal);
        } catch (SQLException ex) {
            BetterShop.Log(Level.SEVERE, ex);
        } catch (Exception ex) {
            BetterShop.Log(Level.SEVERE, ex);
        }
        // an error occured: let player know
        LinkedList<String> ret = new LinkedList<String>();
        ret.add("\u00A74An Error Occurred while retrieving pricelist.. ");
        ret.add("\u00A74 see the server log or let an OP know of this error");
        return ret;
    }

    public LinkedList<String> GetShopListPage(int pageNum, boolean isPlayer) {
        return GetShopListPage(pageNum, isPlayer, true);
    }
}
