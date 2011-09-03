/**
 * Programmer: Jacob Scott
 * Program Name: SpoutPopupListener
 * Description:
 * Date: Sep 1, 2011
 */
package com.nhksos.jjfs85.BetterShop;

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

