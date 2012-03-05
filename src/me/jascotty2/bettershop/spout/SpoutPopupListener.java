/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for handling spout gui events
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

import me.jascotty2.bettershop.utils.BetterShopLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.SliderDragEvent;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;

public class SpoutPopupListener implements Listener {

	private boolean buttonError = false,
			sliderError = false,
			textError = false;
	
	@EventHandler
	public void onButtonClick(ButtonClickEvent event) {
		try{
			SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
			if (d != null && event.getButton().isEnabled()) {
				d.buttonPress(event.getButton());
			}
		} catch (Exception e) {
			BetterShopLogger.Severe("Unexpected error in PopupListener", e, !buttonError);
			buttonError = true;
		}
	}

	@EventHandler
	public void onSliderDrag(SliderDragEvent event) {
		try {
			SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
			if (d != null) {
				d.sliderChanged(event.getSlider());
			}
		} catch (Exception e) {
			BetterShopLogger.Severe("Unexpected error in PopupListener", e, !sliderError);
			sliderError = true;
		}
	}
	
	@EventHandler
	public void onTextFieldChange(TextFieldChangeEvent event){
		try {
			SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
			if (d != null) {
				d.textChanged(event);
			}
		} catch (Exception e) {
			BetterShopLogger.Severe("Unexpected error in PopupListener", e, !textError);
			textError = true;
		}
	}
} // end class SpoutPopupListener

