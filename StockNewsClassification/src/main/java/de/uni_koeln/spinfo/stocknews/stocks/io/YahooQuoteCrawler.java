package de.uni_koeln.spinfo.stocknews.stocks.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Connection.Response;

import de.uni_koeln.spinfo.stocknews.stocks.data.Frequency;

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
	
	public static String buildFileName(String ric, Calendar start, Calendar end){
		String filename = "quote_"+ ric +"_"+sdf.format(start.getTime())+"-"+sdf.format(end.getTime());
		return filename;
	}
	
	public static String extractRicFromFileName(String filename){
		return filename.split("_")[1];
	}

}
