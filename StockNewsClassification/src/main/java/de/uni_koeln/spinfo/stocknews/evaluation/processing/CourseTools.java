package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockTable;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockValueCore;

public class CourseTools {
	
	public static float getcourseBefore(String ric, LocalDate articleDay, StockTable ricQuotes) throws NoQuoteDataException {
		
		LocalDate justBefore = ricQuotes.stockTable.lowerKey(articleDay);
		if(justBefore.equals(null)){
			throw new NoQuoteDataException("No Quote Data for " + ric + " on " + articleDay.toString() + "/n Update quote data first!");
		}
		return ricQuotes.stockTable.get(justBefore).getClose();
	}

	public static float getCourseAfter(String ric, LocalDate articleDay, StockTable ricQuotes) throws NoQuoteDataException {
		LocalDate justAfter = ricQuotes.stockTable.higherKey(articleDay);
		if(justAfter.equals(null)){
			throw new NoQuoteDataException("No Quote Data for " + ric + " on " + articleDay.toString()+"/n Update quote data first!");
		}
		return ricQuotes.stockTable.get(justAfter).getOpen();
	}
	
	public static StockValueCore getQuoteOf(String ric, LocalDate articleDay, StockTable ricQuotes, int offset) throws NoQuoteDataException{
		List<LocalDate> dates = new ArrayList<LocalDate>(ricQuotes.stockTable.keySet());
		int indexOf = dates.indexOf(articleDay);
		LocalDate dateOfInterest = null;
		
		try{
			dateOfInterest = dates.get(indexOf+offset);
		} catch(IndexOutOfBoundsException e){
			throw new NoQuoteDataException("No quote data available for " + ric + " on " + dateOfInterest  );
		}
		return ricQuotes.stockTable.get(dateOfInterest);
	}
			
			

}
