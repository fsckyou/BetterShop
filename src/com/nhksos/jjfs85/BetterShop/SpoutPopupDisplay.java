/**
 * Programmer: Jacob Scott
 * Program Name: SpoutPopupDisplay
 * Description:
 * Date: Sep 1, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.Item.JItem;
import com.jascotty2.Item.JItemDB;
import com.jascotty2.Item.PriceListItem;
import com.jascotty2.bukkit.MinecraftChatStr;
import com.jascotty2.io.CheckInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericItemWidget;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericSlider;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.InGameHUD;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.gui.Slider;
import org.getspout.spoutapi.gui.TextField;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * @author jacob
 */
public class SpoutPopupDisplay {

	static Map<SpoutPlayer, SpoutPopupDisplay> popupOpen = new HashMap<SpoutPlayer, SpoutPopupDisplay>();
	protected static int rows = 7, cols = 3,
			height = 160, width = 420;
	protected static final int MAX_WIDTH = 427, MAX_HEIGHT = 240;
	SpoutPlayer player;
	String popupUUID = null;
	// gui objects
	GenericButton btnExit = new GenericButton(),
			btnAbout = new GenericButton();
	//GenericContainer items = new GenericContainer();
	GenericSlider itemScroll = new GenericSlider();
	GenericButton btnScrollLeft = new GenericButton(),
			btnScrollRight = new GenericButton();
	GenericLabel lblPageNum = new GenericLabel();
	GenericPopup popup = new GenericPopup();
	List<MarketMenuItem> menuItems = new ArrayList<MarketMenuItem>();
	int currentPage = 0;
	int xpos[] = new int[cols];
	MarketItem itemDetail;// = new MarketItem();
	boolean aboutActive = false;

	public SpoutPopupDisplay(SpoutPlayer p) {
		player = p;
		itemDetail = new MarketItem(p);
	} // end default constructor

	public static void popup(SpoutPlayer p, ScreenType scr) {
		if (popupOpen.containsKey(p)) {
			closePopup(p);
			return;
		}
		if ((scr != null)
				&& ((scr == ScreenType.GAME_SCREEN)
				|| (scr == ScreenType.PLAYER_INVENTORY)
				|| (scr == ScreenType.DISPENSER_INVENTORY)
				|| (scr == ScreenType.FURNACE_INVENTORY)
				|| (scr == ScreenType.WORKBENCH_INVENTORY)
				|| (scr == ScreenType.CUSTOM_SCREEN))) {
			SpoutPopupDisplay d = new SpoutPopupDisplay(p);
			//System.out.println(p.getMainScreen().getHeight() + " x " + p.getMainScreen().getWidth());
			popupOpen.put(p, d);
			d.show();
		}

	}

	public static void closePopup(SpoutPlayer p) {
		if (popupOpen.containsKey(p)) {
			SpoutPopupDisplay d = popupOpen.get(p);
			if (d.aboutActive) {
				d.popup.removeWidgets(BetterShop.bettershopPlugin);
				d.show();
				d.aboutActive = false;
			} else {
				p.getMainScreen().closePopup();
				d.close();
				popupOpen.remove(p);
			}
		}
	}

	public static SpoutPopupDisplay getPopup(SpoutPlayer p) {
		return popupOpen.get(p);
	}

	public void close() {
		player = null;
		btnExit = null;
	}

