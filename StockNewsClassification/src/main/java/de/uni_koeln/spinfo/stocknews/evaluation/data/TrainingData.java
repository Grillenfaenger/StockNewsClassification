package de.uni_koeln.spinfo.stocknews.evaluation.data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.StockEvaluatur;

public class TrainingData {
	
	private UUID id;
	private String content;
	private boolean evaluation;
	
	private LocalDateTime date;
	private String ric;
	
	public TrainingData(String ric, Article article, StockEvaluatur eval) throws NoQuoteDataException{
		this.id = UUID.randomUUID();
		article.setId(id);
		this.content = article.getContent();
		this.evaluation = eval.getEvaluation(ric, article.getDate());
		
		this.date = article.getDate();
		this.ric = ric;
	}

	@Override
	public String toString() {
		return "TrainingData [id=" + id + ", content=..."
				+ ", evaluation=" + evaluation + ", date=" + date + ", rics="
				+ ric + "]";
	}

	
	
	
	
	

}
