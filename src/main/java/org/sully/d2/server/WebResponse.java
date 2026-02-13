package org.sully.d2.server;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WebResponse {
    String snapshotId;
    Object response;
}
