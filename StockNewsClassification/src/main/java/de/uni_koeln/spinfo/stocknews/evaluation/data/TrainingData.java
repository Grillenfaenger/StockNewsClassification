package de.uni_koeln.spinfo.stocknews.evaluation.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.AbstractStockEvaluator;

public class TrainingData {
	
	private UUID id;
	private String content;
	private int evaluation;
	
	private LocalDateTime date;
	private String ric;
	
	public TrainingData(String ric, Article article, AbstractStockEvaluator eval) throws NoQuoteDataException{
		this.id = UUID.randomUUID();
		article.setId(id);
		this.content = article.getContent();
		this.evaluation = eval.getEvaluation(ric, article.getDate());
		
		this.date = article.getDate();
		this.ric = ric;
	}
	
	public TrainingData(UUID id, String content, int evaluation, LocalDateTime date, String ric){
		this.id = id;
		this.content = content;
		this.evaluation = evaluation;
		this.date = date;
		this.ric = ric;
	}
	
	

	public UUID getId() {
		return id;
	}



	public String getContent() {
		return content;
	}



	public int getEvaluation() {
		return evaluation;
	}



	public LocalDateTime getDate() {
		return date;
	}



	public String getRic() {
		return ric;
	}



	@Override
	public String toString() {
		return "TrainingData [id=" + id + ", content=..."
				+ ", evaluation=" + evaluation + ", date=" + date + ", rics="
				+ ric + "]";
	}
	
	public String toTSVString() {
		return id+"\t"+content+"\t"+evaluation+"\t"+date+"\t"+ric;
	}

	
	
	
	
	

}
