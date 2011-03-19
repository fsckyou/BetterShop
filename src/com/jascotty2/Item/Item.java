/**
 * Programmer: Jacob Scott
 * Program Name: Item
 * Description: class for defining an item
 * Date: Mar 12, 2011
 */
package com.jascotty2.Item;

import com.jascotty2.CheckInput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.inventory.ItemStack;

public class Item {
    // all items.. used by ItemDB

    protected static HashMap<String, Item> items = new HashMap<String, Item>();
    // Item Information
    protected int itemId;
    protected byte itemData;
    protected boolean isLegal = true; // is is legitemitely obtainable
    protected int maxStack = 64;
    // color is used if this item has a custom color
    public String name, color = null;
    private LinkedList<String> itemAliases = new LinkedList<String>();
    private LinkedList<String> subAliases = new LinkedList<String>();
    private LinkedList<CraftRecipe> recipes = new LinkedList<CraftRecipe>();
    // max data value to accept in a damage value (ItemStack)
    //public final static byte MAX_DATA = (byte)30;
    // max damage. indicates that this is a tool
    protected short maxdamage = 0;

    public Item() {
        itemId = -1;
        name = "";
    } // end default constructor

    public Item(String name) {
        this.name = "";
        if (!setIDD(name)) {
            this.name = name;
        }
    }

    public Item(Item copy) {
        SetItem(copy);
    }

    public Item(int id) {
        itemId = id;
        name = "";
    }

    public Item(int id, byte dat) {
        itemId = id;
        itemData = dat;
        name = "";
    }

    public Item(int id, byte dat, String name) {
        itemId = id;
        itemData = dat;
        this.name = name;
    }

    public Item(ItemStack i) {
        itemId = i.getTypeId();
        //if(maxdamage==0)// i.getDurability()<MAX_DATA)
        itemData = (byte) i.getDurability();
        name = "";
    }

    public int ID() {
        return itemId;
    }

    public byte Data() {
        return itemData;
    }

    public short MaxDamage() {
        return maxdamage;
    }

    public boolean IsTool() {
        return maxdamage > 0;
    }

    public boolean IsLegal() {
        return isLegal;
    }

    public String IdDatStr() {
        return String.format("%d:%d", itemId, itemData);
    }

    public void setID(int id) {
        if (id >= 0 && id <= 9999) {
            itemId = id;
        }
    }

    public void setData(Byte d) {
        itemData = d;
    }

    public void setMaxDamage(short d) {
        maxdamage = d;
    }

    public void SetLegal(boolean isAllowed) {
        isLegal = isAllowed;
    }

    public void SetMaxStack(int stack) {
        maxStack = stack;
    }

    public final boolean setIDD(String idd) {
        if (idd.contains(":")) {
            if (idd.length() > idd.indexOf(":")
                    && CheckInput.IsInt(idd.substring(0, idd.indexOf(":")))
                    && CheckInput.IsByte(idd.substring(idd.indexOf(":") + 1))) {
                itemId = CheckInput.GetInt(idd.substring(0, idd.indexOf(":")), -1);
                itemData = CheckInput.GetByte(idd.substring(idd.indexOf(":") + 1), (byte) 0);
            } else {
                itemId = -1;
            }
        } else {
            itemId = CheckInput.GetInt(idd, -1);
        }
        return itemId >= 0;
    }

    public final void SetItem(Item copy) {
        itemAliases.clear();
        subAliases.clear();
        if (copy == null) {
            this.itemId = -1;
            this.itemData = (byte) 0;
            this.name = "null";
        } else {
            this.itemAliases.addAll(copy.itemAliases);
            this.subAliases.addAll(copy.subAliases);
            this.itemId = copy.itemId;
            this.itemData = copy.itemData;
            this.name = copy.name;
            this.isLegal = copy.isLegal;
            this.maxStack = copy.maxStack;
        }
    }

    public void AddAlias(String a) {
        itemAliases.add(a.trim().toLowerCase());
    }

    public void AddSubAlias(String a) {
        subAliases.add(a.trim().toLowerCase());
    }

    public void AddRecipe(String craft) {
        CraftRecipe toAdd = CraftRecipe.fromStr(craft);
        if (toAdd != null) {
            recipes.add(toAdd);
        }
    }

    public void AddRecipe(CraftRecipe toAdd) {
        if (toAdd != null) {
            recipes.add(toAdd);
        }
    }

    public boolean HasRecipe() {
        return recipes.size() > 0;
    }

    public Kit[] GetRecipeAsKit(int index) {
        if (index >= 0 && index < recipes.size()) {
            return recipes.get(index).getKits();
        }
        return null;
    }

