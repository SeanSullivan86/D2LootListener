package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.List;

@Value
@Builder
public class ItemNotification {

    D2Item item;
    String notificationType;
    List<String> subtypes;
    Integer score;
}
