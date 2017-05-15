package de.uni_koeln.spinfo.stocknews.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing.Keyword;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;

public class DepricatedApplication {

	public static void main(String[] args) throws Exception {
	
		// Read News from XLS
		Map<String, List<Article>> orderedNews = readNews();
		
		// get a List of all Rics
		Set<String> ricSet = orderedNews.keySet();
		List<String> rics = new ArrayList<String>();
		rics.addAll(orderedNews.keySet());
	
		// get StockCourses
//		Calendar start = Calendar.getInstance();
//		start.set(2005, 4, 1);
//		Calendar end = Calendar.getInstance();
//		end.set(2005, 4, 31);
//		
//		GetStockDataApp.getCourseFromYahoo(rics, start, end, Frequency.DAILY);
		
		// extract company Names from Articles
		Map<String, Set<String>> companyNames = RicProcessing.extractCompanyNamesByList(orderedNews, rics, "companyNamesAll");
		
		// filter indices and currencies
		List<String> indices = RicProcessing.getIndices(rics);
		
//		List<String> nodataRics = FileUtils.fileToList("output/nodata.txt");
//		RicProcessing.extractCompanyNamesByList(filteredNews, nodataRics, "noDataRics");
//		
//		List<String> dataRics = FileUtils.fileToList("output/data.txt");
//		RicProcessing.extractCompanyNamesByList(filteredNews, dataRics, "dataRics");
		
		String inputfilepath = "input/News_filtered_DE_1.1.xls";
		List<Article> articles = XLSReader.getArticlesFromXlsFile(inputfilepath);
		
		List<Article> filteredNews = RicProcessing.filterIndicesFromNews(articles);
		System.out.println("overall Articles: " + filteredNews.size());
		List<Article> singleTopicArticles = RicProcessing.getSingleTopicArticles(filteredNews);
		System.out.println("Number of singeTopicArticles: " + singleTopicArticles.size() );
		ricSet.removeAll(indices);
		Map<String, List<Article>> orderArticlesBy = RicProcessing.orderArticlesBy(singleTopicArticles, ricSet, RicProcessing.Keyword.RIC);
	}

	
	public static Map<String, List<Article>> readNews() throws Exception {

		String inputfilepath = "input/News_filtered_DE_1.1.xls";
		String dbFilePath = "output/news_filtered.db";
		
		List<Article> articles = XLSReader.getArticlesFromXlsFile(inputfilepath);
		Set<String> tags = RicProcessing.createTagSet(articles);
		System.out.println("tags: " + tags.size());
		Set<String> rics = RicProcessing.createRicSet(articles);
		System.out.println("rics: " + rics.size());
		Map<String, List<Article>> filteredNews = RicProcessing.orderArticlesBy(articles, rics, Keyword.RIC);
		
		System.out.println("Extrahiert: " + filteredNews.size());
		
		StatisticsApp.printCoverageCSV(filteredNews, "coverage");
		
		
		List<Article> singleTopicArticles = new ArrayList<Article>();
		// Artikel über genau ein Unternehmen
		for(Article article : articles){
			if(article.getRics().size() == 1){
				singleTopicArticles.add(article);
			}
		}
		System.out.println("Articles with only one Ric: " + singleTopicArticles.size());
		Set<String> singleRics = RicProcessing.createRicSet(singleTopicArticles);
		Map<String, List<Article>> singleTopicFilteredNews = RicProcessing.orderArticlesBy(singleTopicArticles, singleRics, Keyword.RIC);
		
		
		StatisticsApp.printCoverageCSV(singleTopicFilteredNews, "singleCoverage");
		
		
		
		// Beispiel: Artikel über RWE
		List<Article> list = filteredNews.get("RWEG.DE");
		List<Article> singleTopic = new ArrayList<Article>();
		System.out.println("RWEG.DE in Document: " + list.size());
		for (Article article : list) {
			System.out.println(article);
			if(article.getRics().size() ==1){
				singleTopic.add(article);
			}
		}
		System.out.println("only RWE: " + singleTopic.size());
		for (Article article : singleTopic) {
			System.out.println(article.printComplete());
		}
		
		// RICS vs Tags
		RicProcessing.compareTags(tags, rics);
		return filteredNews;
		
	}



		

}
