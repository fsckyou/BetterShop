package com.bukkit.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.bukkit.util.config.*;

public class BetterShopPriceList {
	private final Map<Integer, Integer> BuyMap = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> SellMap = new HashMap<Integer, Integer>();
	private final File PLfile = new File("plugins/BetterShop", "PriceList.yml");
	private final Configuration PriceList = new Configuration(PLfile);

	public void load() throws IOException {
		int i = 1;
		if (!PLfile.exists())
			PLfile.createNewFile();
		PriceList.load();
		BuyMap.clear();
		SellMap.clear();
		while (i < 2280) {
			int buy = -1;
			int sell = -1;
			try {
				itemDb.get(String.valueOf(i));
			} catch (Exception e) {
				i++;
				continue;
			}
			buy = PriceList.getInt("prices.item" + String.valueOf(i) + ".buy",
					-1);
			sell = PriceList.getInt(
					"prices.item" + String.valueOf(i) + ".sell", -1);
			// System.out.println("item " + i + " buy: " + buy + " sell: " +
			// sell);
			if ((buy != -1) && (sell != -1)) {
				BuyMap.put(i, buy);
				SellMap.put(i, sell);
			}
			i++;
		}

	}

	public int getBuyPrice(int i) throws Exception {
		if (BuyMap.containsKey(i)) {
			return BuyMap.get(i);
		} else
			throw new Exception();
	}

	public int getSellPrice(int i) throws Exception {
		if (SellMap.containsKey(i)) {
			return SellMap.get(i);
		} else
			throw new Exception();
	}

	public void setPrice(String item, String b, String s) throws Exception {
		int i = itemDb.get(item);
		if ((Integer.parseInt(b) >= 0) && (Integer.parseInt(s) >= 0)) {
			if (BuyMap.containsKey(i)) {
				BuyMap.remove(i);
				SellMap.remove(i);
			}
			BuyMap.put(i, Integer.parseInt(b));
			SellMap.put(i, Integer.parseInt(s));
			System.out.println("let's see: " + BuyMap.get(i) + " ... "
					+ SellMap.get(i));
		}
		save();
	}

	public void remove(String s) throws Exception {
		int i = itemDb.get(s);
		if (BuyMap.containsKey(i)){
			BuyMap.remove(i);
			SellMap.remove(i);
		}
		save();
	}

	private void save() {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(PLfile));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
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
					output.write("    name: " + itemDb.getName(i));
					output.newLine();
					output.write("    buy: " + BuyMap.get(i));
					output.newLine();
					output.write("    sell: " + SellMap.get(i));
					output.newLine();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
