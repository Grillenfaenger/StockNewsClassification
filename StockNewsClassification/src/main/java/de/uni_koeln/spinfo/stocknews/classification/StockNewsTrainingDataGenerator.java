package de.uni_koeln.spinfo.stocknews.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing.Keyword;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingData;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingDataCollection;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.AbstractStockAnalyzer;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.VerySimpleStockAnalyzer;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.stocks.io.QuoteCSVReader;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;

/**
 * Class to annotate ClassifyUnits manually with (one or more) classIDs
 * 
 * @author jhermes, geduldig
 * 
 */
public class StockNewsTrainingDataGenerator {

	private File tdFile;
	private List<ClassifyUnit> classifiedData;
	private int numberOfSingleClasses = 0;
	int deletions;
//	private int numberOfMulticlasses = 4;

	/**
	 * Instanciates a new TrainingDataGenerator corresponding to the specified
	 * file.
	 * 
	 * @param trainingDataFile
	 *            File for trained data
	 */
	public StockNewsTrainingDataGenerator(File trainingDataFile) {
		this.tdFile = trainingDataFile;
		classifiedData = new ArrayList<ClassifyUnit>();
	}

	


	/**
	 * @param trainingDataFile
	 * @param categories number of categories
	 * @param classes number of classes
	 * @param translations translations from classes to categories
	 */
	public StockNewsTrainingDataGenerator(File trainingDataFile, int categories, int classes, Map<Integer, List<Integer>> translations) {
		this.tdFile = trainingDataFile;
		classifiedData = new ArrayList<ClassifyUnit>();
		this.numberOfSingleClasses = classes;
		StockNewsClassifyUnit.setNumberOfCategories(categories, classes, translations);
	}

