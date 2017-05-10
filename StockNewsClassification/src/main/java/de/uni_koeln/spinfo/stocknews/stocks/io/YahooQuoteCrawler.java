package de.uni_koeln.spinfo.stocknews.stocks.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Connection.Response;

import de.uni_koeln.spinfo.stocknews.stocks.data.Frequency;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

import org.jsoup.Jsoup;

public class YahooQuoteCrawler {
	
	/**
	 * Fetches stock market value of given 
	 * @param ric
	 * @param start
	 * @param end
	 * @return
	 */
	static SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
	
	public static boolean getQuoteTable(String ric, Calendar start, Calendar end, Frequency frq) {
		
		if(new File("output/quotes/"+buildFileName(ric,start,end)+ ".csv").exists()){
			System.out.println("Quotes for " + ric + "in the specified period have been already fetched.");
			return true;
		}
		
		String url = "http://chart.finance.yahoo.com/table.csv?s="+ric+"&amp;a="+(start.get(Calendar.MONTH)+"&amp;b="+start.get(Calendar.DAY_OF_MONTH)+"&amp;c="
				+start.get(Calendar.YEAR)+"&amp;d="+(end.get(Calendar.MONTH))+"&amp;e="+end.get(Calendar.DAY_OF_MONTH)+"&amp;f="+end.get(Calendar.YEAR)+"&amp;g="+frq.getChar()+"&amp;ignore=.csv");
		System.out.println(url);

	
		//Open a URL Stream
		Response resultImageResponse;
		try {
			resultImageResponse = Jsoup.connect(url).ignoreContentType(true).execute();
			// output here
			FileOutputStream out = (new FileOutputStream(new java.io.File("output/quotes/"
					+ buildFileName(ric,start,end)+".csv")));
			out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
			out.close();
			return true;
			
		} catch (IOException e) {
			return false;
		}
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
	
	public static String buildFileName(String ric, Calendar start, Calendar end){
		String filename = "quote_"+ ric +"_"+sdf.format(start.getTime())+"-"+sdf.format(end.getTime());
		return filename;
	}
	
	public static String extractRicFromFileName(String filename){
		return filename.split("_")[1];
	}

}
