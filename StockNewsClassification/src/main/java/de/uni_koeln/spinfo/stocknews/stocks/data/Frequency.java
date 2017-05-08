package de.uni_koeln.spinfo.stocknews.stocks.data;

public enum Frequency {
	DAILY('d'),WEEKLY('w'),MONTHLY('m');
	private char c;
	
	private Frequency(char c){
		this.c = c;
	}
		
	public char getChar(){
		return c;
	}
}
