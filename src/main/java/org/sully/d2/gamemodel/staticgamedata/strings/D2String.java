package org.sully.d2.gamemodel.staticgamedata.strings;

import org.sully.d2.util.ResourceFileReader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class D2String {

	private static final Map<String, String> stringsByKey = new HashMap<>();
	
	public static String fromKey(String key) {
		return stringsByKey.get(key);
	}

	public static void loadData() {
		loadFile("game_data/strings/string.txt");
		loadFile("game_data/strings/expansionstring.txt");
		loadFile("game_data/strings/patchstring.txt");
	}
	
	private static void loadFile(String path) {
		String line;
		String[] parts;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceFileReader.getProjectResourceFileAsInputStream(path)))) {
			while ((line = in.readLine()) != null) {
				parts = line.split("\\t",-1);
				if (parts.length != 2) {
					throw new RuntimeException("Unexpected line in string file : " + line);
				}
				stringsByKey.put(parts[0], parts[1]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

