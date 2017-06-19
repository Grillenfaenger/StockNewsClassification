package de.uni_koeln.spinfo.stocknews.articles.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Article implements Comparable<Article> {
	
	UUID id;
	String index;
	LocalDateTime date;
	String timeStr;
	String title;
	String content;
	Set<String> rics;
	Set<String> tags;
	
	
	static Pattern pattern = Pattern.compile("(<)((.*?))(>)");
	
	public Article(String index, String timeStr, String title, String content) throws ParseException {
		this.index = index;
		this.timeStr = timeStr;
		this.date = parseDate(timeStr);
		this.title = title;
		this.content = content;
	}
	
	public Article(String index, String timeStr, String title, String content, Set<String> rics, boolean extractTagsFromText) throws ParseException {
		this.index = index;
		this.timeStr = timeStr;
		this.date = parseDate(timeStr);
		this.title = title;
		this.content = content;
		this.rics = rics;
		if(extractTagsFromText){
			this.tags = extractTags();
		}
	}
	
	public Article(String index, LocalDateTime date, String title, String content, Set<String> rics, Set<String> tags) throws ParseException {
		this.index = index;
		this.date = date;
		this.title = title;
		this.content = content;
		this.rics = rics;
		this.tags = tags;
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getIndex() {
		return index;
	}

	public Set<String> getTags() {
		return tags;
	}
	
	public String tagsToString(){
		if(tags.isEmpty()){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String tag : tags){
			sb.append(tag);
			sb.append(";");
		} 
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	
	public Set<String> getRics() {
		return rics;
	}
	
	public void setRics(Set<String> rics) {
		this.rics = rics;
	}
	
	public String ricsToString(){
		if(rics.isEmpty()){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String ric : rics){
			sb.append(ric);
			sb.append(";");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getTimeStr() {
		return timeStr;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	private LocalDateTime parseDate(String time) throws ParseException {
		 DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yy H:mm:ss.S");
		    LocalDateTime result =  LocalDateTime.parse(time, df); 
		return result;
	}
	
	private Set<String> extractTags() {
		Set<String> extracted = new TreeSet<String>();
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()){
			extracted.add(matcher.group(2).trim());
		}
		return extracted;
	}
	
	
	public String printComplete() {
		return "Article [index=" + index + ", date=" + date + ", timeStr="
				+ timeStr + "\ntitle=" + title + "\ncontent=" + content
				+ ", rics=" + rics + ", tags=" + tags + "]";
	}

	@Override
	public String toString() {
		return "Article [ [date=" + date + ", timeStr=" + timeStr + ", title=" + title+ "]";
//		return "Article [date=" + date + ", title=" + title
//				+ ", content=" + content + "]";
	}


	@Override
//	public boolean equals(Object o) {
//		if(o instanceof Article){
//			return index.equals(((Article) o).getIndex());
//		} else return false;
//	}
	public boolean equals(Object o) {
		if(o instanceof Article){
			return id.equals(((Article) o).getIndex());
		} else return false;
	}

	@Override
	public int compareTo(Article o) {
		LocalDateTime date2 = o.getDate();
		return date.compareTo(date2);
	}

	public Long getDateAsLong() {
		ZonedDateTime zdt = date.atZone(ZoneOffset.UTC);
		Long millis = zdt.toInstant().toEpochMilli();
		return millis;
	}

	public void setContent(String content) {
		this.content = content;
		
	}
	
}
