package org.sully.d2.server;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsumerSummary {
    String consumerId;
    String consumerType;
    Object additionalInfo; // varies depending on the consumerType
}

