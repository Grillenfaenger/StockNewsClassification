package de.uni_koeln.spinfo.stocknews.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.helpers.EncodingProblemTreatment;
import de.uni_koeln.spinfo.classification.db_io.DbConnector;
import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import de.uni_koeln.spinfo.classification.jasc.preprocessing.ClassifyUnitSplitter;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.RegexClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ExperimentSetupUI;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ZoneJobs;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;

public class ConfigurableArticleClassifier{
	
	private List<Article> articles;
	private String trainingDataFileName;
	private ZoneJobs jobs;

	public ConfigurableArticleClassifier(List<Article> articles, String trainingDataFileName, SingleToMultiClassConverter stmc) throws IOException {
		this.articles = articles;
		this.trainingDataFileName = trainingDataFileName;
		this.jobs = new ZoneJobs(stmc);
	}

	public List<ClassifyUnit> classify(StringBuffer sb) throws ClassNotFoundException, IOException, SQLException, NumberFormatException, ParseException {
		// get ExperimentConfiguration
		ExperimentSetupUI ui = new ExperimentSetupUI();
		ExperimentConfiguration expConfig = ui.getExperimentConfiguration(trainingDataFileName);
		if(sb != null){
			System.out.println(sb.toString());
		}
		return classify(expConfig);
	}

	private List<ClassifyUnit> classify(ExperimentConfiguration config) throws IOException, SQLException, ClassNotFoundException, NumberFormatException, ParseException {
		
		List<ClassifyUnit> results = new ArrayList<ClassifyUnit>();
		
		// get trainingdata from file
		File trainingDataFile = new File(trainingDataFileName);
		List<ClassifyUnit> trainingData = new ArrayList<ClassifyUnit>();
		trainingData = jobs.getCategorizedNewsFromFile(trainingDataFile, config.getFeatureConfiguration().isTreatEncoding());
		System.out.println("added " + trainingData.size() + " training-articles from training-file ");
		
		if (trainingData.size() == 0) {
			System.out.println(
					"\nthere are no training articles in the specified trainingDataFile. \nPlease check configuration and try again");
			System.exit(0);
		}
		System.out.println("overall training articles: " + trainingData.size()+"\n");
		System.out.println("\n...classifying...\n");

		trainingData = jobs.initializeClassifyUnits(trainingData);
		trainingData = jobs.setFeatures(trainingData, config.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, config.getFeatureQuantifier(), null);

		// build model
		Model model = jobs.getNewModelForClassifier(trainingData, config);
		if (config.getModelFileName().contains("/models/")) {
			jobs.exportModel(config.getModelFile(), model);
		}
		
		for(Article art: articles){
			// if treat enc
			if (config.getFeatureConfiguration().isTreatEncoding()) {
				art.setContent(EncodingProblemTreatment.normalizeEncoding(art.getContent()));
			}
			
			List<ClassifyUnit> classifyUnits = new ArrayList<ClassifyUnit>();
			ZoneClassifyUnit zcu = new StockNewsClassifyUnit(art, UUID.randomUUID());
			classifyUnits.add(zcu);
			
			// prepare ClassifyUnits
			classifyUnits = jobs.initializeClassifyUnits(classifyUnits);
			classifyUnits = jobs.setFeatures(classifyUnits, config.getFeatureConfiguration(), false);
			classifyUnits = jobs.setFeatureVectors(classifyUnits, config.getFeatureQuantifier(), model.getFUOrder());

			// 2. Classify
			Map<ClassifyUnit, boolean[]> classified = jobs.classify(classifyUnits, config, model);
			classified = jobs.translateClasses(classified);

			
			
			for (ClassifyUnit cu : classified.keySet()) {
				((ZoneClassifyUnit) cu).setClassIDs(classified.get(cu));

				boolean[] ids = ((ZoneClassifyUnit) cu).getClassIDs();
				boolean b = false;
				for (int i = 0; i < ids.length; i++) {
					if (ids[i]) {
						if (b) {
							//System.out.print("& " + (i + 1));
						} else {
							//System.out.println((i + 1));
						}
						b = true;
					}
				}
				
				System.out.println(art.printComplete());
				int classi = ((ZoneClassifyUnit) cu).getActualClassID();
				
				if(classi == 2) {
					System.out.println("steigt");
				}
				else if(classi == 1) {
					System.out.println("fällt");
				}
				results.add(cu);
				
			}			
		}
		return results;		
	}

}
