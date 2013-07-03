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
package me.jascotty2.bettershop.chestshop;

import java.util.List;
import net.minecraft.server.v1_6_R1.EntityHuman;
import net.minecraft.server.v1_6_R1.IInventory;
import net.minecraft.server.v1_6_R1.ItemStack;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventorySmallChest implements IInventory {
	ItemStack items[];String name;
	public InventorySmallChest(String name, org.bukkit.inventory.ItemStack itms[]) {
		this.name=name;
		items = new ItemStack[itms.length];
		for(int i=0; i<itms.length; ++i){
			items[i] = itms[i] == null ? null : new ItemStack(
					itms[i].getTypeId(), itms[i].getAmount(), itms[i].getDurability());
					/*,(byte)(itms[i].getData() == null ? 0 : itms[i].getData().getData())*/
		}
	}

	public int getSize() {
		return items.length;
	}

	public ItemStack getItem(int i) {
		return i >= items.length ? null : items[i];
	}

	public ItemStack splitStack(int i, int j) {
        if (i >= this.items.length) {
			return null;
        }
		ItemStack[] aitemstack = this.items;

        if (aitemstack[i] != null) {
            ItemStack itemstack;

            if (aitemstack[i].count <= j) {
                itemstack = aitemstack[i];
                aitemstack[i] = null;
                return itemstack;
            } else {
                itemstack = aitemstack[i].a(j);
                if (aitemstack[i].count == 0) {
                    aitemstack[i] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
	}

	public void setItem(int i, ItemStack is) {
		items[i] = is;
	}

	public String getName() {
		return name;
	}

	public int getMaxStackSize() {
		return 64;
	}

	public void update() {
	}

	public ItemStack[] getContents() {
		return items;
	}

	public boolean a(EntityHuman eh) {
		return true;
	}
	
	public boolean a_(EntityHuman eh) {
		return true;
	}


	public ItemStack splitWithoutUpdate(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void onOpen(CraftHumanEntity che) {
	}

	public void onClose(CraftHumanEntity che) {
	}

	public List<HumanEntity> getViewers() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public InventoryHolder getOwner() {
		return null;
	}

	public void setMaxStackSize(int i) {
	}

	public void startOpen() {
	}

	@Override
	public boolean c() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean b(int i, ItemStack is) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void g() {
	}

} // end class InventorySmallChest

