package org.sully.d2.websocketserver.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.itemtracking.ItemAndScore;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TopNItemsResponse.class, name = "TopNItemsResponse"),
        @JsonSubTypes.Type(value = LiveItemStatsResponse.class, name = "LiveItemStatsResponse"),
        @JsonSubTypes.Type(value = UseCaseAndCategoryNamesResponse.class, name = "UseCaseAndCategoryNamesResponse"),
        @JsonSubTypes.Type(value = HomePageResponse.class, name = "HomePageResponse"),
        @JsonSubTypes.Type(value = ScoreDistributionResponse.class, name = "ScoreDistributionResponse"),
        @JsonSubTypes.Type(value = ItemGridCountsResponse.class, name = "ItemGridCountsResponse")
})
public interface ServerResponse {

}

