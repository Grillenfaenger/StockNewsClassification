package de.uni_koeln.spinfo.stocknews.applications;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing.Keyword;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.StockEvaluatur;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.VerySimpleStockEvaluator;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;
import de.uni_koeln.spinfo.stocknews.stocks.io.QuoteCSVReader;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingData;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public class TrainingDataGeneratorApplication {

	public static void main(String[] args) throws IOException, ParseException {
	
		// getArticles
		String inputfilepath = "input/News_filtered_DE_1.1.xls";
		List<Article> articles = XLSReader.getArticlesFromXlsFile(inputfilepath);
		
		articles = RicProcessing.filterIndicesFromNews(articles);
		Set<String> ricSet = RicProcessing.createRicSet(articles);
	
		// filterArticles: just one RIC
//		List<Article> singleTopicArticles = new ArrayList<Article>();
//		for(Article article : articles){
//			if(article.getRics().size() == 1){
//				singleTopicArticles.add(article);
//			}
//		}
//		System.out.println("Articles with only one Ric: " + singleTopicArticles.size());
//		Set<String> singleRics = RicProcessing.createRicSet(singleTopicArticles);
		
		Map<String, List<Article>> orderedNews = RicProcessing.orderArticlesBy(articles, ricSet, Keyword.RIC);
		
		System.out.println("Number of different Rics in Articles: " + ricSet.size());
		
		List<String> remainingRics = new ArrayList<String>();
		remainingRics.addAll(orderedNews.keySet());
		
		
		//	loadQuotes
		String quoteDir = "output/quotes";
		CompanyStockTables cst = QuoteCSVReader.readStockCoursesIntoMap(quoteDir, remainingRics);
		
		// initialize StockEvaluator
		StockEvaluatur stEval = new VerySimpleStockEvaluator(cst);
		
		// generate TrainingData
		List<TrainingData> trainingData = new ArrayList<TrainingData>();
		Set<String>noQuoteData = new HashSet<String>();
		
		for(String ric : orderedNews.keySet()){
			for(Article art : orderedNews.get(ric)){
				try {
					TrainingData td = new TrainingData(ric,art,stEval);
					System.out.println(td);
					trainingData.add(td);
				} catch(NoQuoteDataException e){
					System.out.println(e.getMessage());
					noQuoteData.add(ric);
					continue;
				}
			}
		}
		
		System.out.println("Number of TrainingData Sets:"+ trainingData.size());
		System.out.println("No Quote Data for " + noQuoteData.size() + " rics: " + noQuoteData);
		

	}

}
