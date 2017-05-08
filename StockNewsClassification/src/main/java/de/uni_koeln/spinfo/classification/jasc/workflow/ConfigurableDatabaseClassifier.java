package de.uni_koeln.spinfo.classification.jasc.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ConfigurableDatabaseClassifier {

	private Connection inputDb, corrConnection, origConnection, trainingDb;
	int queryLimit, fetchSize, currentId;
	private boolean trainWithDB;
	private boolean trainWithFile;
	private String trainingDataFileName;
	private ZoneJobs jobs;

	public ConfigurableDatabaseClassifier(Connection inputDb, Connection corrConnection, Connection origConnection, Connection trainingDb,
			int queryLimit, int fetchSize, int currentId, boolean trainWithDB, boolean trainWithFile,
			String trainingDataFileName) throws IOException {
		this.inputDb = inputDb;
		this.corrConnection = corrConnection;
		this.origConnection = origConnection;
		this.trainingDb = trainingDb;
		this.queryLimit = queryLimit;
		this.fetchSize = fetchSize;
		this.currentId = currentId;
		this.trainWithDB = trainWithDB;
		this.trainWithFile = trainWithFile;
		this.trainingDataFileName = trainingDataFileName;

		// set Translations
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		List<Integer> categories = new ArrayList<Integer>();
		categories.add(1);
		categories.add(2);
		translations.put(5, categories);
		categories = new ArrayList<Integer>();
		categories.add(2);
		categories.add(3);
		translations.put(6, categories);
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(6, 4, translations);
		jobs = new ZoneJobs(stmc);
	}

	public void classify(StringBuffer sb) throws ClassNotFoundException, IOException, SQLException {
		// get ExperimentConfiguration
		ExperimentSetupUI ui = new ExperimentSetupUI();
		ExperimentConfiguration expConfig = ui.getExperimentConfiguration(trainingDataFileName);
		if(sb != null){
			System.out.println(sb.toString());
		}
		classify(expConfig);
	}

	private void classify(ExperimentConfiguration config) throws IOException, SQLException, ClassNotFoundException {
		
		

		// get trainingdata from file (and db)
		File trainingDataFile = new File(trainingDataFileName);
		List<ClassifyUnit> trainingData = new ArrayList<ClassifyUnit>();

		if (trainWithFile) {
			trainingData.addAll(jobs.getCategorizedParagraphsFromFile(trainingDataFile,
					config.getFeatureConfiguration().isTreatEncoding()));
			System.out.println("added " + trainingData.size() + " training-paragraphs from training-file ");
			
		}

		if (trainWithDB) {
			List<ClassifyUnit> dbTrainingData = jobs.getCategorizedParagraphsFromDB(trainingDb,
					config.getFeatureConfiguration().isTreatEncoding());
			trainingData.addAll(dbTrainingData);
			System.out.println("added " + dbTrainingData.size() + " training-paragraphs from DB ");
		}
		if (trainingData.size() == 0) {
			System.out.println(
					"\nthere are no training paragraphs in the specified training-DB. \nPlease check configuration and try again");
			System.exit(0);
		}
		System.out.println("overall training paragraphs: " + trainingData.size()+"\n");
		System.out.println("\n...classifying...\n");

		trainingData = jobs.initializeClassifyUnits(trainingData);
		trainingData = jobs.setFeatures(trainingData, config.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, config.getFeatureQuantifier(), null);

		// build model
		Model model = jobs.getNewModelForClassifier(trainingData, config);
		if (config.getModelFileName().contains("/models/")) {
			jobs.exportModel(config.getModelFile(), model);
		}
		// get data from db
		int done = 0;
		String query = null;
		int zeilenNr = 0, jahrgang = 0;
		;
		int jobAdCount = 0;
		int paraCount = 0;
		query = "SELECT ZEILENNR, Jahrgang, STELLENBESCHREIBUNG FROM DL_ALL_Spinfo LIMIT ? OFFSET ?;";

		PreparedStatement prepStmt = inputDb.prepareStatement(query);
		prepStmt.setInt(1, queryLimit);
		prepStmt.setInt(2, currentId);
		prepStmt.setFetchSize(fetchSize);
		// execute
		ResultSet queryResult = prepStmt.executeQuery();

		// total entries to process:
		if (queryLimit < 0) {

			String countQuery = "SELECT COUNT(*) FROM DL_ALL_Spinfo;";
			Statement stmt = inputDb.createStatement();
			ResultSet countResult = stmt.executeQuery(countQuery);
			int tableSize = countResult.getInt(1);
			stmt.close();
			stmt = inputDb.createStatement();
			ResultSet rs = null;
			rs = stmt.executeQuery("SELECT COALESCE(" + tableSize + "+1, 0) FROM DL_ALL_Spinfo;");

			queryLimit = rs.getInt(1);
		}

		boolean goOn = true;
		boolean askAgain = true;
		long start = System.currentTimeMillis();
		
		while (queryResult.next() && goOn) {
			jobAdCount++;
			String jobAd = null;
			zeilenNr = queryResult.getInt("ZEILENNR");
			jahrgang = queryResult.getInt("Jahrgang");
			jobAd = queryResult.getString("STELLENBESCHREIBUNG");
			// if there is an empty job description, classifying is of no use,
			// so skip
			if (jobAd == null) {
				System.out.println("jobAd ist null");
				continue;
			}
			if (jobAd.isEmpty()) {
				System.out.println("Ist leer!");
				continue;
			}

			// 1. Split into paragraphs and create a ClassifyUnit per paragraph
			List<String> paragraphs = ClassifyUnitSplitter.splitIntoParagraphs(jobAd);
			// if treat enc
			if (config.getFeatureConfiguration().isTreatEncoding()) {
				paragraphs = EncodingProblemTreatment.normalizeEncoding(paragraphs);
			}
			List<ClassifyUnit> classifyUnits = new ArrayList<ClassifyUnit>();
			for (String string : paragraphs) {
				paraCount++;
				classifyUnits.add(new JASCClassifyUnit(string, jahrgang, zeilenNr));
			}
			// prepare ClassifyUnits
			classifyUnits = jobs.initializeClassifyUnits(classifyUnits);
			classifyUnits = jobs.setFeatures(classifyUnits, config.getFeatureConfiguration(), false);
			classifyUnits = jobs.setFeatureVectors(classifyUnits, config.getFeatureQuantifier(), model.getFUOrder());

			// 2. Classify
			RegexClassifier regexClassifier = new RegexClassifier("classification/data/regex.txt");
			Map<ClassifyUnit, boolean[]> preClassified = new HashMap<ClassifyUnit, boolean[]>();
			for (ClassifyUnit cu : classifyUnits) {
				boolean[] classes = regexClassifier.classify(cu, model);
				preClassified.put(cu, classes);
			}
			Map<ClassifyUnit, boolean[]> classified = jobs.classify(classifyUnits, config, model);
			classified = jobs.mergeResults(classified, preClassified);
			classified = jobs.translateClasses(classified);

			List<ClassifyUnit> results = new ArrayList<ClassifyUnit>();
			for (ClassifyUnit cu : classified.keySet()) {
				((ZoneClassifyUnit) cu).setClassIDs(classified.get(cu));
//				System.out.println();
//				System.out.println(cu.getContent());
//				System.out.print("----->  CLASS: ");
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
				results.add(cu);
			}
			DbConnector.insertClassifiedParagraphsinDB(corrConnection, results, jahrgang, zeilenNr, true);
			DbConnector.insertClassifiedParagraphsinDB(origConnection, results, jahrgang, zeilenNr, false);
			// progressbar
			done++;
			// ProgressBar.updateProgress((float) done/queryLimit);

			// time needed
			if (done % fetchSize == 0) {
				long end = System.currentTimeMillis();
				long time = (end - start) / 1000;

				// continue?
				if (askAgain) {

					System.out.println(
							"\n\n" + "continue (c),\n" + "don't interrupt again (d),\n" + "or stop (s) classifying?");

					boolean answered = false;
					while (!answered) {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						String answer = in.readLine();

						if (answer.toLowerCase().trim().equals("c")) {
							goOn = true;
							answered = true;
							System.out.println("\n...classifying...\n");
						} else if (answer.toLowerCase().trim().equals("d")) {
							goOn = true;
							askAgain = false;
							answered = true;
							System.out.println("\n...classifying...\n");
						} else if (answer.toLowerCase().trim().equals("s")) {
							goOn = false;
							answered = true;
						} else {
							System.out.println("C: invalid answer! please try again...");
							System.out.println();
						}
					}
				}
				start = System.currentTimeMillis();
			}
		}
		System.out.println("Classifying was fun! GoodBye!");
	}

}
