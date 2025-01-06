package org.sully.d2.util;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Pair<A,B> {
	A a;
	B b;

	public static <A,B> Pair<A,B> of(A a, B b) {
		return new Pair<>(a,b);
	}
}