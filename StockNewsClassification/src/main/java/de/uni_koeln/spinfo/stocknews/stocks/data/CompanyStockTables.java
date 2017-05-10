package de.uni_koeln.spinfo.stocknews.stocks.data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public class CompanyStockTables {
	
	/**
	 * A map of StockTables by Company-RIC keys.
	 */
	public Map<String,StockTable> companyStocks; 
	
	public CompanyStockTables(){
		companyStocks = new HashMap<String,StockTable>();
	}
	
	public float courseRelativeToIndex(String ric, String index, LocalDate date) throws NoQuoteDataException{
		float toIndex;
		try {
			float ricPerf = companyStocks.get(ric).stockTable.get(date).relativePerformance();
			float indexPerf = companyStocks.get(index).stockTable.get(date).relativePerformance();
			toIndex =  ricPerf - indexPerf;
			
			if(toIndex>0){
				System.out.println(ric + ": " + ricPerf + " > " + index + ": " +indexPerf);
			} else {
				System.out.println(ric + ": " + ricPerf + " <= " + index + ": " +indexPerf);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoQuoteDataException("No Quote Data for " + ric + " or " + index + " on " + date.toString()+"/n Update quote data first!");
		}
		return toIndex;
	}

}
