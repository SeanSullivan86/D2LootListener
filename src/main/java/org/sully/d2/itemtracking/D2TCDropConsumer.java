package org.sully.d2.itemtracking;

import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;

import java.util.Map;

public interface D2TCDropConsumer {

	String getName();

	long getTotalIterations();

	void initializeFromSnapshot(TCDropConsumerSnapshot snapshot, Map<Long, SerializableD2Item> itemsById);

	void consume(D2TCDrop tcDrop);

	DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot();

}