	/**
	 * Returns trained (manually annotated) data from training data file.
	 * 
	 * @return List of manually annotated ClassifyUnits
	 * @throws IOException
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	public List<ClassifyUnit> getTrainingData() throws IOException, NumberFormatException, ParseException {
		if (classifiedData.isEmpty()) {
			classifiedData = new ArrayList<ClassifyUnit>();
			
			List<TrainingData> trainingData = readTrainingData(tdFile);
			classifiedData = trainingDataToCU(trainingData);
		}	
		return classifiedData;
	}
	
	public static List<TrainingData> readTrainingData(File tdFile) throws IOException {
		List<TrainingData> tds = new ArrayList<TrainingData>();
		
		List<String> tdStrings = FileUtils.fileToList(tdFile.getAbsolutePath());
		for(String s : tdStrings){
			String[] split = s.split("\t");
			TrainingData td = new TrainingData(UUID.fromString(split[0]),split[1],Integer.parseInt(split[2]),LocalDateTime.parse(split[3]),split[4]);
			tds.add(td);
		}
		return tds;
	}




	public List<ClassifyUnit> trainingDataToCU(List<TrainingData> trainingData) {
		List<ClassifyUnit> cu = new ArrayList<ClassifyUnit>();
		
		for (TrainingData td : trainingData) {
			StockNewsClassifyUnit sncu = new StockNewsClassifyUnit(td.getContent(),td.getRic(),td.getDate(),td.getId());
			sncu.setActualClassID(td.getEvaluation());
			cu.add(sncu);
		}
		
		return cu;
	}




	public List<TrainingData> generateTrainingData() throws NumberFormatException, IOException, ParseException{
		// getArticles
		List<Article> articles = XLSReader.getArticlesFromXlsFile(tdFile.getAbsolutePath());
		
		articles = RicProcessing.filterIndicesFromNews(articles);
		Set<String> ricSet = RicProcessing.createRicSet(articles);
		
		Map<String, List<Article>> orderedNews = RicProcessing.orderArticlesBy(articles, ricSet, Keyword.RIC);
		
		System.out.println("Number of different Rics in Articles: " + ricSet.size());
		
		List<String> remainingRics = new ArrayList<String>();
		remainingRics.addAll(orderedNews.keySet());
		
		
		//	loadQuotes
		String quoteDir = "output/quotes";
		CompanyStockTables cst = QuoteCSVReader.readStockCoursesIntoMap(quoteDir, remainingRics);
		
		// initialize StockEvaluator
		AbstractStockAnalyzer stEval = new VerySimpleStockAnalyzer(cst);
		
		// generate TrainingData
		List<TrainingData> trainingData = new ArrayList<TrainingData>();
		Set<String>noQuoteData = new HashSet<String>();
		
		for(String ric : orderedNews.keySet()){
			for(Article art : orderedNews.get(ric)){
				try {
					TrainingData td = new TrainingData(ric,art,stEval);
					System.out.println(td);
					trainingData.add(td);
				} catch(NoQuoteDataException e){
					System.out.println(e.getMessage());
					noQuoteData.add(ric);
					continue;
				}
			}
		}
		
		// write trainingData file
		List<String> printableTd = new ArrayList<String>();
		for(TrainingData td : trainingData){
			printableTd.add(td.toTSVString());
		}
		FileUtils.printList(printableTd, "output/classification/", "trainingData", ".txt");
		
		return trainingData;
	}
	
	public List<TrainingData> generateTrainingData(boolean singleRicOnly, String quoteDir, AbstractStockAnalyzer stEval, String outputFilename) throws NumberFormatException, IOException, ParseException{
		// getArticles
		List<Article> articles = XLSReader.getArticlesFromXlsFile(tdFile.getAbsolutePath());
		
		articles = RicProcessing.filterIndicesFromNews(articles);
		
		if(singleRicOnly){
			articles = RicProcessing.getSingleTopicArticles(articles);
		}
		
		// all rics covered by articles
		Set<String> ricSet = RicProcessing.createRicSet(articles);
		
		Map<String, List<Article>> orderedNews = RicProcessing.orderArticlesBy(articles, ricSet, Keyword.RIC);
		
		System.out.println("Number of different Rics in Articles: " + ricSet.size());
		
		Set<String> remainingRics = new TreeSet<String>();
		remainingRics.addAll(orderedNews.keySet());
		String dax = "^GDAXI";
		remainingRics.add(dax);
		
		//	loadQuotes
		CompanyStockTables cst = QuoteCSVReader.readStockCoursesIntoMap(quoteDir, new ArrayList<String>(remainingRics));
		
//		System.out.println(cst.companyStocks.get(dax));
//		System.out.println(cst.companyStocks.get("ONXX.O"));
		
		stEval.setCst(cst);
		
		// generate TrainingData
		List<TrainingData> trainingData = new ArrayList<TrainingData>();
		Set<String>noQuoteData = new HashSet<String>();
		
		for(String ric : orderedNews.keySet()){
			for(Article art : orderedNews.get(ric)){
				try {
					TrainingData td = new TrainingData(ric,art,stEval);
					System.out.println(td);
					trainingData.add(td);
				} catch(NoQuoteDataException e){
					System.out.println(e.getMessage());
					noQuoteData.add(ric);
					continue;
				}
			}
		}
		
		TrainingDataCollection tdColl = new TrainingDataCollection(stEval.getClass().toString(), stEval.getClasses(), remainingRics, trainingData);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<TrainingDataCollection>(){}.getType();
		String json = gson.toJson(tdColl, type);
		System.out.println(json);
		
		// write trainingData file
		
		FileUtils.printString(json,  "output/classification/", outputFilename, ".json");
//		List<String> printableTd = new ArrayList<String>();
//		for(TrainingData td : trainingData){
//			printableTd.add(td.toTSVString());
//		}
//		FileUtils.printList(printableTd, "output/classification/", outputFilename, ".txt");
		
		FileUtils.printMap(stEval.getClasses(), "output/classification/", outputFilename+"_classes");
		
		return trainingData;
	}
	
	public List<TrainingData> generateTrainingData_new(boolean singleRicOnly, String quoteDir, AbstractStockAnalyzer evaluator, String outputFilename) throws NumberFormatException, IOException, ParseException{
		// getArticles
		List<Article> articles = XLSReader.getArticlesFromXlsFile(tdFile.getAbsolutePath());
		
		articles = RicProcessing.filterIndicesFromNews(articles);
		
		if(singleRicOnly){
			articles = RicProcessing.getSingleTopicArticles(articles);
		}
		
		// all rics covered by articles
		Set<String> ricSet = RicProcessing.createRicSet(articles);
		System.out.println("Number of different Rics in Articles: " + ricSet.size());
		
		String dax = "^GDAXI";
		ricSet.add(dax);
		
		//	loadQuotes
		CompanyStockTables cst = QuoteCSVReader.readStockCoursesIntoMap(quoteDir, new ArrayList<String>(ricSet));
		evaluator.setCst(cst);
		
		// generate TrainingData
		List<TrainingData> trainingData = new ArrayList<TrainingData>();
		List<TrainingData> tdBuffer = new ArrayList<TrainingData>();
		Set<String>noQuoteData = new HashSet<String>();
		
		articleLoop: for(Article art : articles){
			int eval = 0;
			for(String ric : art.getRics()){
				try {
					TrainingData td = new TrainingData(ric,art,evaluator);
					System.out.println(td);
					if(eval == 0){
						eval = td.getEvaluation();
					} else if (eval == td.getEvaluation()){
						tdBuffer.add(td);
					} else if (eval != td.getEvaluation()){
						System.out.println("Article yields contradictory evaluations. " + art);
						tdBuffer.clear();
						continue articleLoop;
					}
				} catch(NoQuoteDataException e){
					System.out.println(e.getMessage());
					noQuoteData.add(ric);
					continue;
				}
			}
			trainingData.addAll(tdBuffer);
			tdBuffer.clear();
		}
		
		ricSet.removeAll(noQuoteData);
		System.out.println("overall trainingData: " + trainingData.size());
		
		TrainingDataCollection tdColl = new TrainingDataCollection(evaluator.getClass().toString(), evaluator.getClasses(), ricSet, trainingData);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<TrainingDataCollection>(){}.getType();
		String json = gson.toJson(tdColl, type);
		System.out.println(json);
		
		// write trainingData file
		FileUtils.printString(json,  "output/classification/", outputFilename, ".json");
		FileUtils.printMap(evaluator.getClasses(), "output/classification/", outputFilename+"_classes");
		
		return trainingData;
	}

}
