package weka.main;

import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.utils.UtilsWeka;

/**
 * This class will pre-process the data with the following filters:
 * 
 * Step 1. StringToWordVector
 * 
 * @author Felipe
 *
 */
public class Step02_RunSTWVFilter {

	public static void runSTWVFilter(String fileName) {

		String dir = "./files/";

		try {

			Instances train = UtilsWeka.readArff(dir + fileName);

			StringToWordVector filter = new StringToWordVector();

			String stringOptions = "-R first -W 1000000000 -prune-rate -1.0 -T -I -N 0 -L "
					+ "-stemmer weka.core.stemmers.SnowballStemmer "
					+ "-stopwords-handler weka.core.stopwords.Rainbow -M 1 "
					+ "-tokenizer \"weka.core.tokenizers.NGramTokenizer -max 2 -min 1 -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\'\\\\\\\"()?!\\\"\"";
			
			String[] filterOptions = Utils.splitOptions(stringOptions);

			filter.setOptions(filterOptions);

			filter.setInputFormat(train);

			Instances trainSTWV = Filter.useFilter(train, filter);

			trainSTWV.setClassIndex(trainSTWV.attribute("confusionclass").index());

			UtilsWeka.writeArffFile(trainSTWV, dir, fileName.substring(0, fileName.indexOf(".")) + "-STWV.arff");

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done with runSTWVFilter for " + fileName);
	}

	public static void main(String[] args) {

		runSTWVFilter(args[0]);
	}
}
