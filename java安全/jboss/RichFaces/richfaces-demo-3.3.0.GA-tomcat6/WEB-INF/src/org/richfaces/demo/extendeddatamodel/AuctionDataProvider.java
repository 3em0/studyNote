package org.richfaces.demo.extendeddatamodel;

import java.util.ArrayList;
import java.util.List;

import org.richfaces.demo.common.RandomDataHelper;

public class AuctionDataProvider {
	
	private String allDescriptions[] = {
			"Digital temperature sensor IC LM75A SOIC8 +Free adapter",
			"MC34063 Adjustable Power Supply DC-DC Converters 5x",
			"100x Ultra Bright White LEDs, 5mm, Clear",
			"100x Ultra Bright Blue LEDs, 5mm, Clear",
			"100x Ultra Bright Yellow LEDs, 5mm, Clear",
			"100x Ultra Bright Red LEDs, 5mm, Clear",
			"High quality Universal Programmer Development Board",
			"Lot of 10pcs 8x8 dot-matrix 3mm dia LED display bicolor",
			"100x Ultra Bright Blue LEDs, 5mm, Clear",
			"40pcs 74HC164 165 573 595 Shift register & latch IC kit",
			"Electric Nail Manicure Drill File 4 Acrylic Polish Nail",
			"LE-DS007 100x Ultra Bright VIOLET ( UV ) LED LEDs, 5mm",
			"LE-DS007 100x Ultra Bright VIOLET ( UV ) LED LEDs, 5mm",
			"Fantastic High brightness Cluster with 8pcs LED (Green)",
			"100x Ultra Bright Yellow LEDs, 5mm, Clear",
			"High brightness 1 pcs 3W 80 lm Lumen LED White",
			"2X ATMEL ATMEGA128-16AU Microcontroller and 2x 64-TQFP",
			"20pcs IRF530 & IRF9630 power mosfet kit",
			"Electrolytic Capacitors Radial SMD SMT assorted kit",
			"100 pcs Ultra Bright Mixed LEDs, 5mm, Clear",
			"Lot of 80 pcs 8 values (1uH~1mH) color wheel inductors",
			"Lot of 100pcs 4 values (4.7uH~220uH) DIP fixed inductor",
			"10pcs 8x8 dot-matrix 3mm dia LED display bicolor",
			"(SMD 0805) 50 Value Resistors + 32 Value Capacitors Kit",
			"Fantastic High brightness Cluster with 24pcs LED (Red)",
			"100x Ultra Bright Green LEDs, 5mm, Clear",
			"100x Ultra Bright Blue LEDs, 5mm, Clear",
			"Double-row Straight 20x male and 10x female pin header",
			"NEW 30pcs HEAT SINKS ,50pcs insulation bushing and film",
			"0.2% Class A Platinum Resistance Thermometers PT100"
	};
	private List<AuctionItem> allItems = null;
	private static final int VOLUME = 200;
	
	private synchronized void initData() {
		List<AuctionItem> data = new ArrayList<AuctionItem>();
		for (int counter=0; counter<VOLUME; counter++) {
			AuctionItem item = new AuctionItem(new Integer(counter));
			item.setDescription((String)RandomDataHelper.random(allDescriptions));
			item.setHighestBid(new Double(RandomDataHelper.random(10, 100)));
			item.setQtyAvialable(new Integer(RandomDataHelper.random(1, 20)));
			data.add(item);
			
		}
		allItems = data;
	}

	public List<AuctionItem> getAllItems() {
		if (allItems!=null && allItems.size()>0) {
			return allItems;
		} else {
			initData();
			return allItems;
		}
	}
	
	public AuctionItem getAuctionItemByPk(Integer pk) {
		for (AuctionItem item:getAllItems()) {
			if (item.getPk().equals(pk)) {
				return item;
			}
		}
		throw new RuntimeException("Auction Item pk="+pk.toString()+" not found");
	}
	public boolean hasAuctionItemByPk(Integer pk) {
		for (AuctionItem item:getAllItems()) {
			if (item.getPk().equals(pk)) {
				return true;
			}
		}
		return false;
		
	}
	
	public List<AuctionItem> getItemsByrange(Integer startPk, int numberOfRows, String sortField, boolean ascending) {
		List<AuctionItem> ret = new ArrayList<AuctionItem>();
		for (int counter=0; counter<numberOfRows; counter++) {
			ret.add(getAllItems().get(startPk.intValue()+counter));
		}
		return ret;
	}
	
	public void update() {
		// nothing need to do
	}
	
	public int getRowCount() {
		return getAllItems().size();
	}
}
