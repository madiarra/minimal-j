package ch.openech.mj.autofill;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// http://www.heise.de/ct/ftp/07/17/182/
public class FirstNameGenerator {

	private static List<NameWithFrequency> males = new ArrayList<NameWithFrequency>(2000);
	private static List<NameWithFrequency> femals = new ArrayList<NameWithFrequency>(2000);
	
	public static String getFirstName(boolean male) {
		return getName(male).name;
	}
	
	public static NameWithFrequency getName() {
		return getName(Math.random() < .5);
	}
	
	public static NameWithFrequency getName(boolean male) {
		if (males.isEmpty()) readNames();
		return choose(male ? males : femals);
	}
	
	private static NameWithFrequency choose(List<NameWithFrequency> list) {
		Collections.shuffle(list);
		for (NameWithFrequency name : list) {
			if (Math.random() > (0.95 / (double)name.frequency)) return name;
		}
		return null;
 	}
	
	private static void readNames() {
		try {
			InputStream inputStream = FirstNameGenerator.class.getClassLoader().getResourceAsStream("ch/openech/resources/vornamen.txt");
			readNames(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void readNames(InputStream fis) throws Exception {
		Scanner scanner = new Scanner(fis, "ISO-8859-1");
		scanner.useDelimiter("\n");
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.charAt(0) == '#') continue;
			char frequency = line.charAt(44); // 44 -> Schweiz
			if (Character.isWhitespace(frequency)) continue;
			String type = line.substring(0, 3);
			NameWithFrequency nameWithFrequency = new NameWithFrequency();
			String name = line.substring(3, 29).trim();
			int pos = name.indexOf(' ');
			if (pos < 0) {
				nameWithFrequency.name = name;
			} else {
				nameWithFrequency.name = name.substring(pos + 1);
				nameWithFrequency.callName = name.substring(0, pos);
			}
			if (Character.isDigit(frequency)) nameWithFrequency.frequency = frequency - '0';
			else nameWithFrequency.frequency = frequency - 'A' + 10;
			if (!type.contains("M")) femals.add(nameWithFrequency);
			if (!type.contains("F")) males.add(nameWithFrequency);
		}
	}
	
}
