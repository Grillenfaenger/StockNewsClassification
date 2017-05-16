package de.uni_koeln.spinfo.stocknews.classification;

import java.time.LocalDateTime;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;


public class StockNewsClassifyUnit extends ZoneClassifyUnit {

	private String ric;
	private LocalDateTime date;

	public StockNewsClassifyUnit(String content, String ric, LocalDateTime date, UUID id) {
		super(content,id);
		this.ric = ric;
		this.date = date;
		
	}
	
	public StockNewsClassifyUnit(String content, String ric, LocalDateTime date) {
		this(content, ric, date, UUID.randomUUID());
		this.ric = ric;
		this.date = date;
	}
	
	public StockNewsClassifyUnit(Article article, UUID id) {
		super(article.getContent(), id);
		article.setId(id);
	}
	
	public String toString(){
		return ric + "\t" + actualClassID + "\n" +  content + "\n";
	}

	
}
