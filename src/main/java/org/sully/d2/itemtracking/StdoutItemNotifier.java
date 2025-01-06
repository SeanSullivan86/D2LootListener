package org.sully.d2.itemtracking;

import java.util.function.Consumer;

public class StdoutItemNotifier implements ItemNotifier {

    @Override
    public void notify(ItemNotification itemNotification) {
        System.out.println(itemNotification.getNotificationType());
        System.out.println(itemNotification.getSubtypes());
        System.out.println("ItemDrop Iteration : " + itemNotification.getItem().getDropIteration());
        System.out.println(itemNotification.getItem().getItem().toLongString());
    }
}