	public void show() {

		InGameHUD hudscreen = player.getMainScreen();

		popup.setVisible(true);

		//popup.getId().toString();

		//Exit Button
		btnExit.setText("EXIT").setWidth(45).setHeight(15).setX(378).setY(222);
		popup.attachWidget(BetterShop.bettershopPlugin, btnExit);

		//"About" :)
		btnAbout.setText("?").setWidth(12).setHeight(12).setX(MAX_WIDTH - 2).setY(MAX_HEIGHT - 2);
		popup.attachWidget(BetterShop.bettershopPlugin, btnAbout);

		//items.setHeight(350).setWidth(200).setX(10).setY(5);

		try {
			int row = 0, col = 0;
//			int wid = (width - xPad * cols) / cols,
//					hgt = (height - yPad * rows) / rows;
			int yPad = (height - MarketMenuItem.DEF_HEIGHT * rows) / (rows + 1),
					xPad = (MAX_WIDTH - MarketMenuItem.DEF_WIDTH * cols) / (cols + 1);
			boolean vis = true;
			for (PriceListItem p : BetterShop.pricelist.getPricelistItems(
					BSutils.hasPermission(player, BSutils.BetterShopPermission.ADMIN_ILLEGAL))) {
				MarketMenuItem m = new MarketMenuItem(p.ID(), p.Data());
				int x = xPad * (col + 1) + m.getWidth() * col,
						y = yPad * (row + 1) + m.getHeight() * row;
				m.setVisible(vis).setY(y).setX(x);//.setWidth(wid).setHeight(hgt);
				m.marketButton.setEnabled(vis);
				menuItems.add(m);
				//items.addChild(m);
				popup.attachWidget(BetterShop.bettershopPlugin, m);
				if (++row >= rows) {
					if (vis) {
						xpos[col] = x;
					}
					if (++col >= cols) {
						vis = false;
						col = cols;
					}
					row = 0;
				}
			}

			//popup.attachWidget(BetterShop.bettershopPlugin, items);

			// GenericSlider is not vertical :(
			//itemScroll.setHeight(350).setWidth(5).setX(100);
			itemScroll.setHeight(8).setWidth(width - 20).setY(height).setX((MAX_WIDTH - (width - 20)) / 2);
			itemScroll.setSliderPosition(0);

			btnScrollLeft.setText("<").setHeight(10).setWidth(10).setY(height).setX(2).setVisible(false);
			btnScrollRight.setText(">").setHeight(10).setWidth(10).setY(height).setX(MAX_WIDTH - 12);

			lblPageNum.setHeight(7).setWidth(40).setY(height + 11).setX(MAX_WIDTH - 75);
			lblPageNum.setText("Page 1 of " + (int) Math.ceil(((float) menuItems.size() / rows) - 2));
			lblPageNum.setTextColor(new Color(.65F, .65F, .65F));

			itemDetail.setX(2).setY(height + 15);

			popup.attachWidget(BetterShop.bettershopPlugin, itemScroll);
			popup.attachWidget(BetterShop.bettershopPlugin, btnScrollLeft);
			popup.attachWidget(BetterShop.bettershopPlugin, btnScrollRight);
			popup.attachWidget(BetterShop.bettershopPlugin, lblPageNum);
			popup.attachWidget(BetterShop.bettershopPlugin, itemDetail);
		} catch (Exception e) {
			popup.attachWidget(BetterShop.bettershopPlugin,
					new GenericLabel().setText("Error Loading Pricelist! \n  "
					+ e.getMessage()).setTextColor(new Color(240 / (float) 255, 45 / (float) 255, 45 / (float) 255)).setX(150).setY(100));
		}

		hudscreen.attachPopupScreen(popup);
		hudscreen.updateWidget(popup);
		hudscreen.setDirty(true);
	}

	protected void showAbout() {
		aboutActive = true;
		//player.getMainScreen().closePopup();

		popup.removeWidgets(BetterShop.bettershopPlugin);

		popup.attachWidget(BetterShop.bettershopPlugin, btnExit);

		GenericLabel lblTitle = new GenericLabel();
		lblTitle.setText("About").setX((MAX_WIDTH - 45) / 2).setY(10).setWidth(45).setHeight(30);
		popup.attachWidget(BetterShop.bettershopPlugin, lblTitle);

		GenericLabel lblAbout = new GenericLabel();
		lblAbout.setX((MAX_WIDTH - 200) / 2).setY((MAX_HEIGHT - 100) / 2).setWidth(200).setHeight(100);
		String about = "BetterShop " + BetterShop.bettershopPlugin.getDescription().getVersion() + "\n"
				+ "Coding by Jacob Scott (jascotty2) \n"
				+ "https://github.com/jascotty2/BetterShop";
		String lines[] = about.split("\n");
		StringBuilder txt = new StringBuilder();
		for (int i = 0; i < lines.length;) {
			txt.append(MinecraftChatStr.strPadCenterChat(lines[i], 210, ' '));
			if (++i < lines.length) {
				txt.append("\n");
			}
		}
		lblAbout.setText(txt.toString());

		popup.attachWidget(BetterShop.bettershopPlugin, lblAbout);

		InGameHUD hudscreen = player.getMainScreen();
		hudscreen.attachPopupScreen(popup);
		hudscreen.updateWidget(popup);
		hudscreen.setDirty(true);
	}

