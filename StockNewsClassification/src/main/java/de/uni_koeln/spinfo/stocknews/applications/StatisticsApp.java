package de.uni_koeln.spinfo.stocknews.applications;

import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;

public class StatisticsApp {
	
	public static Map<String,String> ricMap;


	public static void main(String[] args) throws IOException {
		deRicsStats();
		deDataStats();
		loadRicMap();
		
	}
	
	private static void deDataStats() throws IOException {
		List<String> nodata = FileUtils.fileToList("output/nodata.txt");
		List<String> data = FileUtils.fileToList("output/data.txt");
		
		List<String> deNoData = new ArrayList<String>();
		List<String> deData = new ArrayList<String>();
		
		for(String ric : nodata){
			if(ric.endsWith(".DE")){
				deNoData.add(ric);
			}
		}
		
		for(String ric : data){
			if(ric.endsWith(".DE")){
				deData.add(ric);
			}
		}
		
		System.out.println(".DE with Data: " + deData.size() + "\n" + deData);
		System.out.println(".DE with NoData: " + deNoData.size() + "\n" + deNoData);
	}
	
	private static void noDataArticleStats(Map<String, List<Article>> filteredNews) throws IOException {
		List<String> noDataRics = FileUtils.fileToList("output/nodata.txt");
		
		List<String> noDataArticles = new ArrayList<String>();
		
		for(String ric : noDataRics){
			if(filteredNews.containsKey(ric)){
				List<Article> list = filteredNews.get(ric);
			System.out.println("========\n"+ric+"\n========");
			noDataArticles.add("========\n"+ric+"\n========");
				
					System.out.println(list.get(0));
					noDataArticles.add(list.get(0).printComplete());
			
			}else {
				noDataArticles.add("no Article");
			}
		}
		FileUtils.printList(noDataArticles, "output/", "noDataArticles2", ".txt");
	}
	
	private static void loadRicMap() throws IOException {
		
		Map<String, String> map = new HashMap<String,String>();
		List<String> riclist = FileUtils.fileToList("input/news_export_EN_RICLIST_CI.txt");
		for(String ric : riclist){
			String[] split = ric.split("\t");
			if(split.length == 2){
				map.put(split[0], split[1]);
			} else {
				System.out.println("Error at" + split[0]);
			}
		}
		ricMap = map;
	}
	
	private static void nameEnterprise(String ric) {
		
		if(ricMap.containsKey(ric)){
			System.out.println(ric + ": " + ricMap.get(ric));
		} else {
			System.out.println("Keine Auflösung von " + ric);
		}
	}
	
	private static void deRicsStats() throws IOException{
		List<String> rics = FileUtils.fileToList("output/rics.txt");
		Set<String> ricSet =  new HashSet<String>();
		ricSet.addAll(rics);
		
		List<String> deRics = new ArrayList<String>();
		
		for(String ric : rics){
			if(ric.endsWith(".DE")) deRics.add(ric);
		}
		System.out.println(deRics.size() + " an der Börse Frankfurt gehandelte Unternehmen.");
	}
	
	public static void printCoverageCSV(Map<String, List<Article>> filteredNews, String fileName) throws IOException {
		List<String> singleCoverageCSV = new ArrayList<String>();
		String maxRic = null;
		int max = 0;
		for(String key : filteredNews.keySet()){
			int size = filteredNews.get(key).size();
			if(size > max){
				max = size;
				maxRic = key;
			}
			System.out.println(key + ": " + size);
			singleCoverageCSV.add(key+","+size);
		}
		System.out.println("max Articles: " + maxRic + ": " + max);
		FileUtils.printList(singleCoverageCSV, "output//", fileName, ".csv");
		
	}

}
