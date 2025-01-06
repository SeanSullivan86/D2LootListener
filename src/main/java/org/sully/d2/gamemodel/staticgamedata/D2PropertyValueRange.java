package org.sully.d2.gamemodel.staticgamedata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;
import org.sully.d2.gamemodel.StatIdAndParam;

@Value
@Builder
@AllArgsConstructor
public class D2PropertyValueRange {
    String propertyCode;

    String param;

    @NonFinal
    @PackagePrivate
    int min;

    @NonFinal
    @PackagePrivate
    int max;

    @NonFinal
    @PackagePrivate
    StatIdAndParam statImpactedByRoll;
    @NonFinal
    @PackagePrivate
    D2Property property;

    public D2PropertyValueRange(String propertyCode, String param, int min, int max) {
        this.propertyCode = propertyCode;
        this.param = param;
        this.min = min;
        this.max = max;
    }

}
