/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: small button menu, with the icon overlaying the button
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

package me.jascotty2.bettershop.spout.gui;

public class SmallMarketMenuItem extends ItemButtonContainer {

	public static int DEF_WIDTH = 20, DEF_HEIGHT = 20;

	public SmallMarketMenuItem(int id) {
		this(id, (byte) 0);
	}

	public SmallMarketMenuItem(int id, byte dat) {
		super(id, dat);
		this.setWidth(DEF_WIDTH).setHeight(DEF_HEIGHT);//.setFixed(true).setAnchor(WidgetAnchor.CENTER_LEFT);
		marketButton.setHeight(height).setWidth(width);
		marketButton.setTooltip(itemName);
		picItem.setWidth(8).setHeight(8).setX(2).setY(2);
	}

} // end class SmallMarketMenuItem
