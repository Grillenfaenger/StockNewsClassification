package stocknews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import de.uni_koeln.spinfo.stocknews.stocks.data.Frequency;
import de.uni_koeln.spinfo.stocknews.stocks.io.YahooQuoteCrawler;

public class DownloadQuotes {
	
	@Test
	public void downloadIndexQuotes() throws IOException{
		
		Calendar start = Calendar.getInstance();
		start.set(2005, 3, 28);
		Calendar end = Calendar.getInstance();
		end.set(2005, 5, 3);
		
		List<String> indexes = new ArrayList<String>();
		indexes.add("^GDAXI");
		indexes.add("^CDAXX");
		indexes.add("^TECDAX");
		indexes.add("^STOXX50E");
		indexes.add("^DJI");
		
		YahooQuoteCrawler.getCourseFromYahoo(indexes, start, end, Frequency.DAILY);
		
	}

}
