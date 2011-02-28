package com.nhksos.jjfs85.BetterShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.material.MaterialData;
import org.bukkit.util.config.*;

public class BSPriceList {
	private final static Logger logger = Logger.getLogger("Minecraft");
	final Map<Double, Double> BuyMap = new HashMap<Double, Double>();
	final Map<Double, Double> SellMap = new HashMap<Double, Double>();
	final Map<Double, String> NameMap = new HashMap<Double, String>();
	Set<Double> ItemMap = new TreeSet<Double>();
	final List<String> keys = new LinkedList<String>();
	private File PLfile;
	private Configuration PriceList;
	
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}

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
			double buy = -1;
			double sell = -1;
			String name = "Unk";
			String[] split = keys.get(i).split("[^0-9]");
			if (split.length != 0) {
				int id = 0;
				int sub = 0;
				if (split[split.length - 1].equalsIgnoreCase("") != true)
					sub = Integer.parseInt(split[split.length - 1]);
				if (split[split.length - 4].equalsIgnoreCase("") != true)
					id = Integer.parseInt(split[split.length - 4]);
				if (keys.contains("item" + String.valueOf(id) + "sub"
						+ String.valueOf(sub))) {
					buy = PriceList.getDouble("prices.item"
							+ String.valueOf(id) + "sub" + String.valueOf(sub)
							+ ".buy", -1);
					sell = PriceList.getInt("prices.item" + String.valueOf(id)
							+ "sub" + String.valueOf(sub) + ".sell", -1);
					name = PriceList.getString("prices.item"
							+ String.valueOf(id) + "sub" + String.valueOf(sub)
							+ ".name", "Unk");
				} else if (keys.contains("item" + String.valueOf(id))) {
					buy = PriceList.getDouble("prices.item"
							+ String.valueOf(id) + ".buy", -1);
					sell = PriceList.getInt("prices.item" + String.valueOf(id)
							+ ".sell", -1);
					name = PriceList.getString("prices.item"
							+ String.valueOf(id) + ".name", "Unk");
				}
				if ((buy != -1) && (sell != -1)) {
					BuyMap.put(((double) id + (sub / 100)), buy);
					SellMap.put(((double) id + (sub / 100)), sell);
					NameMap.put(((double) id + (sub / 100)), name);
				}
			}
			i++;
		}
		ItemMap.clear();
		ItemMap.addAll(NameMap.keySet());
	}

	public boolean isForSale(String s) throws Exception {
		double i;
		i = itemDb.get(s).getItemTypeId() + (double)itemDb.get(s).getData() / 100;
		return isForSale(i);
	}

	public boolean isForSale(double i) {
		return NameMap.containsKey(i);
	}

	public double getBuyPrice(String s) throws Exception {
		double i;
		i = itemDb.get(s).getItemTypeId() + (double)itemDb.get(s).getData() / 100;
		return getBuyPrice(i);
	}

	public double getBuyPrice(double i) throws Exception {
		if (NameMap.containsKey(i)) {
			return BuyMap.get(i);
		} else
			throw new Exception();
	}

	public double getSellPrice(String s) throws Exception {
		double i;
		i = itemDb.get(s).getItemTypeId() + (double)itemDb.get(s).getData() / 100;
		return getSellPrice(i);
	}

	public double getSellPrice(double i) throws Exception {
		if (NameMap.containsKey(i)) {
			return SellMap.get(i);
		} else
			throw new Exception();
	}

	public void setPrice(String item, String b, String s) throws Exception {

		double i = itemDb.get(item).getItemTypeId()
				+ (double)itemDb.get(item).getData() / 100;
		// try to parse... hunt for exception...
		Double.parseDouble(b);
		Double.parseDouble(s);
		if (NameMap.containsKey(i)) {
			BuyMap.remove(i);
			SellMap.remove(i);
			NameMap.remove(i);
		}
		BuyMap.put(i, Double.parseDouble(b));
		SellMap.put(i, Double.parseDouble(s));
		NameMap.put(i, itemDb.getName(i));
		save();
	}

	public void remove(String s) throws Exception {
		MaterialData matdat = itemDb.get(s);
		if (NameMap
				.containsKey(matdat.getItemTypeId() + (double)matdat.getData() / 100)) {
			BuyMap.remove(matdat.getItemTypeId() + (double)matdat.getData() / 100);
			SellMap.remove(matdat.getItemTypeId() + (double)matdat.getData() / 100);
			NameMap.remove(matdat.getItemTypeId() + (double)matdat.getData() / 100);
		}
		save();
	}

	private void save() {
		BufferedWriter output = null;
		ItemMap.clear();
		ItemMap.addAll(NameMap.keySet());
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
			Iterator<Double> ItemIt = ItemMap.iterator();
			while (ItemIt.hasNext()) {
				double key = ItemIt.next();
				int item = (int) Math.floor(key);
				int sub = (int) ((key - item) * 100);
				output.write(String.format("  item%01dsub%01d:",item , sub));
				output.newLine();
				output.write("    name: " + NameMap.get(key).toLowerCase());
				output.newLine();
				output.write("    buy: " + BuyMap.get(key));
				output.newLine();
				output.write("    sell: " + SellMap.get(key));
				output.newLine();
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
