package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDateTime;
import java.util.Date;

import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;

public abstract class AbstractStockAnalyzer {
	
	protected CompanyStockTables cst;
	
	public AbstractStockAnalyzer(){};
	
	protected AbstractStockAnalyzer(CompanyStockTables cst){
		this.cst = cst;
	}

	public abstract int getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException;
	
	public void setCst(CompanyStockTables cst) {
		this.cst = cst;
	}

}
