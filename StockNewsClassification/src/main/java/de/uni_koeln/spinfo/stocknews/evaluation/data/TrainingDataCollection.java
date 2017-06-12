package de.uni_koeln.spinfo.stocknews.evaluation.data;

import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.stocknews.stocks.data.Trend;

public class TrainingDataCollection {
	
	private String analysingMethod;
	private Map<Trend,Integer> classes;
	private List<String> coveredRics;
	private List<TrainingData> trainingData;
	
	public TrainingDataCollection(String analysingMethod,
			Map<Trend, Integer> classes, List<String> coveredRics,
			List<TrainingData> trainingData) {
		super();
		this.analysingMethod = analysingMethod;
		this.classes = classes;
		this.coveredRics = coveredRics;
		this.trainingData = trainingData;
	}

	public String getAnalysingMethod() {
		return analysingMethod;
	}

	public Map<Trend, Integer> getClasses() {
		return classes;
	}

	public List<String> getCoveredRics() {
		return coveredRics;
	}

	public List<TrainingData> getTrainingData() {
		return trainingData;
	}
	
	
	

}
