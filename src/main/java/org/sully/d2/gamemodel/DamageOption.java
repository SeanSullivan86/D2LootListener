package org.sully.d2.gamemodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DamageOption {
	String itemType;
	List<String> sockets; // "empty" , "15_40", "ohm", "zod"
	int min;
	int max;
	int dps;

	@JsonIgnore
	public int getAverage() {
		return (min + max)/2;
	}
}
