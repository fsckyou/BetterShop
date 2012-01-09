/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: searchable database for items
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

import me.jascotty2.lib.io.CheckInput;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jascotty2.lib.util.ArrayManip;
import me.jascotty2.lib.util.Str;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.inventory.ItemStack;

import me.jascotty2.lib.bukkit.config.Configuration;
import me.jascotty2.lib.bukkit.config.ConfigurationNode;

public class JItemDB {

	protected static int MAX_LEVENSHTEIN_DIST = 2;
	private final static Logger logger = Logger.getLogger("Minecraft");
	protected static Map<String, JItem> items = new HashMap<String, JItem>();
	protected static Map<Integer, Kit> kits = new HashMap<Integer, Kit>();
	protected static List<String> itemCategories = new ArrayList<String>();
	protected static Map<String, List<JItem>> itemCategoryItemlist = new HashMap<String, List<JItem>>();
	protected static boolean dbLoaded = false;

	public static boolean load() {
		return load(new File("."), "itemsdb.yml");
	}

	public static boolean load(File folder, String fname) {
		return load(new File(folder, fname)); // "itemsdb.yml"
	}

	public static boolean load(File itemFile) {
		//if(!dbLoaded){
		loadDefaultItems();
		//}
		if (itemFile != null && itemFile.exists()) {
			try {
				Configuration itemdb = new Configuration(itemFile);
				itemdb.load();

				ConfigurationNode n = itemdb.getNode("items");
				if (n == null) {
					logger.log(Level.WARNING, String.format("\'items\' not found in %s", itemFile.getName()));
				} else {
					// will run through items twice: 1st load items, then the craft recipes
					for (String k : itemdb.getKeys("items")) {
						if (k.length() >= 5 && k.substring(0, 4).equalsIgnoreCase("item")) {
							n = itemdb.getNode("items." + k);
							if (n != null) {
								JItem item = null;
								if (k.indexOf("sub") > 0) {
									//new JItem();
									item = GetItem(CheckInput.GetInt(k.substring(4, k.indexOf("sub")), -1),
											CheckInput.GetByte(k.substring(k.indexOf("sub") + 3), (byte) 0));
								} else {
									item = GetItem(CheckInput.GetInt(k.substring(4), -1));
								}
								if (item != null) {//  && item.item != null) {

									// can also have a sub-value alias (ex: wool:green)
									String a = n.getString("sub");
									if (a != null) {
										String all[] = a.split(",");
										for (String i : all) {
											item.AddSubAlias(i.trim());
										}
									}

									a = n.getString("name");
									if (a != null) {
										a = a.replace("'", "").replace("\"", "");
										item.name = a;
										if (item.item != null) {
											item.item.name = a;
										}
									}

									item.SetColor(n.getString("color"));

									if (n.getBoolean("legal", item.legal) != item.legal) {
										item.legal = !item.legal;
									}

									// now add aliases
									a = n.getString("aliases");
									if (a != null) {
										String all[] = a.split(",");
										for (String i : all) {
											item.AddAlias(i.trim().toLowerCase());
										}
									}

									// now check for custom maxstack
									if (n.getString("maxstack") != null) {
										item.setMaxStack(n.getInt("maxstack", item.maxStack));
									}

									items.put(item.IdDatStr(), item);
								}
								//System.out.println("Added: " + item);
							}
						}

					}
					// now load craft recipes
					for (String k : itemdb.getKeys("items")) {
						if (k.length() >= 5 && k.substring(0, 4).equalsIgnoreCase("item")) {
							n = itemdb.getNode("items." + k);
							if (n != null) {
								JItem item = JItemDB.findItem(n.getString("name"));
								String a = n.getString("craft");
								if (a != null && item != null) {
									String all[] = a.split(",");
									for (String i : all) {
										CraftRecipe toAdd = CraftRecipe.fromStr(i.trim());
										if (toAdd != null) {
											item.AddRecipe(toAdd);
										} else {
											logger.log(Level.WARNING, String.format("%s has an invalid item or craft syntax error: %s", item.toString(), i));
										}
									}
								}
							}
						}
					}
				} // end item loading

				n = itemdb.getNode("kits");
				if (n == null) {
					logger.log(Level.WARNING, String.format("\'kits\' not found in %s", itemFile.getName()));
				} else {
					kits.clear();
					for (String k : itemdb.getKeys("kits")) { // for( : itemdb.getNodeList("", null)){
						if (k.length() >= 4 && k.substring(0, 3).equalsIgnoreCase("kit")) {
							n = itemdb.getNode("kits." + k);
							if (n != null) {
								String a = n.getString("items");
								if (a != null) {
									Kit kit = Kit.fromStr(a);
									if (kit == null) {
										logger.log(Level.WARNING, String.format("kit %s (%s) is an invalid item or has incorrect syntax", k, n.getString("name", "not named")));
									} else if (n.getString("name", "").length() == 0) {
										logger.log(Level.WARNING, String.format("kit %s has no name", k));
									} else {
										// kits are numbered starting at 5000
										kit.itemId = 4999 + CheckInput.GetInt(k.substring(3), 0);
										if (kit.itemId <= 4999) {
											logger.log(Level.WARNING, String.format("%s is an invalid kit number. (Must start at 1)", k));
											continue;  //next kit node
										}
										kit.SetColor(n.getString("color"));
										kit.legal = n.getBoolean("legal", true);
										kit.name = n.getString("name", "null");
										// now add aliases (if any)
										a = n.getString("aliases");
										if (a != null) {
											String all[] = a.split(",");
											for (String i : all) {
												kit.AddAlias(i.trim().toLowerCase());
											}
										}
//                                            String o = "";
//                                            for(Kit.KitItem ki : kit.getKitItems()){
//                                                o+=ki.toString() + ", ";
//                                            }
//                                            System.out.println(o);

										// add to list
										kits.put(kit.itemId, kit);
										// add to item list
										items.put(kit.IdDatStr(), kit);

									}
								}
							}
						}
					}
				} // end kit loading

				n = itemdb.getNode("entities");
				if (n == null) {
					logger.log(Level.WARNING, String.format("\'entities\' not found in %s", itemFile.getName()));
				} else {
					for (String k : itemdb.getKeys("entities")) {
						if (k.length() >= 7 && k.substring(0, 6).equalsIgnoreCase("entity")) {
							n = itemdb.getNode("entities." + k);
							if (n != null) {
								int eid = CheckInput.GetInt(k.substring(6), -1);
								JItem citem = GetItem(eid + 4000);
								if (eid >= 0 && citem != null) {
									//CreatureItem citem = CreatureItem.getCreature(eid);

									// now add aliases
									String a = n.getString("aliases");
									if (a != null) {
										String all[] = a.split(",");
										for (String i : all) {
											citem.AddAlias(i.trim().toLowerCase());
										}
									}
									items.put(String.format("%d:0", citem.ID()), citem);
								}
							}
						}
					}
				}

				String catErr = "";
				n = itemdb.getNode("categories");
				if (n == null) {
					logger.log(Level.WARNING, String.format("\'categories\' not found in %s", itemFile.getName()));
				} else {
					itemCategories.clear();
					itemCategoryItemlist.clear();
					List<String> cats = itemdb.getKeys("categories");
					for (String c : cats) {
						String itms = n.getString(c);
						if (c != null && itms != null) {
							String list[] = itms.split(",");
							ArrayList<JItem> categoryItems = new ArrayList<JItem>();
							for (String i : list) {
								i = i.trim();
								if (i.length() > 0) {
									JItem it = findItem(i);
									if (it != null) {
										it.AddCategory(c);
										categoryItems.add(it);
									} else {
										catErr += i + ", ";
									}
								}
							}
							itemCategories.add(c);
							itemCategoryItemlist.put(c, categoryItems);
						}
					}
					if (catErr.length() > 0) {
						logger.warning(String.format("Invalid item entries in %s categories: %s", itemFile.getName(), catErr.substring(0, catErr.length() - 2)));
					}
				}
				//logger.log(Level.INFO, "Items loaded: " + items.size() + " + " + kits.size() + " kits");
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Error loading itemsdb.yml", ex);
				return dbLoaded = false;
			}
			return dbLoaded = true;
		}
		return false;
	}

