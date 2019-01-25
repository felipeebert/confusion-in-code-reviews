package weka.main;

import weka.classifiers.meta.AutoWEKAClassifier;
import weka.core.Instances;
import weka.utils.Utilities;
import weka.utils.UtilsWeka;

public class Step07_RunAutoWeka {

	public static void runAutoWeka(String fileName) {

		String inputDir = "./files/";

		String outputDir = "./results/";

		System.out.println("Started Auto-Weka for: " + fileName);
		
		try {

			Instances train = UtilsWeka.readArff(inputDir + fileName);
			
			AutoWEKAClassifier autoWeka = new AutoWEKAClassifier();

			autoWeka.setSeed(123);

			autoWeka.setTimeLimit(120);

			autoWeka.setMemLimit(2048);

			autoWeka.setnBestConfigs(1);
			
			autoWeka.setParallelRuns(1);

			autoWeka.buildClassifier(train);

			StringBuffer sb = new StringBuffer();

			sb.append(autoWeka.toString());

			Utilities.saveFile(sb.toString(), outputDir + "autoweka-" + fileName.substring(0, fileName.indexOf(".")) + "-FS.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Don with runAutoWeka for " + fileName);
	}

	public static void main(String[] args) {
		
		runAutoWeka(args[0]);
	}
}
