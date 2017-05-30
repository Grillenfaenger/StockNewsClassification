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

public class IndexNormalizedAnalyzer extends AbstractStockAnalyzer {
	
	private String indexRic;
	
	public IndexNormalizedAnalyzer(CompanyStockTables cst, String indexRic){
		super(cst);
		this.indexRic = indexRic;
	}

	public IndexNormalizedAnalyzer(String indexRic){
		super();
		this.indexRic = indexRic;
	}
	
	@Override
	public Map<Trend, Integer> defineEvaluationClasses() {
		return getStandardClasses();
	}

	@Override
	public int getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException {
		
		if(cst == null){
			throw new NoQuoteDataException("No Quote Data has been loaded."); 
		}
		
		if(cst.companyStocks.get(ric) == null){
			throw new NoQuoteDataException("No Quote Data for " + ric);
		}
		
		LocalDate articleDay = articleDate.toLocalDate();
		float courseRelativeToIndex = cst.courseRelativeToIndex(ric, indexRic, articleDay);
		
		int eval = courseRelativeToIndex>0 ? classes.get(Trend.RISING) : classes.get(Trend.FALLING);
		
		return eval;
	}


}