	/**
	 * clears items & loads with defaults
	 */
	private static void loadDefaultItems() {
		items.clear();
		// all entities (pre-1.8b)
		CreatureType ordered[] = new CreatureType[]{
						CreatureType.CHICKEN, CreatureType.COW,
						CreatureType.CREEPER, CreatureType.GHAST,
						CreatureType.GIANT, CreatureType.MONSTER,
						CreatureType.PIG, CreatureType.PIG_ZOMBIE,
						CreatureType.SHEEP, CreatureType.SKELETON,
						CreatureType.SLIME, CreatureType.SPIDER,
						CreatureType.SQUID, CreatureType.ZOMBIE,
						CreatureType.WOLF};
		try {
			CreatureType t = CreatureType.CAVE_SPIDER;
			// success, is at least a 1.8 server
			ordered = ArrayManip.arrayConcat(ordered,
					new CreatureType[]{
				CreatureType.CAVE_SPIDER, CreatureType.ENDERMAN, CreatureType.SILVERFISH});
			// now for 1.0
			t = CreatureType.ENDER_DRAGON;
			
			ordered = ArrayManip.arrayConcat(ordered,
					new CreatureType[]{
				CreatureType.ENDER_DRAGON, CreatureType.VILLAGER,
				CreatureType.BLAZE, CreatureType.MUSHROOM_COW,
				CreatureType.MAGMA_CUBE, CreatureType.SNOWMAN});
    
		} catch (Throwable t) {
		}
		int i = 0;
		for (; i < ordered.length; ++i) {
			items.put((4000 + i) + ":0", new CreatureItem(ordered[i]));
		}
		// now check for existing entities that aren't in the ordered array
		for (CreatureType c : CreatureType.values()) {
			if (ArrayManip.indexOf(ordered, c) == -1) {
				items.put((4000 + (i++)) + ":0", new CreatureItem(c));
				System.out.println("New Entity: (" + (4000 + (i - 1)) + ") " + c.getName());
			}
		}
		// now add hard-coded entries, including subtypes
		for (JItems it : JItems.values()) {
			if (it.ID() >= 0) {
				items.put(it.IdDatStr(), new JItem(it));
			}
		}
		// add all items in Material.values() aren't in JItems (upgrade-proof)
		for (Material m : Material.values()) {
			String idd = m.getId() + ":0";
			if (!items.containsKey(idd)) {
				items.put(idd, new JItem(m));
			}
		}
	}

