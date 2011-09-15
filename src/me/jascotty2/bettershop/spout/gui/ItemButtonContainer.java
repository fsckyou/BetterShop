/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: ( TODO )
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

import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericItemWidget;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;

public abstract class ItemButtonContainer extends GenericContainer {

	final GenericButton marketButton = new GenericButton();
	final GenericItemWidget picItem = new GenericItemWidget();
	final int itemId;
	final byte itemData;
	final JItem item;
	final String itemName;
	
	public ItemButtonContainer(int id, byte dat){
		itemId = id;
		itemData = dat;
		item = JItemDB.GetItem(id, dat);
		marketButton.setPriority(RenderPriority.High);
		if (item != null && item.IsValidItem()) {
			picItem.setTypeId(id).setData(dat);
		} else {
			picItem.setTypeId(0);
		}
		itemName = item != null ? item.Name() : String.valueOf(id) + (dat != 0 ? ":" + dat : "");
		this.children.add(marketButton);
		this.children.add(picItem);
//		for (Widget child : children) {
//			child.setContainer(this);
//		}
	}

	public GenericButton getButton(){
		return marketButton;
	}

	public int getID(){
		return itemId;
	}

	public byte getData(){
		return itemData;
	}

	public Widget setEnabled(boolean enable) {
		if (super.isVisible() != enable) {
			marketButton.setEnabled(enable);
			marketButton.setDirty(true);
			//((Widget)marketButton).setPriority(enable ? RenderPriority.Low : RenderPriority.High);
			super.setDirty(true);
			return super.setVisible(enable);
		} else {
			return this;
		}
	}
} // end class ItemButton