    public Kit[] GetFullRecipeKits() {
        LinkedList<Kit> ret = new LinkedList<Kit>();
        for (CraftRecipe c : recipes) {
            ret.addAll(Arrays.asList(c.getKits()));
        }
        return ret.toArray(new Kit[0]);
    }

    public CraftRecipe GetRecipe(int index) {
        if (index >= 0 && index < recipes.size()) {
            return recipes.get(index);
        }
        return null;
    }

    public CraftRecipe[] GetRecipes() {
        return recipes.toArray(new CraftRecipe[0]);
    }

    public boolean HasAlias(String a) {
        return itemAliases.contains(a.trim().toLowerCase());
    }

    public boolean HasSubAlias(String a) {
        return subAliases.contains(a.trim().toLowerCase());
    }

    public static Item findItem(ItemStack search) {
        if (search == null) {
            return null;// || search.getAmount()==0
        }
        return findItem(search.getType() + ":" + search.getDurability());//(search.getDurability()<MAX_DATA?search.getDurability():0));
    }

    public static Item findItem(Item search) {
        if (search == null) {
            return null;
        }
        Item searchitem = new Item(search);
        for (Item i : items.values()) {
            //if(i.equals(search)) return i;
            if (i.equals(searchitem)) {
                return i;
            }
        }
        return null;
        /*
        if (search.ID() >= 0) {
        return findItem(search.IdDatStr());
        }
        return findItem(search.name);*/
    }

    public static Item findItem(int id, byte sub) {
        for (Item i : items.values()) {
            if (i.ID() == id && i.Data() == sub) {
                return i;
            }
        }
        return null;
    }

    public static Item findItem(String search) {
        if (search == null) {
            return null;
        }
        //System.out.println("searching: " + search);
        if (items.containsKey(search)) {
            return items.get(search);
        } else if (CheckInput.IsInt(search)) {// && (!search.contains(":") || search.length() == search.indexOf(":"))) {
            return items.get(search.replace(":", "") + ":0");
        } else if (search.contains(":")) {
            // run a search for both parts (faster than .equals for string)
            Item isearch = findItem(search.substring(0, search.indexOf(":")));
            //System.out.println("found: " + (isearch==null?"null" : isearch) + "   " + (isearch != null && isearch.IsTool()));
            if (isearch != null) {
                if (isearch.IsTool()) {
                    // this is a tool, so return as found
                    return isearch;
                } else {
                    int id = isearch.ID();
                    // now check second part
                    if (CheckInput.IsByte(search.substring(search.indexOf(":") + 1))) {
                        byte dat = CheckInput.GetByte(search.substring(search.indexOf(":") + 1), (byte) 0);
                        for (Item i : items.values()) {
                            if (i.ID() == id && i.Data() == dat) {
                                return i;
                            }
                        }
                    }
                    search = search.substring(search.indexOf(":") + 1);
                    for (Item i : items.values()) {
                        if (i.ID() == id && i.HasSubAlias(search)) {
                            return i;
                        }
                    }
                }
            }
        } else {
            Item searchitem = new Item(search);
            for (Item i : items.values()) {
                //if(i.equals(search)) return i;
                if (i.equals(searchitem)) {
                    return i;
                }
            }
        }
        return null;
    }

    public static Item[] findItems(String search) {
        if (search == null) {
            return null;
        }
        if (CheckInput.IsInt(search)) {
            return new Item[]{items.get(search.replace(":", "") + ":0")};
        } else if (items.containsKey(search)) {
            return new Item[]{items.get(search)};
        } else if (search.contains(":")) {
            return new Item[]{findItem(search)};
        }
        ArrayList<Item> found = new ArrayList<Item>();
        search = search.toLowerCase();
        // run a name search
        for (Item i : items.values()) {
            if (i.name.toLowerCase().contains(search)) {
                found.add(i);
            } else {
                for (String suba : i.itemAliases) {
                    if (suba.contains(search)) {
                        found.add(i);
                        break;
                    }
                }
            }
        }
        return found.toArray(new Item[0]);
    }

    public boolean equals(Item i) {
        if (i == null) {
            return false;
        }
        return (i.ID() == itemId && ((IsTool() && i.IsTool()) || i.Data() == itemData)) || i.equals(name) || equals(i.name);
    }

