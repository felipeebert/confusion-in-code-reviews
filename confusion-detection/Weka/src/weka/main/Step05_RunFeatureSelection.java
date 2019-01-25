package weka.main;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;
import weka.utils.Utilities;
import weka.utils.UtilsWeka;

/**
 * This class run feature selection by using WrapperSubsetEval evaluator.
 * 
 * It is intended to be run on a specific server, that's why there is no
 * specific directory.
 * 
 * @author Felipe
 *
 */
public class Step05_RunFeatureSelection {

	public static void runFeatureSelection(String fileName) {

		String inputDir = "./files/";

		String outputDir = "./results/";
		
		String outputFile = fileName.substring(0, fileName.indexOf(".")) + "-FS-results.txt";
		
		System.out.println("Running feature selection for " + fileName);

		try {

			Instances data = UtilsWeka.readArff(inputDir + fileName);

			AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();

			WrapperSubsetEval eval = new WrapperSubsetEval();

			Classifier baseClassifier = AbstractClassifier.forName("weka.classifiers.trees.J48",
					new String[] { "-C", "0.25", "-M", "2" });

			eval.setClassifier(baseClassifier);

			eval.setSeed(123);

			BestFirst search = new BestFirst();

			classifier.setEvaluator(eval);

			classifier.setSearch(search);

			classifier.setClassifier(baseClassifier);

			classifier.buildClassifier(data);

			String result = classifier.toString();

			Utilities.saveFile(result, outputDir + outputFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with runFeatureSelection for " + fileName);
	}

	public static void main(String[] args) {

		runFeatureSelection(args[0]);
	}
}
