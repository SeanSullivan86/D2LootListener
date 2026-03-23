package org.sully.d2;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class R2Config {
    String endpoint;
    String accessKey;
    String secretKey;
    String bucketName;
}
