package de.uni_koeln.spinfo.stocknews.articles.io;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing.Keyword;
import de.uni_koeln.spinfo.stocknews.articles.io.DbConnector;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;

public class XLSReader {
	
	public static void main(String[] args) throws Exception {

		String inputfilepath = "input/News_filtered_DE_1.1.xls";
		String dbFilePath = "output/news_filtered.db";
		
		List<Article> articles = getArticlesFromXlsFile(inputfilepath);
		Set<String> tags = RicProcessing.createTagSet(articles);
		System.out.println("tags: " + tags.size());
		Set<String> rics = RicProcessing.createRicSet(articles);
		System.out.println("rics: " + rics.size());
		Map<String, List<Article>> filteredNews = RicProcessing.orderArticlesBy(articles, rics, Keyword.RIC);
		
		System.out.println("Extrahiert: " + filteredNews.size());
		List<Article> list = filteredNews.get("RWEG.DE");
		System.out.println("RWEG.DE in Document: " + list.size());
		for (Article article : list) {
			System.out.println(article);
		}
		
		// Dateiausgabe
		// Ausgabe in Datenbank
		Connection con = DbConnector.connect(dbFilePath);
		DbConnector.createNewsFilteredDB(con);
		DbConnector.insert(con, filteredNews);
		
		// Holen aus Datenbank
		ArrayList<Article> loadedArticles = new ArrayList<Article>();
		
		loadedArticles.addAll(DbConnector.getByKeyword(con, "RWEG.DE"));
		Collections.sort(loadedArticles);
		
//		filteredNews = orderArticlesBy(loadedArticles,rics,Keyword.RIC);
		System.out.println("aus db gelesen: " + loadedArticles.size());
		for (Article article : loadedArticles) {
			System.out.println(article);
		}
		
	}
	
	public static List<Article> getArticlesFromXlsFile(String filepath) throws ParseException, IOException{
		List<Article> articles = new ArrayList<Article>();
		Set<String> tagsExtracted = new TreeSet<String>();
		Set<String> ricSet = new TreeSet<String>();
		
		Workbook w;
		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("Cp1252");
			w = Workbook.getWorkbook(new File(filepath), ws);
			Sheet sheet = w.getSheet(0);
			
			String index;
			String seconds;
			String timestamp;
			String timeStr;
			String headline;
			String text;
			String rics;
			
			for (int i = 1; i < sheet.getRows(); i++) {
				
				Set<String> articleRics = new TreeSet<String>();
				
				seconds = sheet.getCell(1,i).getContents();
				index = sheet.getCell(2, i).getContents();
				timestamp = sheet.getCell(5,i).getContents();
				timeStr = timestamp.substring(0, timestamp.length()-2)+ seconds;
				headline = sheet.getCell(7,i).getContents();
				text = sheet.getCell(9,i).getContents();
				rics = sheet.getCell(21,i).getContents();
				
				
//				System.out.println(sheet.getCell(1,i).getContents());
//				System.out.println(sheet.getCell(5,i).getContents());
//				System.out.println(sheet.getCell(7,i).getContents());
//				System.out.println(sheet.getCell(9,i).getContents());

				
				
				
				if(!rics.isEmpty()){
					String[] split = rics.split(";");
					ricSet.addAll(Arrays.asList(split));
					articleRics.addAll(Arrays.asList(split));
				}
				
				Article newArticle = new Article(index, timeStr,headline,text.replaceAll("\n", " ").replaceAll("\t", " "),articleRics,true);
				articles.add(newArticle);
				tagsExtracted.addAll(newArticle.getTags());
				
				
			}
		}
		catch(BiffException e){
			e.printStackTrace();
		}
		
//		compareTags(tagsExtracted, ricSet);
		
		return articles;
		
	}
	
	

}
