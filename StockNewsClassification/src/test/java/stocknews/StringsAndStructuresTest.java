package stocknews;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Test;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.articles.io.XLSReader;
import de.uni_koeln.spinfo.stocknews.classification.workflow.WekaClassifier;
import de.uni_koeln.spinfo.stocknews.utils.BagOfWordsFactory;

public class StringsAndStructuresTest {
	
	
	/**
	* Path to inputfile
	*/
	static String inputFile = "output/classification/trainingData.txt";
	
	/**
	* path to the output folder for classified articles
	*/
	static String outputFolder = "output/classification";


}
