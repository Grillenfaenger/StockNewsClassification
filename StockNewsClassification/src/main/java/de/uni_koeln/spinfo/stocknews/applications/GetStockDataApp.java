package de.uni_koeln.spinfo.stocknews.applications;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.stocks.io.YahooQuoteCrawler;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.stocks.data.Frequency;

public class GetStockDataApp {
	
	public static void main(String[] args) throws IOException, ParseException {
		
		List<String> rics = null;
		Set<String> ricSet = null;
		
		String ricFilePath = "output/rics.txt";
		File ricFile = new File(ricFilePath);
		if(ricFile.exists()){
			rics = FileUtils.fileToList(ricFilePath);
			ricSet= new HashSet<String>();
			ricSet.addAll(rics);
		} else {
			String inputfilepath = "input/News_filtered_DE_1.1.xls";
			List<Article> articles = XLSReader.getArticlesFromXlsFile(inputfilepath);
			ricSet = RicProcessing.createRicSet(articles);
			rics = new ArrayList<String>();
			rics.addAll(ricSet);
		}
		
		Calendar start = Calendar.getInstance();
		start.set(2005, 3, 28);
		Calendar end = Calendar.getInstance();
		end.set(2005, 5, 3);
		
		getCourseFromYahoo(rics, start, end, Frequency.DAILY);
		
	}
	
	public static void getCourseFromYahoo(List<String> rics, Calendar start, Calendar end, Frequency frq) throws IOException{
		
		// List of rics for which NO DATA could be found
		List<String> nodata = new ArrayList<String>();
		// List of rics for which data had been downloaded
		List<String> data = new ArrayList<String>();
		
		System.out.println(start.getMaximum(Calendar.MONTH));
		System.out.println(start.get(Calendar.DAY_OF_MONTH));
		System.out.println(start.get(Calendar.YEAR));
		
		System.out.println(end.get(Calendar.MONTH));
		System.out.println(end.get(Calendar.DAY_OF_MONTH));
		System.out.println(end.get(Calendar.YEAR));
		
		// Stocks traded on NYSE are listed without .N suffix on yahoo finaces
		for (String ric : rics) {
			if(ric.endsWith(".N")){
				ric = ric.substring(0, ric.length()-2);
			}
			System.out.println(ric);
			if(YahooQuoteCrawler.getQuoteTable(ric, start, end, frq)){
				data.add(ric);
			} else {
				nodata.add(ric);
			}
		}
		
		System.out.println("Downloaded quotes for "+  data.size() + " companies");
		FileUtils.printList(data, "output/", "data", ".txt");
		System.out.println("Unable to find any data for " + nodata.size() + " Rics.");
		FileUtils.printList(nodata, "output/", "nodata", ".txt");
		
	}

}
