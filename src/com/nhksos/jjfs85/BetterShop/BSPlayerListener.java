/**
 * Programmer: Jacob Scott
 * Program Name: BSPlayerListener
 * Description: interface for adding a sign interface to bettershop
 * Date: Apr 6, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * @author jacob
 */
public class BSPlayerListener extends PlayerListener {

    //public BSPlayerListener() { } // end default constructor
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.WALL_SIGN
                || event.getClickedBlock().getType() == Material.SIGN_POST)) {
            Sign clickedSign = (Sign) event.getClickedBlock();
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                // if sign is registered, update prices
                // else, (if has permission) add sign
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                System.out.println(clickedSign.getLine(2));
            }
        }
    }
} // end class BSPlayerListener

