package de.uni_koeln.spinfo.stocknews.articles.processing;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

public class RicProcessing {
	
	public static enum Keyword{RIC, TAG};
	static Pattern pattern = Pattern.compile("(.*?)<(.*?)>");
	static Pattern tagPattern = Pattern.compile("(<)((.*?))(>)");
	
	public static Set<String> extractTags(String content) {
		Set<String> extracted = new TreeSet<String>();
		Matcher matcher = tagPattern.matcher(content);
		while(matcher.find()){
			extracted.add(matcher.group(2).trim());
		}
		return extracted;
	}
	
	public static List<String> getIndices(List<String> rics) throws IOException{
		List<String> indices = new ArrayList<String>();
		
		for(String ric : rics){
			if(ric.startsWith(".")||ric.contains("=")){
				indices.add(ric);
			}
		}
		
		FileUtils.printList(indices, "output/", "indices", ".txt");
		return indices;
	}
	
	public static List<Article> filterIndicesFromNews(List<Article> articles) throws ParseException, IOException{
		
		List<String> indices = getIndices(new ArrayList<String>(RicProcessing.createRicSet(articles)));
		
		Set<String> ricsCopy = new HashSet<String>();
		
		for(Article art : articles){
			Set<String> rics = art.getRics();
			ricsCopy.clear();
			ricsCopy.addAll(rics);
			if(rics.removeAll(indices)){
				art.setRics(rics);
				if(!rics.isEmpty()){
					System.out.println("before: " + ricsCopy);
					System.out.println("after: " + rics);
				}
			}
		}
		return articles;
	}
	
	public static Set<String> createRicSet(List<Article> articles){
		Set<String> rics = new TreeSet<String>();
		for(Article art : articles){
			rics.addAll(art.getRics());
		}
		return rics;
	}
	
	public static Set<String> createTagSet(List<Article> articles){
		Set<String> tags = new TreeSet<String>();
		for(Article art : articles){
			tags.addAll(art.getTags());
		}
		return tags;
	}

	public static void compareTags(Set<String> tagsExtracted,
			Set<String> ricSet) throws IOException {
		
		System.out.println("extracted: " + tagsExtracted.size());
		System.out.println("rics: " + ricSet.size());
		
		FileUtils.printSet(ricSet, "output/", "rics");
		FileUtils.printSet(tagsExtracted, "output/", "extracted");
		
		tagsExtracted.removeAll(ricSet);
		System.out.println(tagsExtracted);
		
		FileUtils.printSet(tagsExtracted, "output/", "uniqueExtracted");
		
	}
	
	public static Map<String,List<Article>> orderArticlesBy(List<Article> articles, Set<String> keywords, Keyword byKeyword){
		
		Map<String,List<Article>> filteredNews = new HashMap<String,List<Article>>();
		filteredNews.put("notag", new ArrayList<Article>());
		
		for(Article art : articles){
			Set<String> artKeys = null; 
			if(byKeyword == Keyword.TAG){
				artKeys = art.getTags();
			} else if(byKeyword == Keyword.RIC) {
				artKeys = art.getRics();
			}
			if(artKeys.isEmpty()) {
				List<Article> current = filteredNews.get("notag");
				current.add(art);
				filteredNews.put("notag", current);
			} else {
				for(String keyword : keywords){
					if(artKeys.contains(keyword)){
						if(filteredNews.containsKey(keyword)){
							List<Article> current = filteredNews.get(keyword);
							current.add(art);
							filteredNews.put(keyword, current);
						} else {
							List<Article> current = new ArrayList<Article>();
							current.add(art);
							filteredNews.put(keyword, current);
						}
					}
				}
			}
		}
		
		for(String key : filteredNews.keySet()){
			Collections.sort(filteredNews.get(key));
//			System.out.println("\n\n\n"+ key + ": ");
//			for(Article art : filteredNews.get(key)){
//				System.out.println(art);
//			}
		}
		return filteredNews;
	}
	
