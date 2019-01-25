package weka.main;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.utils.UtilsWeka;

public class Step04_RunSMOTE {
	
	public static void runSMOTE(String fileName, String percentage) {
		
		String dir = "./files/";

		System.out.println("Running SMOTE for " + fileName);
		
		try {

			Instances unbalancedData = UtilsWeka.readArff(dir + fileName);
			
			Instances balancedData = unbalancedData;
			
			SMOTE smote = new SMOTE();

			smote.setRandomSeed(123);
			
			smote.setPercentage(Integer.parseInt(percentage));

			smote.setInputFormat(unbalancedData);

			balancedData = Filter.useFilter(balancedData, smote);
			
			balancedData.setClassIndex(balancedData.attribute("confusionclass").index());

			UtilsWeka.writeArffFile(balancedData, dir, fileName.substring(0, fileName.indexOf(".")) + "-SMOTE.arff");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with runSMOTE for " + fileName);
	}

	public static void main(String[] args) {

		runSMOTE(args[0], args[1]);
	}
}
