package org.sully.d2.server;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsumerSummary {
    String consumerName;
    String consumerType;
    Object additionalInfo; // varies depending on the consumerType
}
