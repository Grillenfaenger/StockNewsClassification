package de.uni_koeln.spinfo.stocknews.stocks.data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CompanyStockTables {
	
	/**
	 * A map of StockTables by Company-RIC keys.
	 */
	public Map<String,StockTable> companyStocks; 
	
	public CompanyStockTables(){
		companyStocks = new HashMap<String,StockTable>();
	}
	
	public float courseRelativeToIndex(String ric, String index, LocalDate date){
		float toIndex = companyStocks.get(ric).stockTable.get(date).relativePerformance()-companyStocks.get(index).stockTable.get(date).relativePerformance();
		return toIndex;
	}

}
