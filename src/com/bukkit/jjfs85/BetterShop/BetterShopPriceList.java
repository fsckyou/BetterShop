package com.bukkit.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.util.config.*;

public class BetterShopPriceList {
	final Map<Integer, Integer> BuyMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> SellMap = new HashMap<Integer, Integer>();
	final Map<Integer, String> NameMap = new HashMap<Integer, String>();
	final File PLfile = new File("plugins/BetterShop", "PriceList.yml");
	private final Configuration PriceList = new Configuration(PLfile);

	public void load() throws IOException {
		int i = 1;
		if (!PLfile.exists())
			PLfile.createNewFile();
		PriceList.load();
		BuyMap.clear();
		SellMap.clear();
		NameMap.clear();
		while (i < 2280) {
			int buy = -1;
			int sell = -1;
			String name = "Unk";
			try {
				Material.getMaterial((String.valueOf(i)));
			} catch (Exception e) {
				i++;
				continue;
			}
			buy = PriceList.getInt("prices.item" + String.valueOf(i) + ".buy",
					-1);
			sell = PriceList.getInt(
					"prices.item" + String.valueOf(i) + ".sell", -1);
			name = PriceList.getString("prices.item" + String.valueOf(i)
					+ ".name", "Unk");
			// System.out.println("item " + i + " buy: " + buy + " sell: " +
			// sell);
			if ((buy != -1) && (sell != -1)) {
				BuyMap.put(i, buy);
				SellMap.put(i, sell);
				NameMap.put(i, name);
			}
			i++;
		}
	}

	public boolean isForSale(int i) {
		return NameMap.containsKey(i);
	}

	public int getBuyPrice(int i) throws Exception {
		if (NameMap.containsKey(i)) {
			return BuyMap.get(i);
		} else
			throw new Exception();
	}

	public int getSellPrice(int i) throws Exception {
		if (NameMap.containsKey(i)) {
			return SellMap.get(i);
		} else
			throw new Exception();
	}

	public void setPrice(String item, String b, String s) throws Exception {
		Material i = null;
		if (Material.matchMaterial(item.toUpperCase()) == null)
			throw new Exception();
		i = Material.matchMaterial(item.toUpperCase());
		if ((Integer.parseInt(b) >= 0) && (Integer.parseInt(s) >= 0)) {
			if (NameMap.containsKey(i)) {
				BuyMap.remove(i);
				SellMap.remove(i);
				NameMap.remove(i);
			}
			BuyMap.put(i.getId(), Integer.parseInt(b));
			SellMap.put(i.getId(), Integer.parseInt(s));
			NameMap.put(i.getId(), i.name());
			//System.out.println("let's see: " + BuyMap.get(i.getId()) + " ... "
			//		+ SellMap.get(i.getId()));
		}
		save();
	}

	public void remove(String s) throws Exception {
		Material i = Material.getMaterial(s.toUpperCase());
		if (NameMap.containsKey(i.getId())) {
			BuyMap.remove(i.getId());
			SellMap.remove(i.getId());
			NameMap.remove(i.getId());
		}
		save();
	}

	private void save() {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(PLfile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			// FileWriter always assumes default encoding is OK!
			output.write("prices:");
			output.newLine();
			for (int i = 0; i < 2280; i++) {
				if (BuyMap.containsKey(i)) {
					output.write("  item" + String.valueOf(i) + ":");
					output.newLine();
					output.write("    name: " + NameMap.get(i).toLowerCase());
					output.newLine();
					output.write("    buy: " + BuyMap.get(i));
					output.newLine();
					output.write("    sell: " + SellMap.get(i));
					output.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
