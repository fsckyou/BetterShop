/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: class for defining an item
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
package me.jascotty2.lib.bukkit.item;

import me.jascotty2.lib.bukkit.MinecraftChatStr;
import me.jascotty2.lib.util.Str;
import java.util.ArrayList;
import java.util.LinkedList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class JItem {

	//protected static int MAX_LEVENSHTEIN_DIST = 1;
	protected JItems item = null;
	public String color = null; // color is used if this item has a custom color
	// data when masquerading something as an item
	protected boolean legal = true;
	protected int itemId = -1;
	protected int maxStack = 0; // so far, only set if init with Material or JItems
	protected short itemDat = 0;
	protected String name = null;
	private LinkedList<String> aliases = new LinkedList<String>();
	private LinkedList<String> subAliases = new LinkedList<String>();
	private LinkedList<String> categories = new LinkedList<String>();
	private LinkedList<CraftRecipe> recipes = new LinkedList<CraftRecipe>();

	public JItem() {
	}

	public JItem(int id) {
		item = JItems.getItem(id);
		setInf(id);
	}

	public JItem(int id, short data) {
		item = JItems.getItem(id, data);
		itemDat = data;
		setInf(id, data);
	}

	public JItem(int id, String name) {
		item = JItems.getItem(id);
		setInf(id);
		this.name = name;
	}

	public JItem(int id, short data, String name) {
		item = JItems.getItem(id, data);
		setInf(id, data);
		this.name = name;
	}

	public JItem(JItems copy) {
		item = copy;
		setInf();
	}

	public JItem(Material copy) {
		if (copy != null) {
			item = JItems.getItem(copy);
			setInf(copy.getId());
			if (item == null) {
				maxStack = copy.getMaxStackSize();
			}
		} else {
			setInf();
		}
		if (copy != null) {
			name = Str.titleCase(copy.name().replace("_", " "));
			//System.out.println(name + " (" + copy.getId() + ")   max: " + copy.getMaxStackSize());
		}
	}

	public JItem(JItem other) {
		copy(other);
	}

	public JItem(Kit copy) {
		if (copy != null) {
			this.name = copy.name;
			color = copy.color;
			itemId = copy.itemId;
			legal = copy.legal;
		}
	}

	private void setInf() {
		itemId = item == null ? -1 : item.ID();
		itemDat = item == null ? 0 : item.Data();
		legal = item == null ? true : item.IsLegal();
		name = item == null ? "null" : item.getName();
		maxStack = item == null ? 0 : item.MaxStackSize();
	}

	private void setInf(int id) {
		setInf(id, (short) 0);
	}

	private void setInf(int id, short dat) {
		if (item != null) {
			itemId = item.ID();
			itemDat = item.Data();
			legal = item.IsLegal();
			maxStack = item.MaxStackSize();
		} else {
			itemId = id;
			itemDat = dat;
		}
	}

	protected final void copy(JItem copy) {
		if (copy != null) {
			item = copy.item;
			color = copy.color;
			legal = copy.legal;
			name = copy.name;
			itemId = copy.itemId;
			itemDat = copy.itemDat;
			maxStack = copy.maxStack;

			aliases = copy.aliases;
			subAliases = copy.subAliases;
			categories = copy.categories;
			recipes = copy.recipes;
		}
	}

	public int ID() {
		return item == null ? itemId : item.ID();
	}

	public short Data() {
		return item == null ? itemDat : item.Data();
	}

	public short MaxDamage() {
		return item == null ? 0 : item.MaxDamage();
	}

	public boolean IsValidItem() {
		return item != null && item.ID() != 0;
	}

	public boolean IsTool() {
		return item == null ? false : item.IsTool();
	}

	public boolean IsLegal() {
		return item == null ? legal : item.IsLegal();
	}

	public int MaxStackSize() {
		return item == null ? 0 : item.MaxStackSize();
	}

	public String IdDatStr() {
		return item == null ? String.format("%d:%d", itemId, (int) itemDat) : item.IdDatStr();
	}

	public String Name() {
		return item == null ? name : item.getName();
	}

	public void SetItem(JItem copy) {
		if (copy == null) {
			item = null;
			color = null;
			itemId = -1;
			legal = true;
		} else {
			item = copy.item;
			color = copy.color;
			itemId = copy.itemId;
			legal = copy.legal;
		}
	}

	public void SetItem(JItems copy) {
		item = copy;
		color = null;
		setInf();
	}

	public void SetItem(int id, short data) {
		SetItem(JItems.getItem(id, data));
	}

	public void AddAlias(String a) {
		if (a != null) {
			aliases.add(a.trim().toLowerCase());
		}
	}

	public void AddSubAlias(String a) {
		if (a != null) {
			subAliases.add(a.trim().toLowerCase());
		}
	}

	public void AddCategory(String a) {
		if (a != null) {
			categories.add(a.trim().toLowerCase());
		}
	}

	public void AddRecipe(String craft) {
		System.out.println("adding " + craft);
		CraftRecipe toAdd = CraftRecipe.fromStr(craft);
		if (toAdd != null) {
			recipes.add(toAdd);
		} else {
			java.util.logging.Logger.getAnonymousLogger().log(java.util.logging.Level.WARNING,
					String.format("(class error) %s has an invalid item or craft syntax error: %s", name, craft));
		}
	}

	public void AddRecipe(CraftRecipe toAdd) {
		if (toAdd != null) {
			recipes.add(toAdd);
		}
	}

	public boolean HasAlias(String a) {
		return a == null ? false : aliases.contains(a.trim().toLowerCase());
//		if (a != null) {
//			a = a.trim().toLowerCase();
//			if (aliases.contains(a)) {
//				return true;
//			} else {
//				for (String al : aliases) {
//					if (Str.getLevenshteinDistance(a, al) <= MAX_LEVENSHTEIN_DIST) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
	}

	public boolean HasSubAlias(String a) {
		return a == null ? false : subAliases.contains(a.trim().toLowerCase());
//		if (a != null) {
//			a = a.trim().toLowerCase();
//			if (subAliases.contains(a)) {
//				return true;
//			} else {
//				for (String al : subAliases) {
//					if (Str.getLevenshteinDistance(a, al) <= MAX_LEVENSHTEIN_DIST) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
	}

	public boolean HasCategory(String a) {
		return a == null ? false : categories.contains(a.trim().toLowerCase());
	}

	public ArrayList<String> Aliases() {
		return new ArrayList<String>(aliases);
	}
//    public static JItem findItem(int id) {
//        JItems i = JItems.getItem(id);
//        return i == null ? null : new JItem(i);
//    }
//
////    public static JItem findItem(JItem it) {
////        JItems i = JItems.getItem(it.ID(), it.Data());
////        return i == null ? null : new JItem(i);
////    }
//    public static JItem findItem(ItemStack search) {
//        if (search == null) {
//            return null;
//        }
//        return findItem(search.getTypeId() + ":" + search.getDurability());
//    }
//
//    public static JItem findItem(ItemStockEntry search) {
//        if (search == null) {
//            return null;
//        }
//        return findItem(search.itemNum + ":" + search.itemSub);
//    }
//
//    public static JItem findItem(Item search) {
//        JItems i = JItems.findItem(search);
//        return i == null ? null : new JItem(i);
//    }

//    public static JItem findItem(int id, short sub) {
//        JItems i = JItems.getItem(id, sub);
//        return i == null ? null : new JItem(i);
//    }
//
//    public static JItem findItem(String search) {
//        JItems i = JItems.findItem(search);
//        //System.out.println("searching for " + search + " " + (i == null ? "(not found)" : i));
//        return i == null ? null : new JItem(i);
//    }
//
//    public static JItem[] findItems(String search) {
//        JItems its[] = JItems.findItems(search);
//        JItem ret[] = new JItem[its.length];
//        for (int i = 0; i < its.length; ++i) {
//            ret[i] = new JItem(its[i]);
//        }
//        return ret;
//    }
	public String coloredName() {
		return (color == null ? "" : color) + (item == null ? name : item.getName());
	}

	public boolean SetColor(String col) {
		if (col == null) {
			color = null;
		}
		color = MinecraftChatStr.getChatColorStr(col, ChatColor.WHITE);
		return color.length() > 0;
	}

	public boolean nameMatch(String str) {
		if (str != null) {
			str = str.trim().toLowerCase();
			if ((item != null
					&& (item.getName().equalsIgnoreCase(str) || HasAlias(str)))
					|| (name != null && name.equalsIgnoreCase(str))) {
				return true;
			}
			if (item != null && str.contains(":")) {
				String s1 = str.substring(0, str.indexOf(":")),
						s2 = str.substring(str.indexOf(":") + 1);
				return (name.equalsIgnoreCase(s1) || HasAlias(s1))
						&& HasSubAlias(s2);
			}
		}
		return false;
	}

	public boolean equals(JItem i) {
		return i != null
				&& (item != null ? i.item != null && item == i.item
				: itemId == i.itemId && itemDat == i.itemDat);
	}

	public boolean equals(JItems i) {
		return item == i;
	}

	public boolean equals(Item i) {
		return i != null && this.equals(i.getItemStack());
	}

	public boolean equals(ItemStack i) {
		return i != null && (item != null ? item.equals(i)
				: itemId == i.getTypeId() && (IsTool() || itemDat == i.getDurability()));
	}

	public boolean equals(ItemStockEntry i) {
		return i != null && (item != null ? item.ID() == i.itemNum && item.Data() == i.itemSub
				: itemId == i.itemNum && (IsTool() || itemDat == i.itemSub));
	}

	public boolean equals(String s) {
		return s != null
				&& ((item != null ? item.getName() : name).replace(" ", "").
				equalsIgnoreCase(s.replace(" ", ""))
				/*|| Str.getLevenshteinDistance(s.replace(" ", ""),
				(item != null ? item.getName() : name).replace(" ", "")) <= MAX_LEVENSHTEIN_DIST*/);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
//        if (obj instanceof String) {
//            return equals((String) obj);
//        }
//        else
		if (obj instanceof JItem) {
			return equals((JItem) obj);
		} else if (obj instanceof Item) {
			return equals((Item) obj);
		} else if (obj instanceof ItemStack) {
			return equals((ItemStack) obj);
		} else if (obj instanceof ItemStockEntry) {
			return equals((ItemStockEntry) obj);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + (this.item != null ? this.item.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return item == null ? (name == null ? "" : name) : item.toString();
	}

	public ItemStack toItemStack() {
		return item == null
				? (itemDat <= Byte.MAX_VALUE ? new ItemStack(itemId, 1, (short) itemDat) : new ItemStack(itemId, 1, itemDat))
				: (isEntity() ? null : item.toItemStack());
	}

	public ItemStack toItemStack(int amount) {
		return item == null
				? (itemDat <= Byte.MAX_VALUE ? new ItemStack(itemId, amount, (short) itemDat) : new ItemStack(itemId, amount, itemDat))
				: (isEntity() ? null : item.toItemStack(amount));
	}

	// creatures are numbered starting at 4000
	public boolean isEntity() {
		return item == null && itemId >= 4000 && itemId < 5000;
	}

	// kits are numbered at 5000+
	public boolean isKit() {
		return item == null ? itemId >= 5000 : item.ID() >= 5000;
	}

	public int getMaxStackSize() {
		return item == null ? maxStack : item.MaxStackSize();
	}

	protected void setMaxStack(int stack) {
		maxStack = stack;
		if (item != null) {
			item.setMaxStack(stack);
		}
	}

	public static boolean contains(JItem[] items, ItemStack check) {
		if (items != null) {
			if (check == null) {
				for (JItem i : items) {
					if (i == null) {
						return true;
					}
				}
			} else {
				for (JItem i : items) {
					if (i != null && i.equals(check)) {
						return true;
					}
				}
			}
		}
		return false;
	}
} // end class JItem