	public boolean isLoaded() {
		return dbLoaded;
	}

	public static JItem GetItem(ItemStockEntry i) {
		return i == null ? null : GetItem(i.itemNum, (byte) i.itemSub);
	}

	public static JItem GetItem(ItemStack i) {
		return i == null ? null : GetItem(i.getTypeId(), (i.getData() == null ? (byte) 0 : i.getData().getData()));
	}

	public static JItem GetItem(int id) {
		return GetItem(id, (byte) 0);
	}

	public static JItem GetItem(int id, byte dat) {
		if(JItems.isTool(id) || !JItems.hasData(id)) {
			dat = 0;
		}
		String idd = id + ":" + dat;
		if (items.containsKey(idd)) {
			return items.get(idd);
		}
//        if (dat == 0) {
//            for (Kit k : kits.values()) {
//                if (k.itemId == id) {
//                    return k;
//                }
//            }
//
////            CreatureItem c = CreatureItem.getCreature(id);
////            if (c != null) {
////                return new JItem(c.ID(), c.name);
////            }
//        }
		return null;
	}

	public static JItem findItem(ItemStack search) {
		try {
			return search == null ? null : GetItem(search.getTypeId(), (search.getData() == null ? (byte) 0 : search.getData().getData()));
		} catch (Exception e) {
			// recent bukkit fix..
			return GetItem(search.getTypeId(), (byte) 0);
		}
	}

