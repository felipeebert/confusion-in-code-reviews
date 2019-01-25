package weka.main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import weka.utils.Utilities;

/**
 * This class builds the .arff file and count each feature from the confusion
 * framework.
 * 
 * It will also do several pre-processing steps in the text, such as remove
 * quoted texts, break lines, corrupted chars, etc.
 * 
 * It reads the .xls file and create the .arff file.
 * 
 * You need to set two things:
 * 
 * 1) input file: comment.xls
 * 
 * The spreadsheet should be in the format: ID, Label, Comment
 * 
 * 2) ouput file: comment.xls.arff
 * 
 * @author Felipe
 *
 */
public class Step01_BuildArff {

	public static void buildArff(String fileName) {

		String file = "./files/" + fileName;

		String outputFile = "./files/" + fileName.substring(0, fileName.indexOf(".")) + ".arff";

		System.out.println("Started building arff for: " + file);

		List<String> cuesFramework = Utilities.initFramework();

		HashSet<String> nameList = Utilities.readNameListFiles();

		Workbook workbook = null;

		Properties props = new Properties();

		props.put("annotators", "tokenize, ssplit, parse");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		try {

			StringBuffer sbArff = new StringBuffer();

			sbArff.append("@relation framework\n\n");

			sbArff.append("@attribute 'comment' string\n");

			Utilities.appendCuesHeader(sbArff, cuesFramework);

			sbArff.append("@attribute 'framework_hedges' numeric\n");
			sbArff.append("@attribute 'framework_hypotheticals' numeric\n");
			sbArff.append("@attribute 'framework_I_statements' numeric\n");
			sbArff.append("@attribute 'framework_meta' numeric\n");
			sbArff.append("@attribute 'framework_nonverbals' numeric\n");
			sbArff.append("@attribute 'framework_probables' numeric\n");
			sbArff.append("@attribute 'framework_questions' numeric\n");

			sbArff.append("@attribute 'framework_SBARQ' numeric\n");
			sbArff.append("@attribute 'framework_SQ' numeric\n");
			sbArff.append("@attribute 'framework_question_mark' numeric\n");

			sbArff.append("@attribute 'comment_id' string\n");

			sbArff.append("@attribute 'confusionclass' {confusion, no_confusion}\n\n");

			sbArff.append("@data\n");

			workbook = Workbook.getWorkbook(new File(file));

			Sheet sheet = workbook.getSheet(0);

			// start reading the xls at 1 to skip header!
			for (int i = 1; i < sheet.getRows(); i++) {

				// read the commentID
				Cell cell0 = sheet.getCell(0, i);
				String id = cell0.getContents();

				// read the label
				Cell cell1 = sheet.getCell(1, i);
				String label = cell1.getContents();

				// read the comment
				Cell cell2 = sheet.getCell(2, i);
				String comment = cell2.getContents();

				// remove quoted texts
				comment = Utilities.removeQuotedTexts(comment);

				// remove break lines: "\\r\\n|\\r|\\n" ==> " "
				comment = Utilities.replaceBreakLines(comment);

				// remove corrupted chars
				comment = Utilities.removeCorruptedChars(comment);

				// remove XML tags
				comment = Utilities.removeXMLTags(comment);

				// replace patch set
				comment = Utilities.replacePatchSet(comment);

				// replace URLs
				comment = Utilities.replaceURLs(comment);

				// replace names
				comment = Utilities.replaceNames(comment, nameList);

				// replace commits SHA1
				comment = Utilities.replaceCommitSha1(comment);

				// replace numbers
				comment = Utilities.replaceNumbers(comment);

				Annotation document = new Annotation(comment);

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				int nSBARQ = 0;

				int nSQ = 0;

				for (CoreMap sentence : sentences) {

					Tree tree = sentence.get(TreeAnnotation.class);

					Tree c = tree.getChild(0);

					if (c.label().toString().equalsIgnoreCase("SBARQ")) {

						nSBARQ = nSBARQ + 1;

					} else if (c.label().toString().equalsIgnoreCase("SQ")) {

						nSQ = nSQ + 1;
					}
				}

				int nQuestionMark = Utilities.countQuestionMark(comment);

				int questions = nSBARQ + nSQ;

				String newComment = Utilities.handleContractionsPerfectContinous(comment);

				newComment = Utilities.handleContractions(newComment);

				newComment = Utilities.handleWhats(newComment);

				String cuesCounting = Utilities.countCues(newComment, cuesFramework);

				newComment = Utilities.handleNegativeQuestions(newComment);

				newComment = Utilities.handleRemainingContractions(newComment);

				// remove all quotes: single and double
				newComment = Utilities.removeQuotes(newComment);

				// remove chars: =, -, &, #, *, $, +, [, ]
				newComment = Utilities.removeChars(newComment);

				newComment = Utilities.handleStopWordsAndNegation(newComment);

				newComment = Utilities.handleRemainingStopWords(newComment);

				String finalString = "\"" + newComment + "\"," + cuesCounting + "," + questions + "," + nSBARQ + ","
						+ nSQ + "," + nQuestionMark + ",\"" + id + "\"," + label + "\n";

				sbArff.append(finalString);
			}

			Utilities.saveFile(sbArff.toString(), outputFile);

		} catch (IOException | BiffException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		System.out.println("Done with buildArff for " + outputFile);
	}

	public static void main(String[] args) {

		buildArff(args[0]);
	}
}
