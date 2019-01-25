package weka.main;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.utils.UtilsWeka;

public class Step06_RemoveFeatureSelectionAtt {

	public static void removeFeatureSelectionAtt(String trainFileName, String testFileName, String options) {

		String dir = "./files/";

		String trainFile = trainFileName;

		String testFile = testFileName;

		String[] op = new String[] { "-V", "-R", options };

		String[] optionsTrain = op.clone();

		String[] optionsTest = op.clone();

		try {

			/**
			 * Remove attributes from train
			 */
			Instances trainData = UtilsWeka.readArff(dir + trainFile);

			Remove remove = new Remove();

			remove.setOptions(optionsTrain);

			remove.setInputFormat(trainData);

			Instances newTrainData = Filter.useFilter(trainData, remove);

			UtilsWeka.writeArffFile(newTrainData, dir, trainFileName.substring(0, trainFileName.indexOf(".")) + "-FS.arff");

			/**
			 * Remove attributes from test
			 */
			Instances testData = UtilsWeka.readArff(dir + testFile);

			Remove removeTest = new Remove();

			removeTest.setOptions(optionsTest);

			removeTest.setInputFormat(testData);

			Instances newTestData = Filter.useFilter(testData, removeTest);

			UtilsWeka.writeArffFile(newTestData, dir, testFileName.substring(0, testFileName.indexOf(".")) + "-FS.arff");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with removeFeatureSelectionAtt for " + trainFile);
	}

	public static void main(String[] args) {

		removeFeatureSelectionAtt(args[0], args[1], args[2]);
	}
}
