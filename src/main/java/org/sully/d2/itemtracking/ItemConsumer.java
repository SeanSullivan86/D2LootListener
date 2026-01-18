package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;

import java.util.function.Consumer;

public interface ItemConsumer {
	void consume(D2Item item, ItemNotifier notifier);
	void closeAndGenerateOutput();
}