	public static JItem findItem(String search) {

		if (search == null) {
			return null;
		} else if (CheckInput.IsInt(search)) {
			return GetItem(CheckInput.GetInt(search, Integer.MIN_VALUE));
		}

		JItem it = null;
		String s1 = search.contains(":") ? search.substring(0, search.indexOf(':')).trim() : search,
				s2 = search.contains(":") ? search.substring(search.indexOf(':') + 1).trim() : "";

		if (CheckInput.IsInt(s1)) {
			it = GetItem(CheckInput.GetInt(s1, Integer.MIN_VALUE));
		} else {
			for (JItem i : items.values()) {
				if (i != null && (i.equals(s1) //&& ((i.name != null && i.name.replace(" ", "").equalsIgnoreCase(s1))
						|| i.HasAlias(s1))) {
					it = i;
					break;
				}
			}
		}

		if (it == null) { // if no imediate result: check plurality
			for (String ss : new String[]{
						(s1.endsWith("s") ? s1.substring(0, s1.length() - 1) : null),
						(s1.endsWith("es") ? s1.substring(0, s1.length() - 2) : null),
						(s1.endsWith("ies") ? s1.substring(0, s1.length() - 3) + "y" : null)}) {
				if (ss == null) {
					break;
				}
				for (JItem i : items.values()) {
					if (i != null && i.equals(ss) /* && i.name != null
							&& (i.name.replace(" ", "").equalsIgnoreCase(ss) || i.HasAlias(ss))*/) {
						it = i;
						break;
					}
				}
				if (it != null) {
					break;
				}
			}
		}

//		if (it == null) {
//
//		}

		if (it == null) {
			// still no result: now do a string compare
			JItem match = null;
			int matchDist = MAX_LEVENSHTEIN_DIST + 1, numMatch = 0;
			for (JItem i : items.values()) {
				if (i != null) { // double-checking..
					int d = Str.getLevenshteinDistance(i.Name().replace(" ", ""), s1);
					if (d < matchDist) {
						match = i;
						matchDist = d;
						numMatch = 1;
					} else if (d == matchDist) {
						++numMatch;
					}
				}
			}
			if (numMatch == 1) {
				it = match;
			} else {
				// now check aliases
				match = null;
				matchDist = MAX_LEVENSHTEIN_DIST + 1;
				numMatch = 0;
				for (JItem i : items.values()) {
					if (i != null) { // double-checking..
						int d = MAX_LEVENSHTEIN_DIST + 1;
						for (String a : i.Aliases()) {
							int d2 = Str.getLevenshteinDistance(a, s1);
							if (d2 < d) {
								d = d2;
							}
						}
						if (d < matchDist) {
							match = i;
							matchDist = d;
							numMatch = 1;
						} else if (d == matchDist) {
							++numMatch;
						}
					}
				}
			}
		}

		if (it != null) {
			if (!it.IsTool() && s2.length() > 0) {
				// now check second part
				if (CheckInput.IsByte(s2)) {
					byte dat = CheckInput.GetByte(s2, (byte) 0);
					for (JItem i : items.values()) {
						if (i.itemId == it.itemId && i.itemDat == dat) {
							return i;
						}
					}
				}
				for (JItem i : items.values()) {
					if (i.itemId == it.itemId && i.HasSubAlias(s2)) {
						return i;
					}
				}
				return null;
			} else {
				return it;
			}
		}

		return null;
	}

	public static JItem[] findItems(String search) {
		if (search == null) {
			return null;
		}
		if (CheckInput.IsInt(search)) {
			return new JItem[]{GetItem(CheckInput.GetInt(search, Integer.MIN_VALUE))};
		} else if (search.contains(":")) {
			return new JItem[]{findItem(search)};
		}
		ArrayList<JItem> found = new ArrayList<JItem>();
		search = search.trim().toLowerCase();

		// run a name search
		for (String ss : new String[]{
					search,
					(search.endsWith("s") ? search.substring(0, search.length() - 1) : null),
					(search.endsWith("es") ? search.substring(0, search.length() - 2) : null),
					(search.endsWith("ies") ? search.substring(0, search.length() - 3) + "y" : null)}) {
			if (ss == null) {
				break;
			}

			for (JItem i : items.values()) {
				if (i != null && !found.contains(i)
						&& (i.equals(ss) /*
						&& (i.name != null && i.name.replace(" ", "").toLowerCase().contains(ss)
						|| i.HasAlias(ss) */
						|| (i.HasCategory(ss)))) {
					found.add(i);
				} else {
					for (String suba : i.Aliases()) {
						if (suba.contains(ss) && !found.contains(i)) {
							found.add(i);
							break;
						}
					}
				}
			}
		}
		return found.toArray(new JItem[0]);
	}

	public static JItem[] getItemsByCategory(String search) {
		if (search == null) {
			return null;
		}

		ArrayList<JItem> found = new ArrayList<JItem>();
		search = search.trim().toLowerCase();
		// run a name search
		for (String ss : new String[]{
					search,
					(search.endsWith("s") ? search.substring(0, search.length() - 1) : null),
					(search.endsWith("es") ? search.substring(0, search.length() - 2) : null),
					(search.endsWith("ies") ? search.substring(0, search.length() - 3) + "y" : null)}) {
			if (ss == null) {
				break;
			}
			for (JItem i : items.values()) {
				if ((i.HasCategory(search))
						&& !found.contains(i)) {
					found.add(i);
				}
			}
		}
		return found.toArray(new JItem[0]);
	}

	public static String[] getCategories() {
		return itemCategories.toArray(new String[0]);
	}

	public static boolean isCategory(String s) {
		return itemCategories.contains(s);
	}

