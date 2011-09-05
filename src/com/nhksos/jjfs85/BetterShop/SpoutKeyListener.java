/**
 * Programmer: Jacob Scott
 * Program Name: SpoutKeyListener
 * Description: for opening & closing shop menu
 * Date: Sep 1, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.io.CheckInput;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.getspout.spoutapi.event.input.InputListener;
import org.getspout.spoutapi.event.input.KeyPressedEvent;
import org.getspout.spoutapi.keyboard.Keyboard;

/**
 * @author jacob
 */
public class SpoutKeyListener extends InputListener {

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

	public SpoutKeyListener() {
		reloadKey();
	} // end default constructor

	public final void reloadKey() {
		String k = BetterShop.config.spoutKey.toUpperCase();
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
			BetterShop.Log(Level.WARNING, "Invalid Key in Spout Config: defaulting to 'B'");
		}
	}

	@Override
	public void onKeyPressedEvent(KeyPressedEvent event) {
		if (!BetterShop.config.spoutEnabled) {
			return;
		} else if (event.getKey() == Keyboard.KEY_ESCAPE) {
			SpoutPopupDisplay.closePopup(event.getPlayer());
		} else if (event.getKey() == listenKey) {
			if (BSutils.hasPermission(event.getPlayer(), BSutils.BetterShopPermission.USER_SPOUT, true)) {
				SpoutPopupDisplay.popup(event.getPlayer(), event.getScreenType());
			}
		}
	}
} // end class SpoutKeyListener

