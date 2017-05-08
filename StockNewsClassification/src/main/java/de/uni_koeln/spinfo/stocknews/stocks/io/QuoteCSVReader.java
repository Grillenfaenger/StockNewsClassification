package de.uni_koeln.spinfo.stocknews.stocks.io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockValueExtended;
import de.uni_koeln.spinfo.stocknews.stocks.data.StockTable;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

public class QuoteCSVReader {
	
	public static List<StockValueExtended> readStockCourseCSV(String ric, String filePath) throws IOException, NumberFormatException, ParseException{
		List<StockValueExtended> course = new ArrayList<StockValueExtended>();
		
		List<String> fileToList = FileUtils.fileToList(filePath);
		fileToList.remove(0);
		for (String string : fileToList) {
			String[] split = string.split(",");
			StockValueExtended stockValueExtended = new StockValueExtended(ric, split[0],Float.parseFloat(split[1]),Float.parseFloat(split[2]),Float.parseFloat(split[3]),Float.parseFloat(split[4]),Integer.parseInt(split[5]),Float.parseFloat(split[6]));
			course.add(stockValueExtended);
		}
		return course;
	}
	
	public static List<StockValueExtended> readStockCourseCSV(File file) throws IOException, NumberFormatException, ParseException{
		List<StockValueExtended> course = new ArrayList<StockValueExtended>();
		
		String ric = YahooQuoteCrawler.extractRicFromFileName(file.getName());
		
		List<String> fileToList = FileUtils.fileToList(file.getAbsolutePath());
		fileToList.remove(0);
		for (String string : fileToList) {
			String[] split = string.split(",");
			StockValueExtended stockValueExtended = new StockValueExtended(ric, split[0],Float.parseFloat(split[1]),Float.parseFloat(split[2]),Float.parseFloat(split[3]),Float.parseFloat(split[4]),Integer.parseInt(split[5]),Float.parseFloat(split[6]));
			course.add(stockValueExtended);
		}
		return course;
	}
	
	public static CompanyStockTables readSingleStockCourseToMap(File file) throws IOException, NumberFormatException, ParseException{
		
		List<StockValueExtended> course = new ArrayList<StockValueExtended>();
		String ric = YahooQuoteCrawler.extractRicFromFileName(file.getName());
		
		List<String> fileToList = FileUtils.fileToList(file.getAbsolutePath());
		fileToList.remove(0);
		for (String string : fileToList) {
			String[] split = string.split(",");
			StockValueExtended stockValueExtended = new StockValueExtended(ric, split[0],Float.parseFloat(split[1]),Float.parseFloat(split[2]),Float.parseFloat(split[3]),Float.parseFloat(split[4]),Integer.parseInt(split[5]),Float.parseFloat(split[6]));
			course.add(stockValueExtended);
		}
		CompanyStockTables cst = new CompanyStockTables();
		cst.companyStocks.put(ric,new StockTable(course));
		
		return cst;
	}
	
	public static CompanyStockTables readStockCoursesIntoMap(String directory, List<String> rics) throws NumberFormatException, IOException, ParseException{
		CompanyStockTables cst = new CompanyStockTables();
		List<File> load = new ArrayList<File>();
		List<File> files = FileUtils.crawlFiles(directory);
		for(File file : files){
			String ric = YahooQuoteCrawler.extractRicFromFileName(file.getName());
			if(rics.contains(ric)){
				load.add(file);
			}
		}
		for(File file : load){
			CompanyStockTables singleCourse = readSingleStockCourseToMap(file);
			for(String key : singleCourse.companyStocks.keySet()){
				cst.companyStocks.put(key,singleCourse.companyStocks.get(key));
			}
		}
		return cst;
	}

}
