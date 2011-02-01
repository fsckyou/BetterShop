package com.bukkit.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.bukkit.util.config.*;

public class BetterShopPriceList {
	private static final Map<Integer, Integer> BuyMap = new HashMap<Integer, Integer>();
	private static final Map<Integer, Integer> SellMap = new HashMap<Integer, Integer>();
	private final File PLfile = new File("BetterShop", "PriceList.yml");
	private final Configuration PriceList = new Configuration(PLfile);
	private final List<Integer> BuySell = new ArrayList<Integer>();

	// Will open, read, and write to an item price list flatfile.
	public void load() {
		int i = 1;
		clearBuySell();
		try {
			PriceList.load();
		} catch (Exception e) {

		}
		BuyMap.clear();
		SellMap.clear();
		while (i < 2280) {

			try {
				PriceList.getIntList("Prices." + i, BuySell);
			} catch (Exception e) {

			}
			if ((BuySell.get(0) != 0) || (BuySell.get(1) != 0)) {
				BuyMap.put(i, BuySell.get(0));
				SellMap.put(i, BuySell.get(1));
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

	public void setPrice(int i, int b, int s) {
		//TODO implement setPrice		
		save();
	}

	private void clearBuySell() {
		BuySell.set(0, 0);
		BuySell.set(0, 0);
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
			output.write("Prices:");
			output.newLine();
			for (int i = 0; i < BuyMap.size(); i++) {
				if (BuyMap.containsKey(i)) {
					output.write("	" + i + ":");
					output.newLine();
					output.write("		Buy:");
					output.newLine();
					output.write("			- "+BuyMap.get(i));
					output.newLine();
					output.write("		Sell:");
					output.newLine();
					output.write("			- " + SellMap.get(i));
					output.newLine();
				}
			}
		} catch (IOException e) {
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
