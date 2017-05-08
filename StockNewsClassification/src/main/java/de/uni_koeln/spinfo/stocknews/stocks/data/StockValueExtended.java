package de.uni_koeln.spinfo.stocknews.stocks.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Core information of stock quotes (see class members) including Date and Company (RIC).  
 * @author avogt
 *
 */
public class StockValueExtended extends StockValueCore{
	
	private String ric;
	private LocalDate date;
	
	
	public StockValueExtended(String ric, LocalDate date, float open, float max, float min,
			float close, int volume, float corrected) {
		super(open,max,min,close,volume,corrected);
		this.ric = ric;
		this.date = date;
	}
	
	public StockValueExtended(String ric, String dateStr, float open, float max, float min,
			float close,int volume, float corrected) throws ParseException {
		super(open,max,min,close,volume,corrected);
		this.ric = ric;
		this.date = parseDate(dateStr);
	}
	
	private LocalDate parseDate(String time) throws ParseException {
		 DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		    LocalDate result =  LocalDate.parse(time, df); 
		return result;
	}
	
	public String getRic() {
		return ric;
	}
		

	public LocalDate getDate() {
		return date;
	}
//	public float getOpen() {
//		return open;
//	}
//	public float getMax() {
//		return max;
//	}
//	public float getMin() {
//		return min;
//	}
//	public float getClose() {
//		return close;
//	}
//	public float getCorrected() {
//		return corrected;
//	}
//	public int getVolume() {
//		return volume;
//	}

	@Override
	public String toString() {
		return "StockValueDay [ric=" + ric + ", date=" + date + ", open="
				+ super.getOpen() + ", max=" + super.getMax() + ", min=" + super.getMin() + ", close=" + super.getClose()
				+ ", volume=" + super.getVolume() + ", corrected=" + super.getCorrected() + "]";
	}
	
	

}
