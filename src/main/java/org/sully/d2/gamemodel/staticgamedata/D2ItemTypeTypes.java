package org.sully.d2.gamemodel.staticgamedata;

public enum D2ItemTypeTypes {
	HELM_INCLUDING_CIRCLETS("helm"),
	GLOVES("glov"),
	BELT("belt"),
	BODY_ARMOR_TORSO("tors"),
	BOOTS("boot"),
	SHIELD_INCLUDING_PALADIN_AND_NECRO("shld"),
	
	CIRCLET("circ"),
	
	BARB_HELM("phlm"),
	SORC_ORB("orb");

	public final String code;

	D2ItemTypeTypes(String code) {
		this.code = code;
	}

	public D2ItemTypeType get() {
		return D2ItemTypeType.fromCode(code);
	}
}
