package weka.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Init {

	public static List<String> initModelVerbs() {
		List<String> modalVerbs = new ArrayList<String>();
		modalVerbs.add("could"); // same from hedges
		modalVerbs.add("may"); // same from hedges
		modalVerbs.add("might"); // same from hedges
		modalVerbs.add("can");
		modalVerbs.add("must");
		modalVerbs.add("should");
		modalVerbs.add("will");
		modalVerbs.add("would");
		return modalVerbs;
	}

	public static List<String> initCategory(String category) {
		List<String> hedges = new ArrayList<String>();
		try {
			List<String> content = Files.readAllLines(Paths.get("./framework/" + category + "-sources.txt"));
			for (String line : content) {
				String[] array = line.split(";");
				String tmp = array[0].replaceAll("'", "’");
				hedges.add(tmp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hedges;
	}
}
