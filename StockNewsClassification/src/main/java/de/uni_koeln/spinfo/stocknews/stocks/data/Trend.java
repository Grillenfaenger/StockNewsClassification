package de.uni_koeln.spinfo.stocknews.stocks.data;

public enum Trend {
	FALLING("Falling"), RISING("Rising");
	private String description;
	
	Trend(String description){
		this.description = description;
	}
	
	public String toString(){
		return description;
	}
}
