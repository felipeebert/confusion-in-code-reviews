package weka.main;

import java.util.Random;

import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.utils.UtilsWeka;

/**
 * This class split the gold-standard-set file with stratification: 70% for
 * training and 30% for testing.
 * 
 * It creates .arff files for both train and test sets: one without commentID
 * and another with the commentID.
 * 
 * @author Felipe
 *
 */
public class Step03_SplitTrainTest {

	private static void splitTrainTest(String fileName) {

		String dir = "./files/";

		try {

			Instances data = UtilsWeka.readArff(dir + fileName);

			/**
			 * Step 1: create train.arff file
			 */
			StratifiedRemoveFolds filterTrain = new StratifiedRemoveFolds();

			String[] optionsTrain = Utils.splitOptions("-N 10 -F 1 -S 123");

			filterTrain.setOptions(optionsTrain);

			filterTrain.setInputFormat(data);

			Instances train = Filter.useFilter(data, filterTrain);

			train.setClassIndex(train.attribute("confusionclass").index());

			for (int i = 2; i < 8; i++) {

				optionsTrain = Utils.splitOptions("-N 10 -F " + i + " -S 123");

				filterTrain.setOptions(optionsTrain);

				filterTrain.setInputFormat(data);

				Instances tmp = Filter.useFilter(data, filterTrain);

				tmp.setClassIndex(tmp.attribute("confusionclass").index());

				train = UtilsWeka.mergeInstances(train, tmp);
			}

			train.randomize(new Random(123));
			
			UtilsWeka.writeArffFile(train, dir, fileName.substring(0, fileName.indexOf(".")) + "-train-ID.arff");

			Instances train_no_ID = UtilsWeka.removeCommentID(train);

			UtilsWeka.writeArffFile(train_no_ID, dir, fileName.substring(0, fileName.indexOf(".")) + "-train.arff");

			/**
			 * Step 2: create test.arrf file
			 */
			StratifiedRemoveFolds filterTest = new StratifiedRemoveFolds();

			String[] optionsTest = Utils.splitOptions("-N 10 -F 8 -S 123");

			filterTest.setOptions(optionsTest);

			filterTest.setInputFormat(data);

			Instances test = Filter.useFilter(data, filterTest);

			test.setClassIndex(test.attribute("confusionclass").index());

			for (int i = 9; i < 11; i++) {

				optionsTest = Utils.splitOptions("-N 10 -F " + i + " -S 123");

				filterTest.setOptions(optionsTest);

				filterTest.setInputFormat(data);

				Instances tmp = Filter.useFilter(data, filterTest);

				tmp.setClassIndex(tmp.attribute("confusionclass").index());

				test = UtilsWeka.mergeInstances(test, tmp);
			}

			test.randomize(new Random(123));
			
			UtilsWeka.writeArffFile(test, dir, fileName.substring(0, fileName.indexOf(".")) + "-test-ID.arff");

			Instances test_no_ID = UtilsWeka.removeCommentID(test);

			UtilsWeka.writeArffFile(test_no_ID, dir, fileName.substring(0, fileName.indexOf(".")) + "-test.arff");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with splitTrainTest for " + fileName);
	}

	public static void main(String[] args) {

		splitTrainTest(args[0]);
	}
}
