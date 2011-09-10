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

import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;
import org.getspout.spoutapi.event.screen.SliderDragEvent;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;

/**
 * @author jacob
 */
public class SpoutPopupListener extends ScreenListener {

	public SpoutPopupListener() {
	} // end default constructor

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
		if (d != null) {
			d.buttonPress(event.getButton());
		}
	}

	@Override
	public void onSliderDrag(SliderDragEvent event) {
		SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
		if (d != null) {
			d.sliderChanged(event.getSlider());
		}
	}
	
	@Override
	public void onTextFieldChange(TextFieldChangeEvent event){
		SpoutPopupDisplay d = SpoutPopupDisplay.getPopup(event.getPlayer());
		if (d != null) {
			d.textChanged(event);
		}
	}
} // end class SpoutPopupListener

