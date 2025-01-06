package org.sully.d2.gamemodel.enums;

import java.util.HashMap;
import java.util.Map;

public enum CharacterClass {
	AMAZON("ama", 0),
	SORCERESS("sor", 1),
	NECROMANCER("nec", 2),
	PALADIN("pal", 3),
	BARBARIAN("bar", 4),
	DRUID("dru", 5),
	ASSASSIN("ass", 6);
	
	String code;
	int id;

	private CharacterClass(String code, int id) {
		this.code = code;
		this.id = id;
	}
	
	private static Map<String,CharacterClass> byCode;
	private static Map<Integer,CharacterClass> byId;
	static {
		byCode = new HashMap<>();
		byId = new HashMap<>();
		for (CharacterClass c : CharacterClass.values()) {
			byCode.put(c.code, c);
			byId.put(c.id, c);
		}
	}
	
	public static CharacterClass fromCode(String code) {
		if (!byCode.containsKey(code)) {
			throw new IllegalArgumentException("Bad character class code : " + code);
		}
		return byCode.get(code);
	}
	
	public static CharacterClass fromId(int id) {
		return byId.get(id);
	}
	
}