	public static boolean isCategory(String s, boolean permissive) {
		if (!permissive) {
			return itemCategories.contains(s);
		}
		for (String c : itemCategories) {
			if (Str.getLevenshteinDistance(s.toLowerCase(), c.toLowerCase()) < 4) {
				return true;
			}
		}
		return false;
	}

	public static String findCategory(String s) {
		for (String c : itemCategories) {
			if (Str.getLevenshteinDistance(s.toLowerCase(), c.toLowerCase()) < MAX_LEVENSHTEIN_DIST) {
				return c;
			}
		}
		return null;
	}

	public static List<JItem> getCategory(String cat) {
		return itemCategoryItemlist.get(cat);
	}

	public static String GetItemName(ItemStack search) {
		if (search == null) {
			return null;
		}
		return GetItemName(search.getTypeId(), search.getData() == null ? 0 : search.getData().getData());
	}

	public static String GetItemName(int id) {
		return GetItemName(id, (byte) 0);
	}

	public static String GetItemName(int id, byte dat) {

		for (JItem i : items.values()) {
			if (i.ID() == id && i.Data() == dat) {
				return i.Name();
			}
		}
		for (Kit k : kits.values()) {
			if (k.ID() == id) {
				return k.Name();
			}
		}
//        CreatureItem c = CreatureItem.getCreature(id);
//        if (c != null) {
//            return c.name;
//        }
		return null;
	}

	public static String GetItemColoredName(ItemStack it) {
		return it == null ? null : GetItemColoredName(it.getTypeId(), it.getData() == null ? 0 : it.getData().getData());
	}

	public static String GetItemColoredName(int id, byte dat) {

		for (JItem i : items.values()) {
			if (i.ID() == id && i.Data() == dat) {
				return i.coloredName();
			}
		}
		for (Kit k : kits.values()) {
			if (k.ID() == id) {
				return k.coloredName();
			}
		}
//        CreatureItem c = CreatureItem.getCreature(id);
//        if (c != null) {
//            return c.name;
//        }
		return null;
	}

	public static boolean isItem(String search) {
		JItem temp = findItem(search);
		return temp != null && temp.itemId < 5000;
	}

	public static Kit getKit(String search) {
		/*if (kits.containsKey(search)) {
		return kits.get(search);
		}*/
		for (Kit k : kits.values()) {
			if (k.equals(search)) {
				// debugging: output items in kit
                /*
				System.out.println("kit " + search + " found: ");
				for (Kit.KitItem i : k.getKitItems()) {
				System.out.println(i + "( " + i.itemAmount + " )");
				}//*/
				return k;
			}
		}
		return null;
	}

	public static Kit getKit(JItem search) {
		/*if (!search.isKit()) {
		return null;
		}*/
		return kits.get(search.itemId);
	}

	public static boolean isKit(String search) {
		return getKit(search) != null;
	}

	public static int size() {
		return items.size();
	}

	/**
	 * can really only be run once...
	 * assigns a color to items that don't have one assigned
	 * @param col color string to set
	 */
	public static void setDefaultColor(String col) {
		for (JItem i : items.values()) {
			if (i.color == null || i.color.length() == 0) {
				i.SetColor(col);
			}
		}
	}
//	public static void main(String[] args) {
//		//JItemDB.load(new java.io.File("../../../../BetterShop/src/itemsdb.yml"));
//		JItemDB.load(new java.io.File("/media/Data/Jacob/Programs/Java/Bukkit Plugins/BetterShop/src/itemsdb.yml"));
//		me.jascotty2.lib.io.ConsoleInput c = new me.jascotty2.lib.io.ConsoleInput();
//		String in = "";
//		while ((in = c.GetString("\n> ")).length() > 0) {
//			JItem i = findItem(in);
//			if (i == null) {
//				JItem is[] = JItemDB.getItemsByCategory(in);
//				if (is != null && is.length > 0) {
//					System.out.println("found in category: ");
//					for (JItem i2 : is) {
//						System.out.println(i2);
//					}
//				} else {
//					in = c.GetString("not found.. input the id# ");//, 0, 10000);
//					i = findItem(in);
//					if (i == null) {
//						System.out.println("invalid #");
//					} else {
//						System.out.println(i + ": " + i.Aliases());
//					}
//				}
//			} else {
//				System.out.println("found: " + in + "==" + i);
//				for (JItem i2 : findItems(in)) {
//					System.out.println("found2: " + i2);
//				}
//			}
//		}
//	}
}
