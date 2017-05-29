package de.uni_koeln.spinfo.stocknews.classification.workflow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

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

public class WekaClassifier {
	
	// Bag of Words
	TreeMap<Integer,TreeMap<String,Integer>> bow;
	
	//String representation of bow
	TreeMap<Integer,String> bowStrings;
	
	// Trainingdata Filename
	String tdFilename;
	
	// output folder
		String outputDir;
	
	// Name of specified classifier
	String classifierName;
	
	private ZoneJobs jobs;
	private ExperimentConfiguration expConfig;
	
	public WekaClassifier(TreeMap<Integer,TreeMap<String,Integer>> bow, String tdFilename, String classifierName) throws IOException {
		
		// input/output anders regeln!!
		
		this.bow = bow;
		this.bowStrings = buildBowString(bow);
		this.tdFilename = tdFilename;
		this.outputDir = tdFilename.substring(0,tdFilename.lastIndexOf("/"));	
		
		// initialize singleToMultiClassConerter
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		List<Integer> categories = new ArrayList<Integer>();
		categories.add(1);
		translations.put(1, categories);
		categories = new ArrayList<Integer>();
		categories.add(2);
		translations.put(2, categories);
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(2, 2, translations);
		
		this.jobs = new ZoneJobs(stmc);
		this.expConfig = getExperimentConfiguration(classifierName);
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
		ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, quantifier, classifier, new File(tdFilename),
				outputDir);
		
		return expConfig;
	}

	private TreeMap<Integer, String> buildBowString(
			TreeMap<Integer, TreeMap<String, Integer>> bow) {
		TreeMap<Integer,String> sMap = new TreeMap<Integer,String>();
		StringBuffer sb = new StringBuffer();
			for(Integer key : bow.keySet()){
				TreeMap<String, Integer> article = bow.get(key); 
				for(String s : article.keySet()){
					for(int i = 0; i<article.get(s); i++){
						sb.append(s);
						sb.append(" ");
					}
				}
				System.out.println(sb.toString());
				sMap.put(key, sb.toString());
			}
		return sMap;
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

	public TreeMap<Integer,ClassifyUnit> classify() throws IOException, SQLException, ClassNotFoundException, NumberFormatException, ParseException {
		
		List<ClassifyUnit> results = new ArrayList<ClassifyUnit>();
		
		// get trainingdata from file
		File trainingDataFile = new File(tdFilename);
		List<ClassifyUnit> trainingData = new ArrayList<ClassifyUnit>();
		trainingData = jobs.getCategorizedNewsFromFile(trainingDataFile, expConfig.getFeatureConfiguration().isTreatEncoding());
		System.out.println("added " + trainingData.size() + " training-articles from training-file ");
		
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
		
		for(Integer key : bowStrings.keySet()){
			ZoneClassifyUnit zcu = new ZoneClassifyUnit(bowStrings.get(key));
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
				
		
				results.add(cu);
			
		}
			
		TreeMap<Integer,ClassifyUnit> cMap = new TreeMap<Integer,ClassifyUnit>();
		HashMap<Integer,ClassifyUnit> tempMap = new HashMap<Integer,ClassifyUnit>();
		int i = 0;
		for(Integer key : bowStrings.keySet()){
			System.out.println(key);
			tempMap.put(key,results.get(i));
			i++;
		}
		cMap.putAll(tempMap);
		
		for(Integer key: bowStrings.keySet()){
			System.out.println(key + " ," + bowStrings.get(key) + " ; \nresult: " + cMap.get(key).getContent());
		}
		
		
		return cMap;		
	}
	
	

}
