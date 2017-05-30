package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockTable;
import de.uni_koeln.spinfo.stocknews.stocks.data.Trend;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

/**
 * A baseline analysis of stock quotation. Provides an evaluation of the stock quotation around a given date. 
 * The opening price of the subsequent day is compared to the closing price of the previous day.
 * 
 * class 1: course if lower or same as before
 * class 2: course rose
 * 
 * @author avogt
 *
 */
public class VerySimpleStockAnalyzer extends AbstractStockAnalyzer {

	public VerySimpleStockAnalyzer(CompanyStockTables cst){
		super(cst);
	}
	
	public VerySimpleStockAnalyzer() {}
	
	@Override
	public Map<Trend, Integer> defineEvaluationClasses() {
		return getStandardClasses();
	}

	@Override
	public int getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException {
		
		if(cst == null){
			throw new NoQuoteDataException("No Quote Data has been loaded."); 
		}
		
		LocalDate articleDay = articleDate.toLocalDate();
		StockTable ricQuotes = cst.companyStocks.get(ric);
		
		if(ricQuotes == null){
			throw new NoQuoteDataException("No Quote Data for " + ric);
		}
		
		float courseBefore = CourseTools.getcourseBefore(ric, articleDay, ricQuotes);
		float courseAfter = CourseTools.getCourseAfter(ric, articleDay, ricQuotes);
		
		if(courseAfter > courseBefore){
			System.out.println("evaluation: true, "+ courseAfter + " > " + courseBefore);
		} else {
			System.out.println("evaluation: false, " + courseAfter + " <= " + courseBefore);
		}
		if(courseAfter-courseBefore<=0.0) return classes.get(Trend.FALLING);
		else return classes.get(Trend.RISING);
	}



}