	public void buttonPress(Button btn) {
		if (btn == btnExit) {
			if (aboutActive) {
				popup.removeWidgets(BetterShop.bettershopPlugin);
				show();
				aboutActive = false;
			} else {
				closePopup(player);
			}
		} else if (btn == btnAbout) {
			showAbout();
		} else if (btn == btnScrollLeft) {
			itemScroll.setSliderPosition((float) (currentPage * rows) / (menuItems.size() - (rows * (cols - 1)))).setDirty(true);
			sliderChanged(itemScroll);
		} else if (btn == btnScrollRight) {
			itemScroll.setSliderPosition((float) ((currentPage + 1.5) * rows) / (menuItems.size() - (rows * (cols - 1)))).setDirty(true);
			sliderChanged(itemScroll);
		} else if (btn == itemDetail.btnUp) {
			itemDetail.buttonUpPressed(1);
		} else if (btn == itemDetail.btnDown) {
			itemDetail.buttonDownPressed(1);
		} else if (btn == itemDetail.btnUp5) {
			itemDetail.buttonUpPressed(5);
		} else if (btn == itemDetail.btnDown5) {
			itemDetail.buttonDownPressed(5);
		} else if (btn == itemDetail.btnUp20) {
			itemDetail.buttonUpPressed(20);
		} else if (btn == itemDetail.btnDown20) {
			itemDetail.buttonDownPressed(20);
		} else if (btn == itemDetail.btnBuy) {
			if (itemDetail.buyAmt() > 0) {
				player.performCommand("shopbuy " + itemDetail.itemIDD() + " " + itemDetail.buyAmt());
				closePopup(player);
			}
		} else if (btn == itemDetail.btnSell) {
			if (itemDetail.sellAmt() > 0) {
				player.performCommand("shopsell " + itemDetail.itemIDD() + " " + itemDetail.sellAmt());
				closePopup(player);
			}
		} else {
			for (MarketMenuItem m : menuItems) {
				if (btn == m.marketButton) {
					itemDetail.updateItem(m.itemId, m.itemData);
					break;
				}
			}
		}
	}

	public void sliderChanged(Slider scrollbar) {
		if (scrollbar == itemScroll) {
			int page = (int) Math.ceil((((float) menuItems.size() / rows) - (cols - 1)) * scrollbar.getSliderPosition()) - 1;
			if(page < 0){
				page = 0;
			}
			if (currentPage != page) {
				for (int i = 0; i < menuItems.size(); ++i) {
					MarketMenuItem m = menuItems.get(i);
					int p = i / rows;
					if (p >= page && p < page + cols) {
						m.setVisible(true);
						m.setX(xpos[p - page]);
						m.marketButton.setEnabled(true);
					} else {
						m.setVisible(false);
						m.marketButton.setEnabled(false);
					}
					m.setDirty(true);
				}
				currentPage = page;

				lblPageNum.setText("Page " + (currentPage + 1) + " of "
						+ (int) (Math.ceil((float) menuItems.size() / rows) - 2)).setDirty(true);

				if (currentPage == 0) {
					if (btnScrollLeft.isVisible()) {
						btnScrollLeft.setVisible(false).setDirty(true);
					}
				} else if (!btnScrollLeft.isVisible()) {
					btnScrollLeft.setVisible(true).setDirty(true);
				}
				if (currentPage >= ((menuItems.size() / rows) - (cols - 1))) {
					if (btnScrollRight.isVisible()) {
						btnScrollRight.setVisible(false).setDirty(true);
					}
				} else if (!btnScrollRight.isVisible()) {
					btnScrollRight.setVisible(true).setDirty(true);
				}
			}
		}
	}

	public void textChanged(TextFieldChangeEvent event) {
		TextField t = event.getTextField();
		if (t == itemDetail.txtAmt) {
			itemDetail.amtChanged(event.getOldText(), event.getNewText());
		}
	}

	public SpoutPlayer getPlayer() {
		return player;
	}

	public String getPopupUUID() {
		return popupUUID;
	}
} // end class SpoutPopupDisplay

class MarketMenuItem extends GenericContainer {

	public static int DEF_WIDTH = 120, DEF_HEIGHT = 17;
	GenericButton marketButton = new GenericButton();
	GenericItemWidget item = new GenericItemWidget();
	//GenericLabel lblName = new GenericLabel();
	int itemId;
	byte itemData;

	public MarketMenuItem(int id) {
		this(id, (byte) 0);
	}

	public MarketMenuItem(int id, byte dat) {
		itemId = id;
		itemData = dat;
		JItem j = JItemDB.GetItem(id, dat);
		this.setWidth(DEF_WIDTH).setHeight(DEF_HEIGHT);//.setFixed(true).setAnchor(WidgetAnchor.CENTER_LEFT);
		marketButton.setHeight(height).setWidth(width - 19);
		marketButton.setText(j != null ? j.Name() : String.valueOf(id)).setX(19).setY(height / 10);
		if (j != null && j.IsValidItem()) {
			item.setTypeId(id).setData(dat);
		} else {
			item.setTypeId(0);
		}
		item.setWidth(8).setHeight(8).setX(1);
		//super(marketButton, item);
		this.children.add(marketButton);
		this.children.add(item);
//		for (Widget child : children) {
//			child.setContainer(this);
//		}
	}
}

