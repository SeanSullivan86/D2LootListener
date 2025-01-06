package org.sully.d2.gamemodel.staticgamedata;

public enum D2ItemTypes {
	SMALL_CHARM("cm1"),
	LARGE_CHARM("cm2"),
	GRAND_CHARM("cm3"),
	JEWEL("jew");

	public final String code;

	D2ItemTypes(String code) {
		this.code = code;
	}

	public D2ItemType get() {
		return D2ItemType.fromCode(code);
	}
}
