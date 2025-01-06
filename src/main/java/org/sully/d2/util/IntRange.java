package org.sully.d2.util;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IntRange {
	int min;
	int max;

	public static IntRange withMinAndMax(int min, int max) {
		return new IntRange(min, max);
	}
}