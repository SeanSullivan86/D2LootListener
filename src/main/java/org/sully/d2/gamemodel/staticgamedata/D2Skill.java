package org.sully.d2.gamemodel.staticgamedata;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // only use 'id' for equality check
public class D2Skill {
	private static final String COL_ID = "Id";
	private static final String COL_CHARACTER_CLASS = "charclass";
	private static final String COL_SKILL_DESCRIPTION_CODE = "skilldesc";
	private static final String COL_ITYPEA1 = "itypea1";
	private static final String COL_REQUIRED_LEVEL = "reqlevel";
	private static final String COL_SKILL_CODE = "skill";
	
	private static Map<Integer, D2Skill> skillsById;
	private static Map<String, D2Skill> skillsByCode;
	private static Map<String, D2Skill> skillsByDescCode;
	
	String skillCode;
	@EqualsAndHashCode.Include int id;
	CharacterClass characterClass;
	String skillNameStringCode;
	String skillDescCode;

	int skillTabIdWithinClass;
	int requiredLevel;
	SkillTab skillTab;
	
	String allowedItemTypeTypeCodeForStaffmods;

	@NonFinal
	D2ItemTypeType allowedItemTypeTypeForStaffmods;
	@NonFinal
	String name;
	
	public static D2Skill fromId(int id) {
		return skillsById.get(id);
	}
	
	public static D2Skill fromCode(String skillCode) {
		return skillsByCode.get(skillCode);
	}

	public static D2Skill fromAmbiguousSkillIdentifier(String val) {
		try {
			int x = Integer.parseInt(val);
			return skillsById.get(x);
		} catch (Exception e) {
			if (skillsByCode.containsKey(val)) {
				return skillsByCode.get(val);
			} else if (skillsByDescCode.containsKey(val)) {
				return skillsByDescCode.get(val);
			} else {
				throw new RuntimeException("Unexpected skill : " + val);
			}
		}
	}

	public static void loadData() {
		skillsById = new HashMap<>();
		skillsByCode = new HashMap<>();
		skillsByDescCode = new HashMap<>();
		
		D2SkillDesc.loadData();
		
    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/Skills.txt",
    			row -> (! row.get(COL_CHARACTER_CLASS).isEmpty()));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
    		D2Skill skill = fromDataRow(dataRow);
    		skillsById.put(skill.id, skill);
    		skillsByCode.put(skill.skillCode, skill);
    		skillsByDescCode.put(skill.skillDescCode, skill);
    	}
	}

	public static void linkData() {
		for (int id : skillsById.keySet()) {
			D2Skill skill = skillsById.get(id);
			if (skill.allowedItemTypeTypeCodeForStaffmods != null) {
				skill.allowedItemTypeTypeForStaffmods = D2ItemTypeType.fromCode(skill.allowedItemTypeTypeCodeForStaffmods);
			}
			skill.name = D2String.fromKey(skill.skillNameStringCode);
		}
	}
	
	static D2Skill fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
		D2SkillBuilder skill = D2Skill.builder();
		skill.skillCode = dataRow.get(COL_SKILL_CODE);
		skill.id = dataRow.getInt(COL_ID);
		skill.characterClass = CharacterClass.fromCode(dataRow.get(COL_CHARACTER_CLASS));
		
		skill.skillDescCode = dataRow.get(COL_SKILL_DESCRIPTION_CODE);
		D2SkillDesc skillDesc = D2SkillDesc.skillDescByCode.get(skill.skillDescCode);
		skill.skillNameStringCode = skillDesc.skillNameStringCode;
		skill.skillTabIdWithinClass = skillDesc.skillTab;
		skill.skillTab = SkillTab.fromClassAndIdWithinClass(skill.characterClass, skill.skillTabIdWithinClass);
		
		skill.allowedItemTypeTypeCodeForStaffmods = dataRow.getNullable(COL_ITYPEA1);
		skill.requiredLevel = dataRow.getInt(COL_REQUIRED_LEVEL);
		return skill.build();
	}
	
	public SkillTab getSkillTab() {
		return this.skillTab;
	}
	
	public CharacterClass getCharacterClass() {
		return this.characterClass;
	}
	
	public D2ItemTypeType getAllowedItemTypeTypeForStaffmod() {
		return this.allowedItemTypeTypeForStaffmods;
	}

	private static class D2SkillDesc {
		private static final String COL_SKILLDESC = "skilldesc";
		private static final String COL_SKILL_NAME_STRING_CODE = "str name";
		private static final String COL_SKILL_TAB = "SkillPage";
		
		private String skillDescCode;
		private String skillNameStringCode;
		private int skillTab;
		
		static Map<String, D2SkillDesc> skillDescByCode;
		
		static void loadData() {
			skillDescByCode = new HashMap<>();
			
	    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
	    			"game_data/SkillDesc.txt",
	    			row -> (! row.get(COL_SKILLDESC).isEmpty()));
	    	
	    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
	    		D2SkillDesc skillDesc = fromDataRow(dataRow);
	    		skillDescByCode.put(skillDesc.skillDescCode, skillDesc);
	    	}
		}
		
		static D2SkillDesc fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
			D2SkillDesc skillDesc = new D2SkillDesc();
			skillDesc.skillDescCode = dataRow.get(COL_SKILLDESC);
			skillDesc.skillNameStringCode = dataRow.get(COL_SKILL_NAME_STRING_CODE);
			skillDesc.skillTab = dataRow.getInt(COL_SKILL_TAB);
			return skillDesc;
		}
		
	}
	
}