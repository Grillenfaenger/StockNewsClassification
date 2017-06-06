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
	
	
	@Test
	public void bowWorkflowTest() throws ParseException, IOException, NumberFormatException, ClassNotFoundException, SQLException{
		
		String tdFile = "input/News_filtered_DE_1.1.xls";
		List<Article> articles = XLSReader.getArticlesFromXlsFile(tdFile);
		articles = articles.subList(0, 50);
		
		TreeMap<Integer,Map<String,Integer>> bows = new TreeMap<Integer,Map<String,Integer>>();
		for(Article art : articles){
			TreeMap<String, Integer> bow = BagOfWordsFactory.build(art.getContent());
			bows.put(new Random().nextInt(10000000), bow);
		}
		
		WekaClassifier classifier = new WekaClassifier(bows, new File(inputFile), "BayesClassifier");
		TreeMap<Integer, ZoneClassifyUnit> classify = classifier.classify();
		// TODO Rückgabe!
		System.out.println(classify.size());
	}

}
