package de.uni_koeln.spinfo.stocknews.stocks.data;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public class StockTable {
	
	/**
	 * A map of StockValueCore by LocalDate as keys
	 */
	public TreeMap<LocalDate, StockValueCore> stockTable;
	
	public StockTable(List<StockValueExtended> quotes){
		stockTable = new TreeMap<LocalDate,StockValueCore>();
		
		for(StockValueExtended quote : quotes){
			stockTable.put(quote.getDate(), new StockValueCore(quote));
		}
	}
	

}
