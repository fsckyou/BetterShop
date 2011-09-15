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

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import me.jascotty2.bettershop.BSEcon;
import me.jascotty2.bettershop.BSutils;
import me.jascotty2.bettershop.BetterShop;
import me.jascotty2.bettershop.spout.SpoutPopupDisplay;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import me.jascotty2.lib.bukkit.item.JItem;
import me.jascotty2.lib.bukkit.item.JItemDB;
import me.jascotty2.lib.io.CheckInput;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericItemWidget;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MarketItemDetail extends GenericContainer {

	protected static final int MAX_WIDTH = 427, MAX_HEIGHT = 240;
	
	int itemId;
	byte itemData;
	//PriceListItem price;
	double buyPrice, sellPrice;
	long stock;
	Player player;
	GenericItemWidget item = new GenericItemWidget();
	GenericLabel lblName = new GenericLabel();
	GenericLabel lblCash = new GenericLabel(),
			lblBuy = new GenericLabel(),
			lblSell = new GenericLabel();
	GenericLabel lblAmt = new GenericLabel();
	public final GenericTextField txtAmt = new GenericTextField();
	public final GenericButton btnUp = new GenericButton(),
			btnDown = new GenericButton(),
			btnUp5 = new GenericButton(),
			btnDown5 = new GenericButton(),
			btnUp20 = new GenericButton(),
			btnDown20 = new GenericButton();
	int currentAmt = 1, maxBuyAmt, maxSellAmt;
	public final GenericButton btnBuy = new GenericButton(),
			btnSell = new GenericButton();
	GenericLabel lblBuyBtn = new GenericLabel(),
			lblSellBtn = new GenericLabel();

	public MarketItemDetail(Player pl) {
		player = pl;
		this.setWidth(300).setHeight(70).setMargin(2);
		item.setDepth(16).setWidth(16).setHeight(16).setX(10).setY(5);
		lblName.setWidth(100).setHeight(20).setX(38).setY(3);

		lblBuy.setWidth(100).setHeight(10).setX(150).setY(3);
		lblSell.setWidth(100).setHeight(10).setX(150).setY(18);

		lblCash.setWidth(110).setHeight(20).setX(MAX_WIDTH - 110).setY(8);
		updateCash();

		txtAmt.setX(8).setY(height - 10).setWidth(30).setHeight(12);
		txtAmt.setFieldColor(new Color(80 / 255F, 80 / 255F, 80 / 255F));
		txtAmt.setBorderColor(new Color(.8F, .8F, .8F));
		txtAmt.setColor(new Color(.9F, .9F, .9F));

		lblAmt.setText("Amount: ").setWidth(55).setHeight(10).setX(3).setY(height - 22);

		btnUp.setText("+").setX(42).setY(height - 15).setWidth(12).setHeight(9);
		btnDown.setText("-").setX(42).setY(height - 5).setWidth(12).setHeight(9);
		btnUp5.setText("+5").setX(btnUp.getX() + 15).setY(btnUp.getY()).setWidth(20).setHeight(9);
		btnDown5.setText("-5").setX(btnDown.getX() + 15).setY(btnDown.getY()).setWidth(20).setHeight(9);
		btnUp20.setText("+20").setX(btnUp5.getX() + 22).setY(btnUp.getY()).setWidth(22).setHeight(9);
		btnDown20.setText("-20").setX(btnDown5.getX() + 22).setY(btnDown.getY()).setWidth(22).setHeight(9);

		btnBuy.setX(120).setY(height - 26).setWidth(110).setHeight(28);
		btnSell.setX(245).setY(height - 26).setWidth(110).setHeight(28);

		lblBuyBtn.setX(btnBuy.getX() + 5).setY(btnBuy.getY() + 5).setWidth(btnBuy.getWidth() - 10).setHeight(btnBuy.getHeight() - 10).setPriority(RenderPriority.Low);
		lblSellBtn.setX(btnSell.getX() + 5).setY(btnSell.getY() + 5).setWidth(btnSell.getWidth() - 10).setHeight(btnSell.getHeight() - 10).setPriority(RenderPriority.Lowest);

		setVisible(false);

		this.children.add(item);
		this.children.add(lblName);
		this.children.add(lblBuy);
		this.children.add(lblSell);
		this.children.add(lblCash);
		this.children.add(txtAmt);
		this.children.add(lblAmt);
		this.children.add(btnUp);
		this.children.add(btnDown);
		this.children.add(btnUp5);
		this.children.add(btnDown5);
		this.children.add(btnUp20);
		this.children.add(btnDown20);
		this.children.add(btnBuy);
		this.children.add(btnSell);
		this.children.add(lblBuyBtn);
		this.children.add(lblSellBtn);
	}
//
//	public MarketItemDetail(LargeMarketMenuItem item) {
//		this(item.itemId, item.itemData);
//	}
//
//	public MarketItemDetail(int id, byte dat) {
//		this();
//		updateItem(id, dat);
//	}

	@Override
	public final Container setVisible(boolean vis){
		
		lblAmt.setVisible(vis);
		txtAmt.setVisible(vis);

		lblName.setVisible(vis);

		lblBuy.setVisible(vis);
		lblSell.setVisible(vis);

		btnUp.setVisible(vis);
		btnDown.setVisible(vis);
		btnUp5.setVisible(vis);
		btnDown5.setVisible(vis);
		btnUp20.setVisible(vis);
		btnDown20.setVisible(vis);

		btnBuy.setVisible(vis);
		btnSell.setVisible(vis);
		
		lblBuyBtn.setVisible(vis);
		lblSellBtn.setVisible(vis);

		this.setDirty(true);
		return this;
	}
	
	public final void updateCash() {
		lblCash.setText("  Cash: \n" + BSEcon.format(BSEcon.getBalance(player))).setDirty(true);
		updateItem();
	}

	public final void updateItem() {
		if (itemId > 0) {
			updateItem(itemId, itemData);
		}
	}

	public final void updateItem(int id, byte dat) {
		if(!this.isVisible()){
			this.setVisible(true).setDirty(true);
		}
		itemId = id;
		itemData = dat;
		//price = null;
		buyPrice = sellPrice = -1;

		JItem j = JItemDB.GetItem(id, dat);
		if (j != null && j.IsValidItem()) {
			item.setTypeId(id).setData(dat);
		} else {
			item.setTypeId(0);
		}
		item.setDirty(true);

		lblName.setText(j != null ? j.Name() : String.valueOf(id)).setDirty(true);

		try {
			//price = BetterShop.getPricelist().getItemPrice(id, dat);
			buyPrice = BetterShop.getPricelist(player.getLocation()).itemBuyPrice(player, id, dat, 1);
			sellPrice = BetterShop.getPricelist(player.getLocation()).itemSellPrice(player, id, dat, 1);
			stock = BetterShop.getStock(player.getLocation()).freeStockRemaining(id, dat);
			maxBuyAmt = BSutils.amtCanBuy(player, j);
			maxSellAmt = BSutils.amtHas(player, j);
			if (BetterShop.getConfig().useItemStock) {
				lblName.setText(lblName.getText() + "\n\n" + (stock < 0 ? "INF" : stock) + " in Stock");
			}

			lblBuy.setText("Buy Price: " + BSEcon.format(buyPrice)).setDirty(true);
			lblSell.setText("Sell Price: " + BSEcon.format(sellPrice)).setDirty(true);

			if (!lblAmt.isVisible()) {
				lblAmt.setVisible(true).setDirty(true);
				txtAmt.setVisible(true);
				btnUp.setVisible(true).setDirty(true);
				btnDown.setVisible(true).setDirty(true);
				btnUp5.setVisible(true).setDirty(true);
				btnDown5.setVisible(true).setDirty(true);
				btnUp20.setVisible(true).setDirty(true);
				btnDown20.setVisible(true).setDirty(true);
				btnBuy.setVisible(true).setDirty(true);
				btnSell.setVisible(true).setDirty(true);
			}
			if (currentAmt > maxBuyAmt && currentAmt > maxSellAmt) {
				currentAmt = maxBuyAmt > maxSellAmt ? maxBuyAmt : maxSellAmt;
			}
			setAmt(currentAmt);//maxBuyAmt == 0 ? (maxSellAmt == 0 ? 0 : 1) : 1);
		} catch (Exception ex) {
			BetterShopLogger.Log(Level.SEVERE, ex);
			SpoutPopupDisplay.closePopup((SpoutPlayer) player);
		}
	}

	public void buttonUpPressed(int d) {
		currentAmt += d;
		if (currentAmt > maxBuyAmt && currentAmt > maxSellAmt) {
			currentAmt = maxBuyAmt > maxSellAmt ? maxBuyAmt : maxSellAmt;
		}
		setAmt(currentAmt);
	}

	public void buttonDownPressed(int d) {
		currentAmt -= d;
		if (currentAmt < 0) {
			currentAmt = (maxBuyAmt == 0 && maxSellAmt == 0) ? 0 : 1;
		}
		setAmt(currentAmt);
	}

	public int buyAmt() {
		return currentAmt >= maxBuyAmt ? maxBuyAmt : currentAmt;
	}

	public int sellAmt() {
		return currentAmt >= maxSellAmt ? maxSellAmt : currentAmt;
	}

	public String itemIDD() {
		return String.valueOf(itemId) + (itemData == 0 ? "" : ":" + itemData);
	}

	public void amtChanged(String from, String to) {
		int min = maxBuyAmt == 0 ? (maxSellAmt == 0 ? 0 : 1) : 1;
		if (to.isEmpty()) {
			setAmt(min);
			//redirtyTxtAmt();
			return;
		}
		int newAmt = -1;
		if (CheckInput.IsInt(to) && (newAmt = CheckInput.GetInt(to, newAmt)) > min) {
			if (newAmt > maxBuyAmt) {
				setAmt(currentAmt = maxBuyAmt);
				redirtyTxtAmt();
			} else {
				setAmt(newAmt);
			}
		} else {
			setAmt(currentAmt);
			redirtyTxtAmt();
		}
	}

	protected void setAmt(int amt) {
		txtAmt.setText(String.valueOf(currentAmt = amt)).setDirty(true);
		txtAmt.setCursorPosition(txtAmt.getText().length());

//		if (btnUp.isEnabled() && currentAmt >= maxBuyAmt && currentAmt >= maxSellAmt) {
//			btnUp.setEnabled(false).setDirty(true);
//		} else if (!btnUp.isEnabled() && maxBuyAmt > 0 && maxSellAmt > 0) {
//			btnUp.setEnabled(true).setDirty(true);
//		}
//
//		if (btnDown.isEnabled() && currentAmt <= 1
//				&& maxBuyAmt <= currentAmt && maxSellAmt <= currentAmt) {
//			btnDown.setEnabled(false).setDirty(true);
//		} else if (!btnDown.isEnabled()
//				&& (currentAmt > 1 || (currentAmt > maxBuyAmt && currentAmt > maxSellAmt))) {
//			btnDown.setEnabled(true).setDirty(true);
//		}

		lblBuyBtn.setText("Buy " + buyAmt() + " for\n "
				+ BSEcon.format(buyPrice * buyAmt())).setDirty(true);
		lblSellBtn.setText("Sell " + sellAmt() + " for\n "
				+ BSEcon.format(sellPrice * sellAmt())).setDirty(true);
	}
	Timer t = null;

	private void redirtyTxtAmt() {
		if (t != null) {
			t.cancel();
			t = null;
		}
		t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				setAmt(currentAmt);
				t = null;
			}
		}, 1000);
	}
} // end class MarketItemDetail