	public static List<Article> getSingleTopicArticles(List<Article> articles){
		List<Article> singleTopicArticles = new ArrayList<Article>();
		// Artikel über genau ein Unternehmen
		for(Article article : articles){
			if(article.getRics().size() == 1){
				singleTopicArticles.add(article);
			}
		}
		return singleTopicArticles;
	}

	
	public static Map<String,Set<String>> extractCompanyNamesByList(Map<String, List<Article>> filteredNews, List<String> rics, String filename) throws IOException {
		
		Map<String,Set<String>> companyRicMap = new HashMap<String,Set<String>>();
		
		for(String key : rics){
			if(filteredNews.containsKey(key)){
				String escapedKey = escapeDot(key);
				Pattern pattern2 = Pattern.compile("([^\\s\\>,\\.]*\\s*[^\\s\\>,\\.]*\\s*[^\\s\\>,\\.]+)(?:<"+escapedKey+">)");
//				Pattern pattern2 = Pattern.compile("([\\>,\\.]?)(\\S*\\s?\\S*\\s?\\S+)(<"+escapedKey+">)");
				Matcher matcher = pattern2.matcher(filteredNews.get(key).get(0).getContent());
				while(matcher.find()){
					String find = matcher.group(1).trim().replaceAll("\\n", " ");
						if(companyRicMap.containsKey(find)){
							companyRicMap.get(key).add(find);
						} else {
							Set<String> alternatives = new HashSet<String>();
							alternatives.add(find);
							companyRicMap.put(key,alternatives);
						}
				}
				if(!companyRicMap.containsKey(key)){
					System.out.println(key);
				}
			} else if(filteredNews.containsKey(key+".N")) {
				key = key+".N";
				String escapedKey = escapeDot(key);
				Pattern pattern2 = Pattern.compile("([^\\s\\>,\\.]*\\s*[^\\s\\>,\\.]*\\s*[^\\s\\>,\\.]+)(?:<"+escapedKey+">)");
//				Pattern pattern2 = Pattern.compile("([^\\s\\>,\\.]*\\s?[^\\s\\>,\\.]*\\s?[^\\s\\>,\\.]+)(<"+escapedKey+">)");
				Matcher matcher = pattern2.matcher(filteredNews.get(key).get(0).getContent());
				String find = null;
				while(matcher.find()){
					find = matcher.group(1).trim().replaceAll("\\n", " ");
						if(companyRicMap.containsKey(find)){
							companyRicMap.get(key).add(find);
						} else {
							Set<String> alternatives = new HashSet<String>();
							alternatives.add(find);
							companyRicMap.put(key,alternatives);
						}
				}
				if(!companyRicMap.containsKey(key)){
					System.out.println(key);
				}
			} else {
				System.out.println(key);
			} 
		}
		FileUtils.printMap(companyRicMap, "output/", filename);
		return companyRicMap;
	}
	

	
	private static String escapeDot(String key) {
		return key.replaceAll("\\.", Matcher.quoteReplacement("\\."));
	}
	
	//	public static void extractCompanyNames(Map<String, List<Article>> filteredNews) throws IOException {
	//	
	//	Map<String,Set<String>> companyRicMap = new HashMap<String,Set<String>>();
	//	
	//	for(String key : filteredNews.keySet()){
	//		String[] split = filteredNews.get(key).get(0).getContent().split(" ");
	//		for (int i = 0; i < split.length; i++) {
	//			Matcher matcher = pattern.matcher(split[i]);
	//			while(matcher.find()){
	//				String find = matcher.group().replaceAll(">", "");
	//					String[] split2 = find.split("<",2);
	//					String value = split[i-1]+" "+split2[0];
	//					value = value.trim();
	//					if(companyRicMap.containsKey(split2[1])){
	//						companyRicMap.get(split2[1]).add(value);
	//					} else {
	//						Set<String> alternatives = new HashSet<String>();
	//						alternatives.add(value);
	//						companyRicMap.put(split2[1],alternatives);
	//					}
	//			}
	//		}
	//	}
	//	FileUtils.printMap(companyRicMap, "output/", "ricMap");
	//	
	//}
	//
	//public static void extractCompanyNamesByList(Map<String, List<Article>> filteredNews, List<String> rics) throws IOException {
	//	
	//	Map<String,Set<String>> companyRicMap = new HashMap<String,Set<String>>();
	//	
	//	for(String key : rics){
	//		if(filteredNews.containsKey(key)){
	//			String[] split = filteredNews.get(key).get(0).getContent().split(" ");
	//			for (int i = 0; i < split.length; i++) {
	//				Matcher matcher = pattern.matcher(split[i]);
	//				while(matcher.find()){
	//					String find = matcher.group().replaceAll(">", "");
	//						String[] split2 = find.split("<",2);
	//						String value = split[i-1]+" "+split2[0];
	//						value = value.trim();
	//						if(companyRicMap.containsKey(split2[1])){
	//							companyRicMap.get(split2[1]).add(value);
	//						} else {
	//							Set<String> alternatives = new HashSet<String>();
	//							alternatives.add(value);
	//							companyRicMap.put(split2[1],alternatives);
	//						}
	//				}
	//			}
	//		}
	//	}
	//	FileUtils.printMap(companyRicMap, "output/", "ricMap");
	//	
	//}
	//
	//public static void extractCompanyNames2(Map<String, List<Article>> filteredNews) throws IOException {
	//		
	//	
	//	
	//		Map<String,Set<String>> companyRicMap = new HashMap<String,Set<String>>();
	//		
	//		for(String key : filteredNews.keySet()){
	//			String escapedKey = escapeDot(key);
	//			System.out.println(escapedKey);
	//			Pattern pattern2 = Pattern.compile("(\\S*\\s\\S*)(<"+escapedKey+">)");
	//				Matcher matcher = pattern2.matcher(filteredNews.get(key).get(0).getContent());
	//				while(matcher.find()){
	//					String find = matcher.group(1).trim();
	//					System.out.println(find);
	//						if(companyRicMap.containsKey(find)){
	//							companyRicMap.get(key).add(find);
	//						} else {
	//							Set<String> alternatives = new HashSet<String>();
	//							alternatives.add(find);
	//							companyRicMap.put(key,alternatives);
	//						}
	//				}
	//		}
	//		FileUtils.printMap(companyRicMap, "output/", "ricMapRegex");
	//		
	//	}
}
