package de.uni_koeln.spinfo.stocknews.applications;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.classification.workflow.ConfigurableArticleClassifier;
import de.uni_koeln.spinfo.stocknews.classification.StockNewsClassifyUnit;

public class ClassifyArticlesApp {
	
	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////
	
	/**
	* Path to inputfile
	*/
	static String inputFile = "input/News_filtered_DE_1.1.xls";
	
	/**
	* path to the output folder for classified articles
	*/
	static String outputFolder = "output/classification";
	
	/**
	* Path to the trainingdata-file (is used to train the
	* classifiers)
	*/
	static String trainingdataFile = "output/classification/trainingData.txt";
	
	/////////////////////////////
	// END
	/////////////////////////////
	
	public static void main(String[] args) throws IOException, ParseException, NumberFormatException, ClassNotFoundException, SQLException {
		
		List<Article> articles = XLSReader.getArticlesFromXlsFile(inputFile);
		
		System.out.println(articles.size());
		articles = articles.subList(0, 7000);
		
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		List<Integer> categories = new ArrayList<Integer>();
		categories.add(1);
		translations.put(1, categories);
		categories = new ArrayList<Integer>();
		categories.add(2);
		translations.put(2, categories);
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(2, 2, translations);
		ConfigurableArticleClassifier cac = new ConfigurableArticleClassifier(articles, trainingdataFile, stmc);
		
		List<ClassifyUnit> classified = cac.classify(null);
		
		int steigt = 0;
		
		for(ClassifyUnit cu : classified){
			int classi = ((ZoneClassifyUnit) cu).getActualClassID();
			
			if(classi == 2) {
				steigt++;
			}
		}
		System.out.println(articles.size());
		System.out.println(steigt + " steigende Klassifikationen");
	}
	

}
