package org.sully.d2.websocketserver.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.websocket.Session;
import org.sully.d2.itemtracking.ItemGridCounts;

import java.util.function.Consumer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TopNItemsRequest.class, name = "TopNItemsRequest"),
        @JsonSubTypes.Type(value = UseCaseAndCategoryNamesRequest.class, name = "UseCaseAndCategoryNamesRequest"),
        @JsonSubTypes.Type(value = HomePageRequest.class, name = "HomePageRequest"),
        @JsonSubTypes.Type(value = ScoreDistributionRequest.class, name = "ScoreDistributionRequest"),
        @JsonSubTypes.Type(value = ItemGridCountsRequest.class, name = "ItemGridCountsRequest")
})
public interface ClientRequest {
    void acceptHandler(ClientRequest.Handler handler, Consumer<String> responseSender);


    static interface Handler {
        void handle(TopNItemsRequest request, Consumer<String> responseSender);
        void handle(UseCaseAndCategoryNamesRequest request, Consumer<String> responseSender);
        void handle(HomePageRequest request, Consumer<String> responseSender);
        void handle(ScoreDistributionRequest request, Consumer<String> responseSender);
        void handle(ItemGridCountsRequest request, Consumer<String> responseSender);
    }
}