    public boolean equals(String s) {
        if (s == null) {
            return false;
        }
        s = s.toLowerCase().trim();
        if (s.contains(":")) {
            // find base id
            Item first = findItem(s.substring(0, s.indexOf(":")));
            // if exists & id matched this one:
            if (first != null && first.ID() == itemId) {
                // check if second part is a number or alias
                if (CheckInput.IsByte(s.substring(s.indexOf(":") + 1))) {
                    return itemData == CheckInput.GetByte(s.substring(s.indexOf(":") + 1), (byte) 0);
                } else {
                    return itemAliases.contains(s.substring(s.indexOf(":") + 1));
                }
            } else {
                return false;
            }
            //return (s.substring(0, s.indexOf(":")).equalsIgnoreCase(name) || itemAliases.indexOf(s.substring(0, s.indexOf(":"))) != -1)
            //        && (subAliases.indexOf(s.substring(s.indexOf(":") + 1)) != -1);
        } else {
            return s.equalsIgnoreCase(name) || itemAliases.indexOf(s) != -1;
        }
    }

    public String coloredName() {
        return color == null ? name : color + name;
    }

    public boolean SetColor(String col) {
        if (col == null) {
            return false;
        }
        col = col.toLowerCase().trim();
        /*
        #       &0 is black
        #       &1 is dark blue
        #       &2 is dark green
        #       &3 is dark sky blue
        #       &4 is red
        #       &5 is magenta
        #       &6 is gold or amber
        #       &7 is light grey
        #       &8 is dark grey
        #       &9 is medium blue
        #       &2 is light green
        #       &b is cyan
        #       &c is orange-red
        #       &d is pink
        #       &e is yellow
        #       &f is white
         */
        if (col.equalsIgnoreCase("black")) {
            color = "\u00A70"; //String.format("\u00A7%x", 0x0);// 
        } else if (col.equals("blue") || col.equals("dark blue")) {
            color = "\u00A71"; // String.format("\u00A7%x", 0x1);// 
        } else if (col.equals("green") || col.equals("dark green")) {
            color = "\u00A72"; // String.format("\u00A7%x", 0x2);// 
        } else if (col.equals("sky blue") || col.equals("dark sky blue")) {
            color = "\u00A73"; // String.format("\u00A7%x", 0x3);// 
        } else if (col.equals("red")) {
            color = "\u00A74"; // String.format("\u00A7%x", 0x4);// 
        } else if (col.equals("magenta") || col.equals("purple")) {
            color = "\u00A75"; // String.format("\u00A7%x", 0x5);// 
        } else if (col.equals("gold") || col.equals("amber") || col.equals("dark yellow")) {
            color = "\u00A76"; // String.format("\u00A7%x", 0x6);// 
        } else if (col.equals("light gray") || col.equals("light grey")) {
            color = "\u00A77"; // String.format("\u00A7%x", 0x7);// 
        } else if (col.equals("dark gray") || col.equals("dark grey") || col.equals("gray") || col.equals("grey")) {
            color = "\u00A78"; // String.format("\u00A7%x", 0x8);// 
        } else if (col.equals("medium blue")) {
            color = "\u00A79"; // String.format("\u00A7%x", 0x9);// 
        } else if (col.equals("light green") || col.equals("lime") || col.equals("lime green")) {
            color = "\u00A7a"; // String.format("\u00A7%x", 0xA);// 
        } else if (col.equals("cyan") || col.equals("light blue")) {
            color = "\u00A7b"; // String.format("\u00A7%x", 0xB);// 
        } else if (col.equals("orange") || col.equals("orange-red") || col.equals("red-orange")) {
            color = "\u00A7c"; // String.format("\u00A7%x", 0xC);// 
        } else if (col.equals("pink") || col.equals("light red")) {
            color = "\u00A7d"; // String.format("\u00A7%x", 0xD);// 
        } else if (col.equals("yellow")) {
            color = "\u00A7e"; // String.format("\u00A7%x", 0xE);// 
        } else if (col.equals("white")) {
            color = "\u00A7f"; //String.format("\u00A7%x", 0xF);//
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Item) {
            return equals((Item) obj);
        } else if (obj instanceof String) {
            return equals((String) obj);
        } else if (obj instanceof ItemStack) {
            return equals((ItemStack) obj);
        }
        return false;
    }

    public boolean equals(ItemStack i) {
        if (i == null) {
            return false;
        }
        return itemId == i.getTypeId() && (IsTool() || itemData == i.getDurability());
    }
    /*
    public boolean equals(KitItem ki){
    return ki.equals(this);
    }*/

    // required for equals(Object obj)
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.itemId;
        hash = 79 * hash + this.itemData;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s (%d:%d)", name, itemId, itemData);
    }

    public ItemStack toItemStack() {
        return new ItemStack(itemId, 1, (short) 0, itemData);
    }

    public ItemStack toItemStack(int amount) {
        return new ItemStack(itemId, amount, (short) 0, itemData);
    }

    // kits are numbered at 5000+
    public boolean isKit() {
        return itemId >= 5000;
    }

    public int getMaxStackSize() {
        return maxStack;
    }
} // end class Item

