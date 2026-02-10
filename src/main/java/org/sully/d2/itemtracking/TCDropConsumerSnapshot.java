package org.sully.d2.itemtracking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.sully.d2.server.ConsumerSummary;

import java.util.Set;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="__type")
public interface TCDropConsumerSnapshot {

	String getName();
	long getTotalIterations();

	@JsonIgnore
	Set<Long> getReferencedItemIds();

	@JsonIgnore
	ConsumerSummary toSummaryObject();
}
