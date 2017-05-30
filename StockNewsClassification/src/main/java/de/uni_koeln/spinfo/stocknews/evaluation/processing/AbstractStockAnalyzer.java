package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.data.Trend;

public abstract class AbstractStockAnalyzer {
	
	protected final Map<Trend,Integer> classes;
	protected CompanyStockTables cst;
	
	protected AbstractStockAnalyzer(){
		this.classes = defineEvaluationClasses();
	}
	
	protected AbstractStockAnalyzer(CompanyStockTables cst){
		this.classes = defineEvaluationClasses();
		this.cst = cst;
	}
	
	public abstract Map<Trend, Integer> defineEvaluationClasses();
	
	public abstract int getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException;
	
	public void setCst(CompanyStockTables cst) {
		this.cst = cst;
	}
	
	public Map<Trend,Integer> getClasses(){
		return classes;
	}
	
	public Map<Trend, Integer> getStandardClasses() {
		Map<Trend, Integer> result = new HashMap<Trend, Integer>();
		result.put(Trend.FALLING, 1);
		result.put(Trend.RISING, 2);
		return Collections.unmodifiableMap(result);
	}

}
