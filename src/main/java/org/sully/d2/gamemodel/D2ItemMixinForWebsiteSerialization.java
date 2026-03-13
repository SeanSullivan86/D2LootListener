package org.sully.d2.gamemodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public abstract class D2ItemMixinForWebsiteSerialization {

	@JsonIgnore
	public abstract List<StatValue> getStats();
}
