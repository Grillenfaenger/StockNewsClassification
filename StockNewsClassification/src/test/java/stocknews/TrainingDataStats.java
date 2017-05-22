package stocknews;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import de.uni_koeln.spinfo.stocknews.classification.StockNewsTrainingDataGenerator;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingData;

public class TrainingDataStats {
	
	@Test
	public void trainingDataStats() throws IOException{
		
		Map<Integer,Integer> classDistr = new TreeMap<Integer,Integer>();
		
		File tdFile =  new File("output/classification/trainingData.txt");
		List<TrainingData> trainingData = StockNewsTrainingDataGenerator.readTrainingData(tdFile);
		
		for(TrainingData td : trainingData){
			int eval = td.getEvaluation();
			int count = classDistr.containsKey(eval) ? classDistr.get(eval) : 0;
			classDistr.put(eval, count + 1);
		}
		
		System.out.println(classDistr);
	}
	

}
