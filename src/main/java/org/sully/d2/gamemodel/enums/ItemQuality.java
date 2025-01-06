package org.sully.d2.gamemodel.enums;

import java.util.HashMap;
import java.util.Map;

public enum ItemQuality {
	INFERIOR(1),
	NORMAL(2),
	SUPERIOR(3),
	MAGIC(4),
	SET(5),
	RARE(6),
	UNIQUE(7),
	CRAFTED(8);
	
	public final int id;
	
	private ItemQuality(int id) {
		this.id = id;
	}
	
	private static Map<Integer,ItemQuality> qualityById;
	static {
		qualityById = new HashMap<>();
		for (ItemQuality q : values()) {
			qualityById.put(q.id, q);
		}
	}
	
	public static ItemQuality fromId(int id) {
		return qualityById.get(id);
	}
}
