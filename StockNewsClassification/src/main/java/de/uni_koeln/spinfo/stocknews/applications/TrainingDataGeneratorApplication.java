package de.uni_koeln.spinfo.stocknews.applications;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing;
import de.uni_koeln.spinfo.stocknews.articles.processing.RicProcessing.Keyword;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.AbstractStockEvaluator;
import de.uni_koeln.spinfo.stocknews.evaluation.processing.VerySimpleStockEvaluator;
import de.uni_koeln.spinfo.stocknews.utils.FileUtils;
import de.uni_koeln.spinfo.stocknews.stocks.io.QuoteCSVReader;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.classification.StockNewsTrainingDataGenerator;
import de.uni_koeln.spinfo.stocknews.stocks.data.CompanyStockTables;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingData;
import de.uni_koeln.spinfo.stocknews.exceptions.NoQuoteDataException;

public class TrainingDataGeneratorApplication {
	
	static File tdFile  = new File("input/News_filtered_DE_1.1.xls");

	public static void main(String[] args) throws IOException, ParseException {

		
		// initialize StockEvaluator
		AbstractStockEvaluator stEval = new VerySimpleStockEvaluator();

		StockNewsTrainingDataGenerator tdg = new StockNewsTrainingDataGenerator(tdFile);
		List<TrainingData> generateTrainingData = tdg.generateTrainingData(false,"output/quotes",stEval);
	}

	
}
