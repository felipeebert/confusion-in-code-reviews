package weka.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class UtilsWeka {
	
	public static void saveResults(Evaluation eval, StringBuffer predsBuffer, String classifier, String fileName)
			throws Exception {

		String breakLine = "\n\n=======================\n\n";

		StringBuffer results = new StringBuffer();

		results.append(classifier + breakLine);

		results.append("inst#     actual  predicted error prediction\n");

		results.append(predsBuffer.toString() + breakLine);

		results.append(eval.toSummaryString("\nSummary\n======\n", false));

		results.append(breakLine);

		results.append(eval.toClassDetailsString() + breakLine);

		results.append("=== Confusion Matrix ===\n");

		results.append("a    b    <-- classified as\n");

		Double a = eval.confusionMatrix()[0][0];

		Double b = eval.confusionMatrix()[0][1];

		Double c = eval.confusionMatrix()[1][0];

		Double d = eval.confusionMatrix()[1][1];

		results.append(a.intValue() + "   " + b.intValue() + "    |   a = confusion\n");

		results.append(c.intValue() + "   " + d.intValue() + "   |   b = no_confusion\n");

		Utilities.saveFile(results.toString(), fileName);
	}

	public static Instances readArff(String file) {

		Instances data = null;

		try {

			DataSource source = new DataSource(file);

			data = source.getDataSet();

			// set the class as confusion
			data.setClassIndex(data.attribute("confusionclass").index());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static Instances mergeInstances(Instances data1, Instances data2) throws Exception {

		// Check where are the string attributes
		int asize = data1.numAttributes();

		boolean strings_pos[] = new boolean[asize];

		for (int i = 0; i < asize; i++) {

			Attribute att = data1.attribute(i);

			strings_pos[i] = ((att.type() == Attribute.STRING) || (att.type() == Attribute.NOMINAL));
		}

		// Create a new dataset
		Instances dest = new Instances(data1);

		dest.setRelationName(data1.relationName() + "+" + data2.relationName());

		DataSource source = new DataSource(data2);

		Instances instances = source.getStructure();

		Instance instance = null;

		while (source.hasMoreElements(instances)) {

			instance = source.nextElement(instances);

			dest.add(instance);

			// Copy string attributes
			for (int i = 0; i < asize; i++) {

				if (strings_pos[i]) {

					dest.instance(dest.numInstances() - 1).setValue(i, instance.stringValue(i));
				}
			}
		}

		return dest;
	}
	
	public static void writeArffFile(Instances data, String dir, String fileName) throws IOException {

		ArffSaver saver = new ArffSaver();

		saver.setInstances(data);

		saver.setFile(new File(dir + fileName));

		saver.writeBatch();

		System.out.println("saved " + fileName + ".arff with " + data.numInstances() + " instances and "
				+ data.numAttributes() + " attributes!");
	}

	public static Instances removeCommentID(Instances data) throws Exception {

		String indexCommentID = String.valueOf(data.attribute("comment_id").index() + 1);

		Remove remove = new Remove();

		remove.setAttributeIndices(indexCommentID);

		remove.setInputFormat(data);

		return Filter.useFilter(data, remove);
	}
	
public static String[] parseRemoveOptions(String options) {
		
		char[] chars = options.toCharArray();

		List<String> cmds = new ArrayList<String>();

		String tmp = "";

		for (int i = 0; i < chars.length; i++) {

			if (chars[i] == '-') {

				tmp = tmp + chars[i];
			}

			if (chars[i] == 'V') {

				tmp = tmp + chars[i];

				cmds.add(tmp);

				tmp = "";
			}

			if (chars[i] == 'R') {

				tmp = tmp + chars[i];
				
				cmds.add(tmp);
				
				tmp = options.substring(i + 1);
				
				cmds.add(tmp);

				tmp = "";

				i = i + 3;
			}
		}
		
		return cmds.toArray(new String[cmds.size()]);
	}
}
