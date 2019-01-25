package weka.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class Utilities {

	static String pronouns = "(i|you|he|she|it|we|they|there|that|this|which)";

	static Pattern Contractions = Pattern.compile(
			"(?i)" + pronouns + "((\\s+(do|does|did|are|was|were|has|have|had|is|ca|wo|would|should|could)n(['’′])?t)"
					+ "|n['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)");

	static Pattern Contractions_perfect = Pattern.compile("(?i)" + pronouns + "(['’′](s|d|ve)(\\s+not)?(\\s+been))");

	static Pattern Contractions_whats = Pattern.compile("(?i)(what's)");

	static Pattern Negation_old = Pattern
			.compile("(?i)^(((never|no|nothing|nowhere|noone|none|not|havent|hasnt|hadnt|cant|couldnt|shouldnt"
					+ "|wont|wouldnt|dont|doesnt|didnt|isnt|arent|aint)$|n(['’′])?t)|cannot)");

	static Pattern Negation = Pattern.compile("(?i)^((never|no|nothing|nowhere|noone|none|not|cannot)|"
			+ "(do|does|did|are|was|were|has|have|had|is|ca|wo|would|should|could|ai)n(['’′])?t)$");

	static Pattern Ponctuation = Pattern.compile("^[.:;!?]+$");

	static Pattern Ponctuation_ignore = Pattern.compile("^[-,\\\\)+=~\\(\\\\{\\\\}<>\\\\\"'/|]+$");

	static Pattern Ponctuation_all = Pattern.compile("^[.,:;!?]+$");

	static String neg = "_NEG";
	
	static Pattern Negative_Stopwords = Pattern.compile("(?i)(cannot|never|no|none|noone|not|nothing|nowhere)");

	static Pattern Contractions_neg_questions = Pattern
			.compile("(?i)((do|does|did|are|was|were|has|have|had|is|ca|wo|would|should)n(['’′])?t)");

	static Pattern Contractions_neg_questions_strict = Pattern
			.compile("(?i)^((do|does|did|are|was|were|has|have|had|is|ca|wo|would|should)n(['’′])?t)$");

	static Pattern Auxiliars = Pattern
			.compile("(do|does|did|are|was|were|has|have|had|is|can|can|wo|would|should|could)");
	
	static Pattern StopWordsRemaining = Pattern
			.compile("(?i)^(don|doesn|didn|aren|wasn|weren|hasn|haven|hadn|isn|wouldn|shouldn|couldn)$");

	public static List<String> initFramework() {

		List<String> list = new ArrayList<String>();

		List<String> filesToRead = new ArrayList<String>();

		filesToRead.add("hedges-sources.txt");
		filesToRead.add("hypotheticals-sources.txt");
		filesToRead.add("I_statements-sources.txt");
		filesToRead.add("meta-sources.txt");
		filesToRead.add("nonverbals-sources.txt");
		filesToRead.add("probables-sources.txt");

		for (String file : filesToRead) {

			try {

				List<String> content = Files.readAllLines(Paths.get("./framework-complete/" + file));

				String source = file.substring(0, file.indexOf("-"));

				for (String line : content) {

					String[] array = line.split(";");

					String feature = array[0].replaceAll("'", "’");

					if (!list.contains(feature)) {

						list.add(feature + ";" + source);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static void appendCuesHeader(StringBuffer sbArff, List<String> cues) {

		for (String cue : cues) {

			String feature = cue.split(";")[0];

			String source = cue.split(";")[1];

			// not needed anymore as there is no single quotes in the features
			// String tmp = Utilities.replaceSingleQuotes(feature);

			sbArff.append("@attribute '" + source + "_" + feature + "' numeric\n");
		}
	}

	public static String countCues(String comment, List<String> cues) {

		String str = "";

		int hedges = 0;
		int hypotheticals = 0;
		int I_statements = 0;
		int meta = 0;
		int nonverbals = 0;
		int probables = 0;

		for (String cue : cues) {

			String feature = cue.split(";")[0];

			String source = cue.split(";")[1];

			int total = Utilities.countWordFrequency(feature, comment);

			str = str + total + ",";

			if (source.equalsIgnoreCase("hedges")) {

				hedges = hedges + total;

			} else if (source.equalsIgnoreCase("hypotheticals")) {

				hypotheticals = hypotheticals + total;

			} else if (source.equalsIgnoreCase("I-statements")) {

				I_statements = I_statements + total;

			} else if (source.equalsIgnoreCase("meta")) {

				meta = meta + total;

			} else if (source.equalsIgnoreCase("nonverbals")) {

				nonverbals = nonverbals + total;

			} else if (source.equalsIgnoreCase("probables")) {

				probables = probables + total;
			}
		}

		str = str + hedges + "," + hypotheticals + "," + I_statements + "," + meta + "," + nonverbals + "," + probables;

		return str;
	}

	public static int countWordFrequency(String feature, String comment) {
		int matches = 0;
		Matcher matcher = Pattern.compile("\\b" + feature + "\\b", Pattern.CASE_INSENSITIVE).matcher(comment);
		while (matcher.find()) {
			matches++;
		}
		return matches;
	}

	public static String replacePatchSet(String str) {
		StringBuilder newLine = new StringBuilder(str);
		Matcher matcher = Pattern.compile("(?i)(patch set)").matcher(str);
		while (matcher.find()) {
			newLine.replace(matcher.start(), matcher.end(), "PATCHSET");
		}
		return newLine.toString();
	}

	public static String replaceBreakLines(String str) {
		String replaced = str;
		if (replaced.contains("\n") || str.contains("\r")) {
			replaced = str.replaceAll("\\r\\n|\\r|\\n", " ");
		}
		return replaced;
	}

	public static String removeQuotedTexts(String text) {
		String lines[] = text.split("\\r?\\n");
		String newMessage = "";
		for (String line : lines) {
			if (!line.matches("^(\\p{Blank})*[>].*")) {
				newMessage = newMessage.concat(line + "\n");
			}
		}
		return newMessage;
	}

	public static String removeXMLTags(String text) {
		return text.replaceAll("<[^>]+>", "");
	}

	public static String removeChars(String text) {
		text = text.replaceAll("=", "");
		text = text.replaceAll(":", "");
		text = text.replaceAll("-", "");
		text = text.replaceAll("&", "");
		text = text.replaceAll("#", "");
		text = text.replaceAll("\\*", "");
		text = text.replaceAll("\\$", "");
		text = text.replaceAll("\\+", "");
		text = text.replaceAll("\\[", "");
		text = text.replaceAll("\\]", "");
		text = text.replaceAll("<", "");
		text = text.replaceAll(">", "");
		text = text.replaceAll("\\^", "");
		text = text.replaceAll("~", "");
		text = text.replaceAll("\\|", "");
		text = text.replaceAll("\\{", "");
		text = text.replaceAll("\\}", "");
		text = text.replaceAll("\\\\", "");
		return text;
	}

	public static String replaceSingleQuotes(String str) {
		String replaced = str;
		if (str.contains("'")) {
			replaced = str.replaceAll("'", "\\\\\\'");
		}
		return replaced;
	}

	public static String replaceDoubleQuotes(String str) {
		String replaced = str;
		if (replaced.contains("\"")) {
			replaced = str.replaceAll("\"", "\\\\\"");
		}
		return replaced;
	}

	public static String removeQuotes(String str) {
		str = str.replaceAll("'", " ");
		str = str.replaceAll("’", " ");
		str = str.replaceAll("`", " ");
		str = str.replaceAll("\"", "");
		return str;
	}

	public static String removeGreaterThan(String str) {
		String replaced = str;
		if (str.contains(">")) {
			replaced = str.replaceAll(">", "");
		}
		return replaced;
	}

	public static String replaceCommitSha1(String str) {
		return str.replaceAll("\\b[0-9a-f]{5,40}\\b", "COMMIT");
	}

	public static String replaceNumbers(String str) {
		return str.replaceAll("\\b\\d+\\b", "NUMBER");
	}

	public static String replaceNames(String str, HashSet<String> nameList) {
		String[] words = str.split("[\\s.,;:\n!?()]+");
		for (int i = 0; i < words.length; i++) {
			for (String name : nameList) {
				String possessive = name + "’s";
				if (name.equalsIgnoreCase(words[i]) || possessive.equalsIgnoreCase(words[i])) {
					// System.out.println(str);
					// System.out.println("name: " + words[i]);
					// System.out.println("|| array[0].equalsIgnoreCase(\"" + name + "\")");
					str = str.replaceAll("\\b" + words[i] + "\\b", "@USERNAME");
					words[i] = "@USERNAME";
					// System.out.println(str);
					// System.out.println("========================");
				}
			}
		}
		return str;
	}

	public static HashSet<String> readNameListFiles() {
		String path = "./namesList/";
		HashSet<String> set = new HashSet<String>();
		try {
			List<File> filesInFolder = new ArrayList<>();
			filesInFolder.addAll(Files.walk(Paths.get(path)).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toList()));
			for (File file : filesInFolder) {
				List<String> list = Files.readAllLines(file.toPath());
				for (String line : list) {
					String[] array = line.split(";");

					if (array[0].equalsIgnoreCase("Set") || array[0].equalsIgnoreCase("And")
							|| array[0].equalsIgnoreCase("In") || array[0].equalsIgnoreCase("How")
							|| array[0].equalsIgnoreCase("To") || array[0].equalsIgnoreCase("Be")
							|| array[0].equalsIgnoreCase("A") || array[0].equalsIgnoreCase("That")
							|| array[0].equalsIgnoreCase("Over") || array[0].equalsIgnoreCase("By")
							|| array[0].equalsIgnoreCase("Any") || array[0].equalsIgnoreCase("The")
							|| array[0].equalsIgnoreCase("On") || array[0].equalsIgnoreCase("Side")
							|| array[0].equalsIgnoreCase("Even") || array[0].equalsIgnoreCase("Maybe")
							|| array[0].equalsIgnoreCase("For") || array[0].equalsIgnoreCase("Best")
							|| array[0].equalsIgnoreCase("Do") || array[0].equalsIgnoreCase("You")
							|| array[0].equalsIgnoreCase("Non") || array[0].equalsIgnoreCase("An")
							|| array[0].equalsIgnoreCase("Log") || array[0].equalsIgnoreCase("I")
							|| array[0].equalsIgnoreCase("Master") || array[0].equalsIgnoreCase("Or")
							|| array[0].equalsIgnoreCase("numbers") || array[0].equalsIgnoreCase("Very")
							|| array[0].equalsIgnoreCase("Run") || array[0].equalsIgnoreCase("Java")
							|| array[0].equalsIgnoreCase("Lot") || array[0].equalsIgnoreCase("Me")
							|| array[0].equalsIgnoreCase("So") || array[0].equalsIgnoreCase("One")
							|| array[0].equalsIgnoreCase("Due") || array[0].equalsIgnoreCase("Have")
							|| array[0].equalsIgnoreCase("Ah") || array[0].equalsIgnoreCase("Alias")
							|| array[0].equalsIgnoreCase("Api") || array[0].equalsIgnoreCase("Are")
							|| array[0].equalsIgnoreCase("Ask") || array[0].equalsIgnoreCase("Big")
							|| array[0].equalsIgnoreCase("C") || array[0].equalsIgnoreCase("cherry")
							|| array[0].equalsIgnoreCase("Constant") || array[0].equalsIgnoreCase("D")
							|| array[0].equalsIgnoreCase("Dear") || array[0].equalsIgnoreCase("desire")
							|| array[0].equalsIgnoreCase("Done") || array[0].equalsIgnoreCase("Else")
							|| array[0].equalsIgnoreCase("File") || array[0].equalsIgnoreCase("Final")
							|| array[0].equalsIgnoreCase("Fine") || array[0].equalsIgnoreCase("G")
							|| array[0].equalsIgnoreCase("Gerrit") || array[0].equalsIgnoreCase("Git")
							|| array[0].equalsIgnoreCase("Given") || array[0].equalsIgnoreCase("Go")
							|| array[0].equalsIgnoreCase("Great") || array[0].equalsIgnoreCase("Happy")
							|| array[0].equalsIgnoreCase("Has") || array[0].equalsIgnoreCase("He")
							|| array[0].equalsIgnoreCase("J") || array[0].equalsIgnoreCase("Jar")
							|| array[0].equalsIgnoreCase("Jenkins") || array[0].equalsIgnoreCase("Kind")
							|| array[0].equalsIgnoreCase("L") || array[0].equalsIgnoreCase("Light")
							|| array[0].equalsIgnoreCase("Like") || array[0].equalsIgnoreCase("Line")
							|| array[0].equalsIgnoreCase("Little") || array[0].equalsIgnoreCase("Long")
							|| array[0].equalsIgnoreCase("Mailing") || array[0].equalsIgnoreCase("Make")
							|| array[0].equalsIgnoreCase("May") || array[0].equalsIgnoreCase("Mean")
							|| array[0].equalsIgnoreCase("Media") || array[0].equalsIgnoreCase("More")
							|| array[0].equalsIgnoreCase("My") || array[0].equalsIgnoreCase("N")
							|| array[0].equalsIgnoreCase("Name") || array[0].equalsIgnoreCase("Native")
							|| array[0].equalsIgnoreCase("Nice") || array[0].equalsIgnoreCase("Ok")
							|| array[0].equalsIgnoreCase("Os") || array[0].equalsIgnoreCase("Phone")
							|| array[0].equalsIgnoreCase("Pick") || array[0].equalsIgnoreCase("ping")
							|| array[0].equalsIgnoreCase("Pretty") || array[0].equalsIgnoreCase("Ready")
							|| array[0].equalsIgnoreCase("Real") || array[0].equalsIgnoreCase("Said")
							|| array[0].equalsIgnoreCase("Say") || array[0].equalsIgnoreCase("Silly")
							|| array[0].equalsIgnoreCase("Skip") || array[0].equalsIgnoreCase("Sorry")
							|| array[0].equalsIgnoreCase("Special") || array[0].equalsIgnoreCase("Sure")
							|| array[0].equalsIgnoreCase("Tar") || array[0].equalsIgnoreCase("Than")
							|| array[0].equalsIgnoreCase("Them") || array[0].equalsIgnoreCase("Tie")
							|| array[0].equalsIgnoreCase("Time") || array[0].equalsIgnoreCase("Too")
							|| array[0].equalsIgnoreCase("Tricky") || array[0].equalsIgnoreCase("Try")
							|| array[0].equalsIgnoreCase("Us") || array[0].equalsIgnoreCase("Valid")
							|| array[0].equalsIgnoreCase("With") || array[0].equalsIgnoreCase("Zero")
							|| array[0].equalsIgnoreCase("Key") || array[0].equalsIgnoreCase("Ran")
							|| array[0].equalsIgnoreCase("Im") || array[0].equalsIgnoreCase("Okay")
							|| array[0].equalsIgnoreCase("Guy") || array[0].equalsIgnoreCase("Beta")
							|| array[0].equalsIgnoreCase("grant") || array[0].equalsIgnoreCase("Made")
							|| array[0].equalsIgnoreCase("Mark") || array[0].equalsIgnoreCase("Job")
							|| array[0].equalsIgnoreCase("Core") || array[0].equalsIgnoreCase("bar")
							|| array[0].equalsIgnoreCase("bill") || array[0].equalsIgnoreCase("Let")
							|| array[0].equalsIgnoreCase("Late") || array[0].equalsIgnoreCase("Less")
							|| array[0].equalsIgnoreCase("Dev") || array[0].equalsIgnoreCase("Sets")
							|| array[0].equalsIgnoreCase("Ids") || array[0].equalsIgnoreCase("Trees")
							|| array[0].equalsIgnoreCase("Per") || array[0].equalsIgnoreCase("Rom")
							|| array[0].equalsIgnoreCase("Hammer") || array[0].equalsIgnoreCase("MD")
							|| array[0].equalsIgnoreCase("Bat") || array[0].equalsIgnoreCase("Read")
							|| array[0].equalsIgnoreCase("Nit") || array[0].equalsIgnoreCase("Dash")
							|| array[0].equalsIgnoreCase("Odd") || array[0].equalsIgnoreCase("Val")
							|| array[0].equalsIgnoreCase("Handy") || array[0].equalsIgnoreCase("Header")
							|| array[0].equalsIgnoreCase("Lambda") || array[0].equalsIgnoreCase("Odd")
							|| array[0].equalsIgnoreCase("R") || array[0].equalsIgnoreCase("Live")
							|| array[0].equalsIgnoreCase("Soon") || array[0].equalsIgnoreCase("Setter")
							|| array[0].equalsIgnoreCase("Io") || array[0].equalsIgnoreCase("Echo")
							|| array[0].equalsIgnoreCase("Kinda") || array[0].equalsIgnoreCase("Dex")
							|| array[0].equalsIgnoreCase("Maxx") || array[0].equalsIgnoreCase("Matrix")
							|| array[0].equalsIgnoreCase("Success") || array[0].equalsIgnoreCase("max")
							|| array[0].equalsIgnoreCase("Hate") || array[0].equalsIgnoreCase("mar")
							|| array[0].equalsIgnoreCase("Cas") || array[0].equalsIgnoreCase("jill")
							|| array[0].equalsIgnoreCase("Free") || array[0].equalsIgnoreCase("Math")
							|| array[0].equalsIgnoreCase("Ever") || array[0].equalsIgnoreCase("Foo")
							|| array[0].equalsIgnoreCase("Eos") || array[0].equalsIgnoreCase("Denies")
							|| array[0].equalsIgnoreCase("S") || array[0].equalsIgnoreCase("Min")
							|| array[0].equalsIgnoreCase("len") || array[0].equalsIgnoreCase("Tor")
							|| array[0].equalsIgnoreCase("ram") || array[0].equalsIgnoreCase("Hal")
							|| array[0].equalsIgnoreCase("Binder") || array[0].equalsIgnoreCase("Deb")
							|| array[0].equalsIgnoreCase("Mako") || array[0].equalsIgnoreCase("Gen")
							|| array[0].equalsIgnoreCase("Dest") || array[0].equalsIgnoreCase("Re")
							|| array[0].equalsIgnoreCase("Phi") || array[0].equalsIgnoreCase("Abi")
							|| array[0].equalsIgnoreCase("Art") || array[0].equalsIgnoreCase("Te")
							|| array[0].equalsIgnoreCase("Ast") || array[0].equalsIgnoreCase("Neon")
							|| array[0].equalsIgnoreCase("Ota") || array[0].equalsIgnoreCase("Se")
							|| array[0].equalsIgnoreCase("Vold")) {

						continue;

					} else {
						set.add(array[0]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static String removeCorruptedChars(String str) {
		str = str.replaceAll("[^\\x00-\\x7F]", "");
		return str;
	}

	public static String replaceURLs(String text) {
		String str = text;
		String urlRegex = "((http(s)?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			String url = text.substring(urlMatcher.start(0), urlMatcher.end(0));
			str = str.replace(url, "@URL");
		}
		return str;
	}

	public static int countQuestionMark(String comment) {
		comment = Utilities.replaceURLs(comment);
		return StringUtils.countMatches(comment, "?");
	}

	public static int countWords(String comment, List<String> list) {
		int total = 0;
		for (String item : list) {
			total = total + Utilities.countWordFrequency(item, comment);
		}
		return total;
	}

	public static void saveFile(String sb, String file) {

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {

			writer.write(sb);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static String handleStopWordsAndNegation(String text) {

		List<String> tokensAll = Twokenize.tokenizeRawTweetText(text);

		/**
		 * Step 1: remove the stopwords
		 */
//		RainbowEdited rainbow = new RainbowEdited();
//
//		List<String> tokens = removeStopWords(rainbow, tokensAll);
//
//		tokens = removeMiscTokens(tokens);
		
		List<String> tokensFinal = new ArrayList<String>();

		/**
		 * Step 2: handle negation
		 */
		for (int i = 0; i < tokensAll.size(); i++) {

			Matcher mNegation = Negation.matcher(tokensAll.get(i));

			if (mNegation.find()) {

				for (int j = i + 1; j < tokensAll.size(); j++) {

					Matcher mPonctuation = Ponctuation.matcher(tokensAll.get(j));

					if (mPonctuation.find()) {
						
						tokensFinal.add(tokensAll.get(j));

						i = j;

						break;

					} else {

						Matcher mPonctuation_ignore = Ponctuation_ignore.matcher(tokensAll.get(j));

						if (!mPonctuation_ignore.find()) {
							
							Matcher mNegativeStopWords = Negative_Stopwords.matcher(tokensAll.get(j));
							
							if (!mNegativeStopWords.find()) {
								
								tokensFinal.add(tokensAll.get(j) + neg);
							}
						}
					}
				}
				
			} else {
				
				tokensFinal.add(tokensAll.get(i));
			}
		}
		
		return buildStringFromTokens(tokensFinal);
	}

	private static String buildStringFromTokens(List<String> tokens) {

		StringBuilder newText = new StringBuilder();

		tokens.add(" ");

		for (int i = 0; i < tokens.size() - 1; i++) {

			Matcher mPonctuation = Ponctuation_all.matcher(tokens.get(i + 1));

			if (mPonctuation.find()) {

				newText.append(tokens.get(i));

			} else {

				newText.append(tokens.get(i) + " ");
			}
		}

		return newText.toString().trim();
	}

	public static String handleNegation_old(String text) {

		List<String> tokens = Twokenize.tokenizeRawTweetText(text);

		for (int i = 0; i < tokens.size(); i++) {

			Matcher mNegation = Negation.matcher(tokens.get(i));

			if (mNegation.find()) {

				for (int j = i + 1; j < tokens.size(); j++) {

					Matcher mPonctuation = Ponctuation.matcher(tokens.get(j));

					if (mPonctuation.find()) {

						i = j;

						break;

					} else {

						Matcher mPonctuation_ignore = Ponctuation_ignore.matcher(tokens.get(j));

						if (!mPonctuation_ignore.find()) {

							tokens.set(j, tokens.get(j) + neg);
						}
					}
				}
			}
		}

		return buildStringFromTokens(tokens);
	}

	public static String handleWhats(String line) {

		StringBuilder newLine = new StringBuilder(line);

		Matcher m = Contractions_whats.matcher(newLine);

		while (m.find()) {

			newLine.replace(m.start(), m.end(), "what is");
		}

		return newLine.toString();
	}

	public static String handleContractionsPerfectContinous(String line) {

		StringBuilder newLine = new StringBuilder(line);

		Matcher m = Contractions_perfect.matcher(newLine);

		while (m.find()) {

			String contration = m.group(3).trim();

			if (contration.equalsIgnoreCase("s")) {

				newLine.replace(m.start(3) - 1, m.end(3), " has");

			} else if (contration.equalsIgnoreCase("d")) {

				newLine.replace(m.start(3) - 1, m.end(3), " had");

			} else if (contration.equalsIgnoreCase("ve")) {

				newLine.replace(m.start(3) - 1, m.end(3), " have");

			}
		}

		return newLine.toString();
	}

	public static String handleContractions(String line) {

		StringBuilder newLine = new StringBuilder(line);

		Matcher m = Contractions.matcher(newLine);

		while (m.find()) {

			String left = m.group(1).trim();

			String right = m.group(2).trim();

			// System.out.println(left + " + " + right);

			if (right.equalsIgnoreCase("n't")) {

				String tmp = "";

				if (left.equalsIgnoreCase("wo")) {

					tmp = "will";

				} else if (left.equalsIgnoreCase("ca")) {

					tmp = "can";

				} else {

					tmp = left;
				}

				newLine.replace(m.start(), m.end(), tmp + " not");

			} else if (right.equalsIgnoreCase("'ve")) {

				newLine.replace(m.start(), m.end(), left + " have");

			} else if (right.equalsIgnoreCase("'m")) {

				newLine.replace(m.start(), m.end(), left + " am");

			} else if (right.equalsIgnoreCase("'ll")) {

				newLine.replace(m.start(), m.end(), left + " will");

			} else if (right.equalsIgnoreCase("'d")) {

				newLine.replace(m.start(), m.end(), left + " would");

			} else if (right.equalsIgnoreCase("'re")) {

				newLine.replace(m.start(), m.end(), left + " are");

			} else if (right.equalsIgnoreCase("'s")) {

				newLine.replace(m.start(), m.end(), left + " is");

			} else if (right.equalsIgnoreCase("don't") || right.equalsIgnoreCase("dont")) {

				newLine.replace(m.start(), m.end(), left + " do not");

			} else if (right.equalsIgnoreCase("doesn't") || right.equalsIgnoreCase("doesnt")) {

				newLine.replace(m.start(), m.end(), left + " does not");

			} else if (right.equalsIgnoreCase("didn't") || right.equalsIgnoreCase("didnt")) {

				newLine.replace(m.start(), m.end(), left + " did not");

			} else if (right.equalsIgnoreCase("aren't") || right.equalsIgnoreCase("arent")) {

				newLine.replace(m.start(), m.end(), left + " are not");

			} else if (right.equalsIgnoreCase("wasn't") || right.equalsIgnoreCase("wasnt")) {

				newLine.replace(m.start(), m.end(), left + " was not");

			} else if (right.equalsIgnoreCase("weren't") || right.equalsIgnoreCase("werent")) {

				newLine.replace(m.start(), m.end(), left + " were not");

			} else if (right.equalsIgnoreCase("hasn't") || right.equalsIgnoreCase("hasnt")) {

				newLine.replace(m.start(), m.end(), left + " has not");

			} else if (right.equalsIgnoreCase("haven't") || right.equalsIgnoreCase("havent")) {

				newLine.replace(m.start(), m.end(), left + " have not");

			} else if (right.equalsIgnoreCase("hadn't") || right.equalsIgnoreCase("hadnt")) {

				newLine.replace(m.start(), m.end(), left + " had not");

			} else if (right.equalsIgnoreCase("isn't") || right.equalsIgnoreCase("isnt")) {

				newLine.replace(m.start(), m.end(), left + " is not");

			} else if (right.equalsIgnoreCase("can't")) {

				newLine.replace(m.start(), m.end(), left + " cannot");

			} else if (right.equalsIgnoreCase("won't") || right.equalsIgnoreCase("wont")) {

				newLine.replace(m.start(), m.end(), left + " will not");

			} else if (right.equalsIgnoreCase("wouldn't") || right.equalsIgnoreCase("wouldnt")) {

				newLine.replace(m.start(), m.end(), left + " would not");

			} else if (right.equalsIgnoreCase("shouldn't") || right.equalsIgnoreCase("shouldnt")) {

				newLine.replace(m.start(), m.end(), left + " should not");

			}
		}

		return newLine.toString();
	}

	public static String handleNegativeQuestions(String line) {

		line = replaceMiscellaneous(line);

		Properties props = new Properties();

		props.put("annotators", "tokenize, ssplit, parse");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		lp.setOptionFlags(
				new String[] { "-outputFormat", "penn,typedDependenciesCollapsed", "-retainTmpSubcategories" });

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();

		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		StringBuilder newLine = new StringBuilder();

		Annotation document = new Annotation(line);

		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			StringBuilder newSentence = new StringBuilder(sentence.toString());

			List<String> tokens = Twokenize.tokenizeRawTweetText(newSentence.toString());

			for (int i = 0; i < tokens.size(); i++) {

				Matcher m = Contractions_neg_questions_strict.matcher(tokens.get(i));

				if (m.find()) {

					String question = m.group().trim();

					Tree parse = (Tree) lp.parse(sentence.toString());

					Tree tree = sentence.get(TreeAnnotation.class);

					boolean isQuestion = false;

					for (Tree leave : tree) {

						if (leave.label().toString().equalsIgnoreCase("SBARQ")
								|| leave.label().toString().equalsIgnoreCase("SQ")) {

							isQuestion = true;

							break;
						}
					}

					if (isQuestion) {

						GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);

						Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

						// =============== NEW ===========================

						TypedDependency before = null;

						TypedDependency negationType = null;

						TypedDependency after = null;

						String afterString = null;

						Object[] arrayType = tdl.toArray();

						for (int j = 0; j < arrayType.length; j++) {

							TypedDependency tp = (TypedDependency) arrayType[j];

							GrammaticalRelation node = tp.reln();

							if (node.getShortName().equalsIgnoreCase(("neg"))
									&& tp.dep().word().equalsIgnoreCase("n't")) {

								before = (TypedDependency) arrayType[j - 1];

								negationType = tp;

								TypedDependency tmp = (TypedDependency) arrayType[j + 1];

								if (tmp.reln().getShortName().equalsIgnoreCase(("nsubj"))
										|| tmp.reln().getShortName().equalsIgnoreCase(("nsubjpass"))) {

									after = tmp;

								} else {

									boolean check = false;

									boolean check2 = false;

									for (Tree leaf : tree) {

										if (check2) {

											afterString = leaf.nodeString();

											break;
										}

										if (leaf.nodeString().equalsIgnoreCase("n't")) {

											check = true;
										}

										if (check) {

											// NN - Noun, singular or mass
											// NNS - Noun, plural
											// NNP - Proper noun, singular
											// NNPS - Proper noun, plural
											// PRP - Personal pronoun
											// PRP$ - Possessive pronoun (prolog version PRP-S)

											if (leaf.nodeString().contains("NN") || leaf.nodeString().contains("PRP")) {

												check2 = true;
											}
										}
									}
								}

								break;
							}
						}

						// ===============================================

						// =============== OLD ===========================

//						TypedDependency auxType = null;
//
//						TypedDependency negType = null;
//
//						TypedDependency nsubjType = null;
//
//						String verbWord = null;
//
//						for (TypedDependency td : tdl) {
//
//							GrammaticalRelation node = td.reln();
//
//							Matcher mAuxiliar = Auxiliars.matcher(td.dep().toString());
//
//							if (mAuxiliar.find() && (node.getShortName().equalsIgnoreCase(("aux"))
//									|| node.getShortName().equalsIgnoreCase(("root")))) {
//
//								auxType = td;
//
//								verbWord = td.gov().toString();
//							}
//
//							if (node.getShortName().equalsIgnoreCase(("neg"))
//									&& td.gov().toString().equalsIgnoreCase(verbWord) && auxType != null) {
//
//								negType = td;
//							}
//
//							if (node.getShortName().equalsIgnoreCase(("nsubj"))
//									&& td.gov().toString().equalsIgnoreCase(verbWord) && auxType != null
//									&& negType != null) {
//
//								nsubjType = td;
//
//								break;
//							}
//						}

						// ==========================================

						int afterStringPos = 0;

						boolean exceptionalCase = false;

						if (afterString == null && after != null) {

							IndexedWord specific = after.dep();

							afterStringPos = specific.endPosition();

						} else if (afterString != null) {

							afterStringPos = sentence.toString().indexOf(afterString) + afterString.length();

						} else if (afterString == null && after == null) {

							exceptionalCase = true;
						}

						if (!exceptionalCase) {

							String subject = sentence.toString().substring(negationType.dep().endPosition(),
									afterStringPos);

							if (question.equalsIgnoreCase("don't") || question.equalsIgnoreCase("dont")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"do" + subject + " not");

							} else if (question.equalsIgnoreCase("doesn't") || question.equalsIgnoreCase("doesnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"does" + subject + " not");

							} else if (question.equalsIgnoreCase("didn't") || question.equalsIgnoreCase("didnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"did" + subject + " not");

							} else if (question.equalsIgnoreCase("aren't") || question.equalsIgnoreCase("arent")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"are" + subject + " not");

							} else if (question.equalsIgnoreCase("wasn't") || question.equalsIgnoreCase("wasnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"was" + subject + " not");

							} else if (question.equalsIgnoreCase("weren't") || question.equalsIgnoreCase("werent")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"were" + subject + " not");

							} else if (question.equalsIgnoreCase("hasn't") || question.equalsIgnoreCase("hasnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"has" + subject + " not");

							} else if (question.equalsIgnoreCase("haven't") || question.equalsIgnoreCase("havent")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"have" + subject + " not");

							} else if (question.equalsIgnoreCase("hadn't") || question.equalsIgnoreCase("hadnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"had" + subject + " not");

							} else if (question.equalsIgnoreCase("isn't") || question.equalsIgnoreCase("isnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"is" + subject + " not");

							} else if (question.equalsIgnoreCase("can't")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos, "cannot" + subject);

							} else if (question.equalsIgnoreCase("won't") || question.equalsIgnoreCase("wont")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"will" + subject + " not");

							} else if (question.equalsIgnoreCase("wouldn't") || question.equalsIgnoreCase("wouldnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"would" + subject + " not");

							} else if (question.equalsIgnoreCase("shouldn't")
									|| question.equalsIgnoreCase("shouldnt")) {

								newSentence.replace(before.dep().beginPosition(), afterStringPos,
										"should" + subject + " not");
							}

						} else {

							newSentence.replace(0, newSentence.length(),
									handleRemainingContractions(newSentence.toString()));
						}

					}
				}
			}

			newLine.append(newSentence + " ");
		}

		return newLine.toString().trim();
	}

	public static String replaceMiscellaneous(String line) {

		List<String> tokens = Twokenize.tokenizeRawTweetText(line);

		for (int i = 0; i < tokens.size(); i++) {

			Matcher m1 = Pattern.compile("(?i)^(dont)$").matcher(tokens.get(i));

			if (m1.find()) {
				tokens.set(i, "don't");
			}

			Matcher m3 = Pattern.compile("(?i)^(doesnt)$").matcher(tokens.get(i));

			if (m3.find()) {
				tokens.set(i, "doesn't");
			}

			Matcher m4 = Pattern.compile("(?i)^(didnt)$").matcher(tokens.get(i));

			if (m4.find()) {
				tokens.set(i, "didn't");
			}

			Matcher m5 = Pattern.compile("(?i)^(arent)$").matcher(tokens.get(i));

			if (m5.find()) {
				tokens.set(i, "aren't");
			}

			Matcher m6 = Pattern.compile("(?i)^(wasnt)$").matcher(tokens.get(i));

			if (m6.find()) {
				tokens.set(i, "wasn't");
			}

			Matcher m7 = Pattern.compile("(?i)^(werent)$").matcher(tokens.get(i));

			if (m7.find()) {
				tokens.set(i, "weren't");
			}

			Matcher m8 = Pattern.compile("(?i)^(hasnt)$").matcher(tokens.get(i));

			if (m8.find()) {
				tokens.set(i, "hasn't");
			}

			Matcher m9 = Pattern.compile("(?i)^(havent)$").matcher(tokens.get(i));

			if (m9.find()) {
				tokens.set(i, "haven't");
			}

			Matcher m14 = Pattern.compile("(?i)^(hadnt)$").matcher(tokens.get(i));

			if (m14.find()) {
				tokens.set(i, "hadn't");
			}

			Matcher m10 = Pattern.compile("(?i)^(isnt)$").matcher(tokens.get(i));

			if (m10.find()) {
				tokens.set(i, "isn't");
			}

			Matcher m11 = Pattern.compile("(?i)^(wont)$").matcher(tokens.get(i));

			if (m11.find()) {
				tokens.set(i, "won't");
			}

			Matcher m12 = Pattern.compile("(?i)^(wouldnt)$").matcher(tokens.get(i));

			if (m12.find()) {
				tokens.set(i, "wouldn't");
			}

			Matcher m13 = Pattern.compile("(?i)^(shouldnt)$").matcher(tokens.get(i));

			if (m13.find()) {
				tokens.set(i, "shouldn't");
			}
		}

		return buildStringFromTokens(tokens);
	}

	public static String handleRemainingContractions(String line) {

		List<String> tokens = Twokenize.tokenizeRawTweetText(line);

		for (int i = 0; i < tokens.size(); i++) {

			Matcher m1 = Pattern.compile("(?i)^(don't)$").matcher(tokens.get(i));

			if (m1.find()) {
				tokens.set(i, "do not");
			}

			Matcher m2 = Pattern.compile("(?i)^(can't)$").matcher(tokens.get(i));

			if (m2.find()) {
				tokens.set(i, "cannot");
			}

			Matcher m15 = Pattern.compile("(?i)^(cant)$").matcher(tokens.get(i));

			if (m15.find()) {
				tokens.set(i, "cannot");
			}

			Matcher m3 = Pattern.compile("(?i)^(doesn't)$").matcher(tokens.get(i));

			if (m3.find()) {
				tokens.set(i, "does not");
			}

			Matcher m4 = Pattern.compile("(?i)^(didn't)$").matcher(tokens.get(i));

			if (m4.find()) {
				tokens.set(i, "did not");
			}

			Matcher m5 = Pattern.compile("(?i)^(aren't)$").matcher(tokens.get(i));

			if (m5.find()) {
				tokens.set(i, "are not");
			}

			Matcher m6 = Pattern.compile("(?i)^(wasn't)$").matcher(tokens.get(i));

			if (m6.find()) {
				tokens.set(i, "was not");
			}

			Matcher m7 = Pattern.compile("(?i)^(weren't)$").matcher(tokens.get(i));

			if (m7.find()) {
				tokens.set(i, "were not");
			}

			Matcher m8 = Pattern.compile("(?i)^(hasn't)$").matcher(tokens.get(i));

			if (m8.find()) {
				tokens.set(i, "has not");
			}

			Matcher m9 = Pattern.compile("(?i)^(haven't)$").matcher(tokens.get(i));

			if (m9.find()) {
				tokens.set(i, "have not");
			}

			Matcher m14 = Pattern.compile("(?i)^(hadn't)$").matcher(tokens.get(i));

			if (m14.find()) {
				tokens.set(i, "had not");
			}

			Matcher m10 = Pattern.compile("(?i)^(isn't)$").matcher(tokens.get(i));

			if (m10.find()) {
				tokens.set(i, "is not");
			}

			Matcher m11 = Pattern.compile("(?i)^(won't)$").matcher(tokens.get(i));

			if (m11.find()) {
				tokens.set(i, "will not");
			}

			Matcher m12 = Pattern.compile("(?i)^(wouldn't)$").matcher(tokens.get(i));

			if (m12.find()) {
				tokens.set(i, "would not");
			}

			Matcher m13 = Pattern.compile("(?i)^(shouldn't)$").matcher(tokens.get(i));

			if (m13.find()) {
				tokens.set(i, "should not");
			}
		}

		return buildStringFromTokens(tokens);
	}
	
	public static String handleRemainingStopWords(String line) {

		List<String> tokens = Twokenize.tokenizeRawTweetText(line);
		
		List<String> newTokens = new ArrayList<String>();

		for (int i = 0; i < tokens.size(); i++) {

			Matcher m = StopWordsRemaining.matcher(tokens.get(i));

			if (!m.find()) {
				
				newTokens.add(tokens.get(i));
			}
			
		}

		return buildStringFromTokens(newTokens);
	}

	public static List<String> removeStopWords(RainbowEdited rainbow, List<String> tokens) {

		List<String> newTokens = new ArrayList<String>();

		for (String word : tokens) {

			if (!rainbow.isStopWord(word)) {

				newTokens.add(word);
			}
		}

		return newTokens;
	}

	public static List<String> removeMiscTokens(List<String> tokens) {

		List<String> newTokens = new ArrayList<String>();

		for (String word : tokens) {

			Matcher m = Pattern.compile("(?i)^(@|/|//)$").matcher(word);

			if (!m.find()) {

				newTokens.add(word);
			}
		}

		return newTokens;
	}

}
