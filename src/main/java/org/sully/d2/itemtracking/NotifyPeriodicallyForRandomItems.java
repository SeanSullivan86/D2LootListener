package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;

public class NotifyPeriodicallyForRandomItems implements ItemConsumer {

    long lastNotificationNanos = 0;
    long durationBetweenNotificationsInNanos = 2_000_000_000L; // 2 seconds

    @Override
    public void consume(D2ItemDrop itemDrop, ItemNotifier notifier) {

        D2Item item = itemDrop.getItem();
        if (item.getQuality() == ItemQuality.RARE) {
            long newTime = System.nanoTime();
            if (newTime > lastNotificationNanos + durationBetweenNotificationsInNanos) {
                notifier.notify(ItemNotification.builder()
                        .item(itemDrop)
                        .notificationType("RandomItem")
                        .build());
                lastNotificationNanos = newTime;
            }
        }


    }

    @Override
    public void closeAndGenerateOutput() {

    }
}
