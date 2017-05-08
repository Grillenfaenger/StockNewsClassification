package de.uni_koeln.spinfo.stocknews.stocks.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Core information of stock quotes (see class members); Date and Company (RIC) excluded.  
 * @author avogt
 *
 */
public class StockValueCore {
	
	/**
	 * Kurs zur Eröffnung
	 */
	private float open;
	private float max;
	private float min;
	/**
	 * Schlusskurs
	 */
	private float close;
	private int volume;
	/**
	 * Berichtigter Kurs: Schlusspreis nach Berichtigung für Dividenden und Splits.
	 */
	private float corrected;
	
	
	public StockValueCore(float open, float max, float min,
			float close, int volume, float corrected) {
		this.open = open;
		this.max = max;
		this.min = min;
		this.close = close;
		this.volume = volume;
		this.corrected = corrected;
	}
	
	
	public StockValueCore(StockValueExtended quote) {
		this.open = quote.getOpen();
		this.max = quote.getMax();
		this.min = quote.getMin();
		this.close = quote.getClose();
		this.volume = quote.getVolume();
		this.corrected = quote.getCorrected();
	}
		
	public float getOpen() {
		return open;
	}
	public float getMax() {
		return max;
	}
	public float getMin() {
		return min;
	}
	public float getClose() {
		return close;
	}
	public float getCorrected() {
		return corrected;
	}
	public int getVolume() {
		return volume;
	}

	@Override
	public String toString() {
		return "StockValueCore [open="	+ open + ", max=" + max + ", min=" + min + ", close=" + close
				+ ", volume=" + volume + ", corrected=" + corrected + "]";
	}
	
	public float absoluteVolatility(){
		return max-min;
	}
	
	public float relativeVolatility(){
		return (max-min)/min;
	}
	
	public float logVolatility(){
		return (float) Math.log(max/min);
	}
	
	public float absolutPerformance(){
		return close-open;
	}
	
	public float relativePerformance(){
		return absolutPerformance()/open;
	}

}
