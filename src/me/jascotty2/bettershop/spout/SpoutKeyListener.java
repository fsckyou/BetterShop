/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: for opening & closing shop menu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.bettershop.spout;

import me.jascotty2.lib.io.CheckInput;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.enums.BetterShopPermission;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import org.getspout.spoutapi.event.input.InputListener;
import org.getspout.spoutapi.event.input.KeyPressedEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.keyboard.Keyboard;

/**
 * @author jacob
 */
public class SpoutKeyListener implements Listener {

	static HashMap<String, String> keys = new HashMap<String, String>();

	{
		keys.put("'", "APOSTROPHE");
		keys.put("+", "ADD");
		keys.put("@", "AT");
		keys.put("\\", "BACKSLASH");
		keys.put(":", "COLON");
		keys.put(",", "COMMA");
		keys.put("/", "SLASH");
		keys.put("=", "EQUALS");
		keys.put("`", "GRAVE");
		keys.put("[", "LBRACKET");
		keys.put("-", "MINUS");
		keys.put("*", "MULTIPLY");
		keys.put(".", "PERIOD");
		keys.put("^", "POWER");
		keys.put("]", "RBRACKET");
		keys.put(";", "SEMICOLON");
		keys.put(" ", "SPACE");
		keys.put("-", "SUBTRACT");
		keys.put("_", "UNDERLINE");
		keys.put("\t", "TAB");
	}
	Keyboard listenKey = null;
	boolean keyPressError = false;

	public SpoutKeyListener() {
		reloadKey();
	} // end default constructor

	public final void reloadKey() {
		String k = BetterShop.getSettings().getSpoutKey().toUpperCase();
		try {
			if (CheckInput.IsInt(k)) {
				listenKey = Keyboard.getKey(CheckInput.GetInt(k, -1));
			} else {
				k = k.replace("KEY_", "");
				if (k.length() > 1 || Character.isLetterOrDigit(k.charAt(0))) {
					for (Entry<String, String> ky : keys.entrySet()) {
						if (k.equals(ky.getKey())) {
							k = ky.getValue();
							break;
						}
					}
				}
				listenKey = Keyboard.valueOf("KEY_" + k);
			}
		} catch (Exception e) {
			BetterShopLogger.Log(Level.WARNING, "Invalid Key in Spout Config: defaulting to 'B'");
			listenKey = Keyboard.KEY_B;
		}
	}

	@EventHandler
	public void onKeyPressedEvent(KeyPressedEvent event) {
		if (!BetterShop.getSettings().spoutEnabled) {
			return;
		}
		try {
			if (event.getKey() == Keyboard.KEY_ESCAPE) {
				SpoutPopupDisplay.closePopup(event.getPlayer());
			} else if (event.getKey() == listenKey) {
				if (event.getScreenType() == ScreenType.GAME_SCREEN) {
					if (BSPermissions.hasPermission(event.getPlayer(), BetterShopPermission.USER_SPOUT, true)) {
						if (BetterShop.commandShopEnabled(event.getPlayer().getLocation())) {
							SpoutPopupDisplay.popup(event.getPlayer());
						} else {
							BSutils.sendMessage(event.getPlayer(),
									BetterShop.getSettings().getString("regionShopDisabled"));
						}
					}
				}
			}
//			else if(event.getKey() == Keyboard.KEY_P) { SpoutPopupDisplay.testScreen(event.getPlayer()); }
		} catch (Exception e) {
			BetterShopLogger.Severe("Unexpected error in KeyListener", e, !keyPressError);
			keyPressError = true;
		}
	}
} // end class SpoutKeyListener

