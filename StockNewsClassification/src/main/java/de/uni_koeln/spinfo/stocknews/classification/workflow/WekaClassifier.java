package de.uni_koeln.spinfo.stocknews.classification.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.TFIDFFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.helpers.EncodingProblemTreatment;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneAbstractClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneKNNClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneNaiveBayesClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneRocchioClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.svm.SVMClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ExperimentSetupUI;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ZoneJobs;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;
import de.uni_koeln.spinfo.stocknews.classification.StockNewsClassifyUnit;
import de.uni_koeln.spinfo.stocknews.evaluation.data.TrainingDataCollection;
import de.uni_koeln.spinfo.stocknews.stocks.data.Trend;

public class WekaClassifier {
	
	// texts to classify
	TreeMap<Integer,String> texts;
	
	// Trainingdata File
	File tdFile;
	TrainingDataCollection tdColl;
	
	// output folder
		String outputDir;
	
	// Name of specified classifier
	String classifierName;
	
	private ZoneJobs jobs;
	private ExperimentConfiguration expConfig;
	
	public WekaClassifier(TreeMap<Integer, String> texts, File tdFile, String classifierName) throws IOException {
		
		this.texts = texts;
		
		this.tdFile = tdFile;
		String filePath = tdFile.getAbsolutePath();
		deserializeTDFile();
		this.outputDir = filePath.substring(0,filePath.lastIndexOf("\\"));
		
		// initialize singleToMultiClassConerter
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		
		for(Trend trend : tdColl.getClasses().keySet()){
			List<Integer> categories = new ArrayList<Integer>();
			int classNr = tdColl.getClasses().get(trend);
			categories.add(classNr);
			translations.put(classNr, categories);
		}
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(tdColl.getClasses().size(), tdColl.getClasses().size(), translations);
		
		this.jobs = new ZoneJobs(stmc);
		this.expConfig = getExperimentConfiguration(classifierName);
	}

	private void deserializeTDFile() throws FileNotFoundException, IOException {
		final Gson gson = new Gson();
		try (Reader reader = new FileReader(tdFile.getAbsolutePath())) {
			tdColl = gson.fromJson(reader, TrainingDataCollection.class);
		}
	}

	private ExperimentConfiguration getExperimentConfiguration(String classifierName) {
		ZoneAbstractClassifier classifier = getClassifier(classifierName);
		
		boolean ignoreStopwords = false;
		boolean normalizeInput = false;
		boolean useStemmer = false;
		int[] nGrams = null;
		int miScoredFeaturesPerClass = 0;
		
		boolean suffixTrees = false;
		
		int knnValue = 4;
		AbstractFeatureQuantifier quantifier = new TFIDFFeatureQuantifier();
		Distance distance = Distance.COSINUS;
		
		if(!(classifier instanceof ZoneNaiveBayesClassifier) && !(classifier instanceof SVMClassifier)){
			classifier.setDistance(distance);
		}
		if (classifier instanceof ZoneKNNClassifier) {
			((ZoneKNNClassifier) classifier).setK(knnValue);
		}
		
		if ((classifier instanceof ZoneNaiveBayesClassifier)) {
			quantifier = null;
		} 
		
		FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalizeInput, useStemmer, ignoreStopwords, nGrams,
				false, miScoredFeaturesPerClass, suffixTrees);
		ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, quantifier, classifier, tdFile,
				outputDir);
		
		return expConfig;
	}

	private ZoneAbstractClassifier getClassifier(String classifierName) {
		
		switch(classifierName){
		case "KNNClassifier" :
			return new ZoneKNNClassifier();
		case "RocchioClassifier" : 
			return new ZoneRocchioClassifier();
		case "BayesClassifier" : 
			return new ZoneNaiveBayesClassifier();
		default : 
			return new ZoneNaiveBayesClassifier();	
		}
	}

	public TreeMap<Integer,ZoneClassifyUnit> classify() throws IOException, SQLException, ClassNotFoundException, NumberFormatException, ParseException {
		
		List<ZoneClassifyUnit> results = new ArrayList<ZoneClassifyUnit>();
		
		// get trainingdata from file
		List<ClassifyUnit> trainingData = new ArrayList<ClassifyUnit>();
		trainingData = jobs.getCategorizedNewsFromTdCollection(tdColl, tdFile, expConfig.getFeatureConfiguration().isTreatEncoding());
		System.out.println("Trainingdata generated with " + tdColl.getAnalysingMethod());
		System.out.println("classes: " + tdColl.getClasses());
		
		if (trainingData.size() == 0) {
			System.out.println(
					"\nthere are no training articles in the specified trainingDataFile. \nPlease check configuration and try again");
			System.exit(0);
		}
		System.out.println("overall training articles: " + trainingData.size()+"\n");
		System.out.println("\n...classifying...\n");

		trainingData = jobs.initializeClassifyUnits(trainingData);
		trainingData = jobs.setFeatures(trainingData, expConfig.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, expConfig.getFeatureQuantifier(), null);

		// build model
		Model model = jobs.getNewModelForClassifier(trainingData, expConfig);
		if (expConfig.getModelFileName().contains("/models/")) {
			jobs.exportModel(expConfig.getModelFile(), model);
		}
		
		List<ClassifyUnit> classifyUnits = new ArrayList<ClassifyUnit>();
		
		for(Integer key : texts.keySet()){
			ZoneClassifyUnit zcu = new ZoneClassifyUnit(texts.get(key));
			classifyUnits.add(zcu);
		}

			// prepare ClassifyUnits
			classifyUnits = jobs.initializeClassifyUnits(classifyUnits);
			classifyUnits = jobs.setFeatures(classifyUnits, expConfig.getFeatureConfiguration(), false);
			classifyUnits = jobs.setFeatureVectors(classifyUnits, expConfig.getFeatureQuantifier(), model.getFUOrder());

		
			// 2. Classify
			Map<ClassifyUnit, boolean[]> classified = jobs.classify(classifyUnits, expConfig, model);
			classified = jobs.translateClasses(classified);
	
			
			for (ClassifyUnit cu : classified.keySet()) {
				((ZoneClassifyUnit) cu).setClassIDs(classified.get(cu));
	
	//				boolean[] ids = ((ZoneClassifyUnit) cu).getClassIDs();
	//				boolean b = false;
	//				for (int i = 0; i < ids.length; i++) {
	//					if (ids[i]) {
	//						if (b) {
	//							//System.out.print("& " + (i + 1));
	//						} else {
	//							//System.out.println((i + 1));
	//						}
	//						b = true;
	//					}
	//				}
				
		
				results.add((ZoneClassifyUnit)cu);
			
		}
			
		TreeMap<Integer,ZoneClassifyUnit> cMap = new TreeMap<Integer,ZoneClassifyUnit>();
		HashMap<Integer,ZoneClassifyUnit> tempMap = new HashMap<Integer,ZoneClassifyUnit>();
//		int i = 0;
		System.out.println("results: " + results.size());
		System.out.println("texts: " + texts.size());
		for(Integer key : texts.keySet()){
			tempMap.put(key,results.get(key));
//			i++;
//			System.out.println(i);
		}
		cMap.putAll(tempMap);	
		
		return cMap;		
	}
	
	

}
