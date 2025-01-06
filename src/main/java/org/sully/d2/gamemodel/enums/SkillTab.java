package org.sully.d2.gamemodel.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum SkillTab {
	AMAZON_BOW_AND_CROSSBOW_SKILLS("ama", 1),
	AMAZON_PASSIVE_SKILLS("ama", 2),
	AMAZON_JAVELIN_AND_SPEAR_SKILLS("ama", 3),
	
	SORC_FIRE_SPELLS("sor", 1),
	SORC_LIGHTNING_SPELLS("sor", 2),
	SORC_COLD_SPELLS("sor", 3),
	
	NECRO_CURSES("nec", 1),
	NECRO_POISON_AND_BONE_SPELLS("nec", 2),
	NECRO_SUMMONING_SPELLS("nec", 3),
	
	PALADIN_COMBAT_SKILLS("pal", 1),
	PALADIN_OFFENSIVE_AURAS("pal", 2),
	PALADIN_DEFENSIVE_AURAS("pal", 3),
	
	BARB_COMBAT_SKILLS("bar", 1),
	BARB_COMBAT_MASTERIES("bar", 2),
	BARB_WARCRIES("bar", 3),
	
	DRUID_SUMMONING("dru", 1),
	DRUID_SHAPESHIFTING("dru", 2),
	DRUID_ELEMENTAL("dru", 3),
	
	ASSASSIN_TRAPS("ass", 1),
	ASSASSIN_SHADOW_DISCIPLINES("ass", 2),
	ASSASSIN_MARTIAL_ARTS("ass", 3);

	@Getter
	final CharacterClass characterClass;
	final int skillTabNumberWithinClass;
	final int itemStatParam;

	private SkillTab(String characterClassCode, int skillTabNumberWithinClass) {
		this.characterClass = CharacterClass.fromCode(characterClassCode);
		this.skillTabNumberWithinClass = skillTabNumberWithinClass;
		this.itemStatParam = this.characterClass.ordinal()*8 + (skillTabNumberWithinClass-1);
	}
	
	private static final Map<Integer,SkillTab> byItemStatParam = new HashMap<>();
	static {
		for (SkillTab tab : SkillTab.values()) {
			byItemStatParam.put(tab.itemStatParam, tab);
		}
	}
	
	public static SkillTab fromClassAndIdWithinClass(CharacterClass characterClass, int id) {
		return fromItemStatParam(characterClass.ordinal()*8 + (id-1));
	}
	
	public static SkillTab fromItemStatParam(int id) {
		return byItemStatParam.get(id);
	}
	
}