class MarketItem extends GenericContainer {

	int itemId;
	byte itemData;
	PriceListItem price;
	long stock;
	Player player;
	GenericItemWidget item = new GenericItemWidget();
	GenericLabel lblName = new GenericLabel();
	GenericLabel lblCash = new GenericLabel(),
			lblBuy = new GenericLabel(),
			lblSell = new GenericLabel();
	GenericTextField txtAmt = new GenericTextField();
	GenericLabel lblAmt = new GenericLabel();
	GenericButton btnUp = new GenericButton(),
			btnDown = new GenericButton(),
			btnUp5 = new GenericButton(),
			btnDown5 = new GenericButton(),
			btnUp20 = new GenericButton(),
			btnDown20 = new GenericButton();
	int currentAmt = 1, maxBuyAmt, maxSellAmt;
	GenericButton btnBuy = new GenericButton(),
			btnSell = new GenericButton();
	GenericLabel lblBuyBtn = new GenericLabel(),
			lblSellBtn = new GenericLabel();

	public MarketItem(Player pl) {
		player = pl;
		this.setWidth(300).setHeight(60).setMargin(2);
		item.setDepth(16).setWidth(16).setHeight(16).setX(10).setY(10);
		lblName.setWidth(100).setHeight(20).setX(38).setY(3);

		lblBuy.setWidth(100).setHeight(10).setX(150).setY(2);
		lblSell.setWidth(100).setHeight(10).setX(150).setY(17);

		lblCash.setWidth(110).setHeight(20).setX(SpoutPopupDisplay.MAX_WIDTH - 110).setY(5);
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

		lblAmt.setVisible(false);
		txtAmt.setVisible(false);
		btnUp.setVisible(false);
		btnDown.setVisible(false);
		btnUp5.setVisible(false);
		btnDown5.setVisible(false);
		btnUp20.setVisible(false);
		btnDown20.setVisible(false);
		btnBuy.setVisible(false);
		btnSell.setVisible(false);

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
//	public MarketItem(MarketMenuItem item) {
//		this(item.itemId, item.itemData);
//	}
//
//	public MarketItem(int id, byte dat) {
//		this();
//		updateItem(id, dat);
//	}

	public final void updateCash() {
		lblCash.setText("  Cash: \n" + BSutils.formatCurrency(BSutils.playerBalance(player))).setDirty(true);
		updateItem();
	}

	public final void updateItem() {
		if (itemId > 0) {
			updateItem(itemId, itemData);
		}
	}

	public final void updateItem(int id, byte dat) {
		itemId = id;
		itemData = dat;
		price = null;

		JItem j = JItemDB.GetItem(id, dat);
		if (j != null && j.IsValidItem()) {
			item.setTypeId(id).setData(dat);
		} else {
			item.setTypeId(0);
		}
		item.setDirty(true);

		lblName.setText(j != null ? j.Name() : String.valueOf(id)).setDirty(true);

		try {
			price = BetterShop.pricelist.getItemPrice(id, dat);
			stock = BetterShop.stock.freeStockRemaining(id, dat);
			maxBuyAmt = BSutils.amtCanBuy(player, j);
			maxSellAmt = BSutils.amtHas(player, j);
			if (BetterShop.config.useItemStock) {
				lblName.setText(lblName.getText() + "\n\n" + (stock < 0 ? "INF" : stock) + " in Stock");
			}

			lblBuy.setText("Buy Price: " + BSutils.formatCurrency(price.buy)).setDirty(true);
			lblSell.setText("Sell Price: " + BSutils.formatCurrency(price.sell)).setDirty(true);

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
			if (currentAmt >= maxBuyAmt || currentAmt >= maxSellAmt) {
				currentAmt = maxBuyAmt > maxSellAmt ? maxBuyAmt : maxSellAmt;
			}
			setAmt(currentAmt);//maxBuyAmt == 0 ? (maxSellAmt == 0 ? 0 : 1) : 1);
		} catch (Exception ex) {
			BetterShop.Log(Level.SEVERE, ex);
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
				+ BSutils.formatCurrency(price.buy * buyAmt())).setDirty(true);
		lblSellBtn.setText("Sell " + sellAmt() + " for\n "
				+ BSutils.formatCurrency(price.sell * sellAmt())).setDirty(true);
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
}
