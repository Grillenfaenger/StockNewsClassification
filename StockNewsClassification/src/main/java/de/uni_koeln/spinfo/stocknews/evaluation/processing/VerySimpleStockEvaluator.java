package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockTable;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public class VerySimpleStockEvaluator extends StockEvaluatur {

	private CompanyStockTables cst;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
	
	public VerySimpleStockEvaluator(CompanyStockTables cst){
		this.cst = cst;
	}
	
	@Override
	public boolean getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException {
		
		LocalDate articleDay = articleDate.toLocalDate();
		StockTable ricQuotes = cst.companyStocks.get(ric);
		
		if(ricQuotes ==null){
			throw new NoQuoteDataException("No Quote Data for " + ric);
		}
		
		float courseBefore = getcourseBefore(ric, articleDay, ricQuotes);
		float courseAfter = getCourseAfter(ric, articleDay, ricQuotes);
		
		if(courseAfter > courseBefore){
			System.out.println("evaluation: true, "+ courseAfter + " > " + courseBefore);
		} else {
			System.out.println("evaluation: false, " + courseAfter + " <= " + courseBefore);
		}
		if(courseAfter-courseBefore<=0.0) return false;
		else return true;
	}

	private float getcourseBefore(String ric, LocalDate articleDay, StockTable ricQuotes) throws NoQuoteDataException {
		
		LocalDate justBefore = ricQuotes.stockTable.lowerKey(articleDay);
		if(justBefore.equals(null)){
			throw new NoQuoteDataException("No Quote Data for " + ric + " on " + articleDay.toString() + "/n Update quote data first!");
		}
		return ricQuotes.stockTable.get(justBefore).getClose();
	}

	private float getCourseAfter(String ric, LocalDate articleDay, StockTable ricQuotes) throws NoQuoteDataException {
		LocalDate justAfter = ricQuotes.stockTable.higherKey(articleDay);
		if(justAfter.equals(null)){
			throw new NoQuoteDataException("No Quote Data for " + ric + " on " + articleDay.toString()+"/n Update quote data first!");
		}
		return ricQuotes.stockTable.get(justAfter).getOpen();
	}
	
//	@SuppressWarnings("deprecation")
//	private boolean whileOpen(LocalDateTime news) {
//		if(!isBeforeOpening(news) && !isAfterClosing(news)) return true;
//		else return false;
//	}
//
//	private boolean isAfterClosing(LocalDateTime news) {
//		
//		int closingHour = 17;
//		int closingMinutes = 30;
//		
//		if(news.get(ChronoField.HOUR_OF_DAY) > closingHour) {return true;}
//		else if(news.get(ChronoField.HOUR_OF_DAY) == closingHour && news.get(ChronoField.MINUTE_OF_HOUR) > closingMinutes ){ return true;}
//		else return false;
//	}
//
//	private boolean isBeforeOpening(LocalDateTime news) {
//		int openingHour = 9;
//		
//		if(news.get(ChronoField.HOUR_OF_DAY) < openingHour) return true;
//		else return false;
//	}


	
	
	

}
