package weka.main;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.utils.UtilsWeka;

public class Step08_TrainTestModel {

	public static void trainTestModel(String trainFileName, String testFileName, Classifier classifier) {

		String dirArff = "./files/";

		String dirResults = "./results/";

		String trainFile = trainFileName;

		String testFile = testFileName;

		System.out.println("Started training and testing for: " + trainFile);

		try {

			Instances train = UtilsWeka.readArff(dirArff + trainFile);

			Instances test = UtilsWeka.readArff(dirArff + testFile);

			classifier.buildClassifier(train);

			SerializationHelper.write(dirResults + "model-" + trainFileName.substring(0, trainFileName.indexOf(".")) + ".model", classifier);

			Evaluation evaluation = new Evaluation(train);

			StringBuffer predsBuffer = new StringBuffer();

			CSV csv = new CSV();

			csv.setHeader(test);

			csv.setBuffer(predsBuffer);

			evaluation.evaluateModel(classifier, test, csv);

			UtilsWeka.saveResults(evaluation, predsBuffer, classifier.toString(),
					dirResults + "results-" + trainFileName.substring(0, trainFileName.indexOf(".")) + ".txt");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with trainModel for " + trainFile);
	}

	public static void main(String[] args) {

		try {

			// Copy the classifier line from the result of Auto-Weka!
			Classifier classifier = AbstractClassifier.forName("weka.classifiers.lazy.LWL",
					new String[] { "-A", "weka.core.neighboursearch.LinearNNSearch", "-W",
							"weka.classifiers.rules.JRip", "--", "-N", "3.178308398714082", "-E", "-O", "4" });

			trainTestModel(args[0], args[1], classifier);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
