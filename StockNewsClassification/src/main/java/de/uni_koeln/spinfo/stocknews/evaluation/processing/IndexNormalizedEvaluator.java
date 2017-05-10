package de.uni_koeln.spinfo.stocknews.evaluation.processing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockTable;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public class IndexNormalizedEvaluator extends AbstractStockEvaluator {
	
	private String indexRic;
	
	public IndexNormalizedEvaluator(CompanyStockTables cst, String indexRic){
		super(cst);
		this.indexRic = indexRic;
	}

	public IndexNormalizedEvaluator(String indexRic){
		super();
		this.indexRic = indexRic;
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
		
		int eval = courseRelativeToIndex>0 ? 2 : 1;
		
		return eval;
	}


}
