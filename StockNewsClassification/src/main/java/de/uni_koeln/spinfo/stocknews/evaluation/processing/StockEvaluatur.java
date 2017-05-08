package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDateTime;
import java.util.Date;

import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public abstract class StockEvaluatur {
	
	public abstract boolean getEvaluation(String ric, LocalDateTime articleDate) throws NoQuoteDataException;

}
