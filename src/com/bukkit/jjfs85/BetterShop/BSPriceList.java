package com.bukkit.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.util.config.*;

public class BSPriceList {
	private final static Logger logger = Logger.getLogger("Minecraft");
	final Map<Integer, Integer> BuyMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> SellMap = new HashMap<Integer, Integer>();
	final Map<Integer, String> NameMap = new HashMap<Integer, String>();
	final List<String> keys = new LinkedList<String>();
	private File PLfile;
	private Configuration PriceList;

	public void load(File PLpath, String fileName) throws IOException {
		int i = 0;
		PLfile = new File(PLpath, fileName);
		if (!PLfile.getParentFile().exists()) {
			logger.info("Creating " + PLpath.getAbsolutePath());
			PLpath.mkdirs();
		}
		if (!PLfile.exists()) {
			logger.info("Creating " + PLfile.getAbsolutePath());
			PLfile.createNewFile();
		}
		logger.info("Loading PriceList.yml");
		PriceList = new Configuration(PLfile);
		PriceList.load();
		logger.info("PriceList.yml loaded.");
		BuyMap.clear();
		SellMap.clear();
		NameMap.clear();

		try {
			keys.addAll(PriceList.getKeys("prices"));
		} catch (Exception e0) {
			logger.info("Empty PriceList");
			return;
		}
		while (i < keys.size()) {
			int buy = -1;
			int sell = -1;
			String name = "Unk";
			String[] split = keys.get(i).split("[^0-9]");
			if (split.length != 0) {
				int id = Integer.parseInt(split[split.length-1]);
				if (keys.contains("item" + String.valueOf(id))) {
					buy = PriceList.getInt("prices.item" + String.valueOf(id)
							+ ".buy", -1);
					sell = PriceList.getInt("prices.item" + String.valueOf(id)
							+ ".sell", -1);
					name = PriceList.getString("prices.item"
							+ String.valueOf(id) + ".name", "Unk");
				}
				if ((buy != -1) && (sell != -1)) {
					BuyMap.put(id, buy);
					SellMap.put(id, sell);
					NameMap.put(id, name);
				}
			}
			i++;
		}
	}

	public boolean isForSale(String s) throws Exception {
		int i;
		i = itemDb.get(s).getItemTypeId();
		return isForSale(i);
	}

	public boolean isForSale(int i) {
		return NameMap.containsKey(i);
	}

	public int getBuyPrice(String s) throws Exception {
		int i;
		i = itemDb.get(s).getItemTypeId();
		return getBuyPrice(i);
	}

	public int getBuyPrice(int i) throws Exception {
		if (NameMap.containsKey(i)) {
			return BuyMap.get(i);
		} else
			throw new Exception();
	}

	public int getSellPrice(String s) throws Exception {
		int i;
		i = itemDb.get(s).getItemTypeId();
		return getSellPrice(i);
	}

	public int getSellPrice(int i) throws Exception {
		if (NameMap.containsKey(i)) {
			return SellMap.get(i);
		} else
			throw new Exception();
	}

	public void setPrice(String item, String b, String s) throws Exception {
		MaterialData mat = null;
		mat = itemDb.get(item); // check if it's in items.db
		int i = mat.getItemTypeId();
		if ((Integer.parseInt(b) >= 0) && (Integer.parseInt(s) >= 0)) {
			if (NameMap.containsKey(i)) {
				BuyMap.remove(i);
				SellMap.remove(i);
				NameMap.remove(i);
			}
			BuyMap.put(i, Integer.parseInt(b));
			SellMap.put(i, Integer.parseInt(s));
			NameMap.put(i, itemDb.getName(i, mat.getData()));
		}
		save();
	}

	public void remove(String s) throws Exception {
		MaterialData matdat = itemDb.get(s);
		Material mat = matdat.getItemType();
		if (NameMap.containsKey(mat.getId())) {
			BuyMap.remove(mat.getId());
			SellMap.remove(mat.getId());
			NameMap.remove(mat.getId());
		}
		save();
	}

	private void save() {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(PLfile));
		} catch (IOException e1) {
			logger.warning("Cannot write to " + PLfile.getName());
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
		} catch (Exception e) {
			logger.warning("Cannot write to " + PLfile.getName());
			e.printStackTrace();
		} finally {
			try {
				output.flush();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
