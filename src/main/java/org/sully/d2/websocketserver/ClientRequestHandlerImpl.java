package org.sully.d2.websocketserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.itemtracking.*;
import org.sully.d2.websocketserver.requests.*;
import org.sully.d2.websocketserver.responses.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ClientRequestHandlerImpl  /* implements ClientRequest.Handler */ {
/*
    private final ItemUseCaseTracker itemUseCaseTracker;
    private final BasicStatsConsumer basicStatsConsumer;
    private final Map<String,ItemGrid<?,?>> itemGrids;
    private final ObjectMapper jackson;

    public ClientRequestHandlerImpl(ItemUseCaseTracker itemUseCaseTracker, BasicStatsConsumer basicStatsConsumer,
                                    Map<String, ItemGrid<?,?>> itemGrids, ObjectMapper jackson) {
        this.itemUseCaseTracker = itemUseCaseTracker;
        this.basicStatsConsumer = basicStatsConsumer;
        this.itemGrids = itemGrids;
        this.jackson = jackson;
    }


    @Override
    public void handle(TopNItemsRequest request, Consumer<String> responseSender) {
        List<ItemAndScore<D2Item>> topNItems = itemUseCaseTracker.getTopNItems(request.getUseCase(), request.getCategory());
        List<ItemAndScore<D2Item>> filteredItems = new ArrayList<>();
        ItemAndScore<D2Item> item;

        int itemCountToReturn = Math.min(topNItems.size(), request.getN());

        for (int i = 0; i < itemCountToReturn; i++) {
            item = topNItems.get(i);
            if ((item.getSequenceNumber() > request.getMaxSequenceNumberAlreadyFetched()) ||
                    (i+1) > request.getMaxRankAlreadyFetched()) {
                filteredItems.add(item);
            }
        }

        TopNItemsResponse response = TopNItemsResponse.builder()
                .n(itemCountToReturn)
                .diffToFillTopN(filteredItems)
                .requestId(request.getRequestId())
                .build();

        try {
            String responseString = jackson.writeValueAsString(response);
            System.out.println("Sending Response : " + responseString);
            responseSender.accept(responseString);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void handle(UseCaseAndCategoryNamesRequest request, Consumer<String> responseSender) {
        Map<String, List<String>> useCasesAndCategories = itemUseCaseTracker.getUseCasesAndCategories();

        UseCaseAndCategoryNamesResponse response = UseCaseAndCategoryNamesResponse.builder()
                .categoryNamesByUseCase(useCasesAndCategories)
                .requestId(request.getRequestId())
                .build();

        try {
            String responseString = jackson.writeValueAsString(response);
            System.out.println("Sending Response : " + responseString);
            responseSender.accept(responseString);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void handle(HomePageRequest request, Consumer<String> responseSender) {
        BasicStats stats = basicStatsConsumer.getStats();
        HomePageResponse response = HomePageResponse.builder()
                .itemCountsByQuality(stats.getCountsByQuality())
                .recentItemsByQuality(stats.getMostRecentItemsByQuality())
                .requestId(request.getRequestId())
                .build();

        try {
            String responseString = jackson.writeValueAsString(response);
            System.out.println("Sending Response : " + responseString);
            responseSender.accept(responseString);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void handle(ScoreDistributionRequest request, Consumer<String> responseSender) {

        ConcurrentHashMap<Integer,Long> x = itemUseCaseTracker.getScoreDistribution(request.getUseCase(), request.getCategory());
        List<Integer> scores = new ArrayList<>();
        List<Long> frequencies = new ArrayList<>();
        x.forEach((score, frequency) -> {
            scores.add(score);
            frequencies.add(frequency);
        });

        ScoreDistributionResponse response = ScoreDistributionResponse.builder()
                .scores(scores)
                .counts(frequencies)
                .requestId(request.getRequestId())
                .build();

        try {
            String responseString = jackson.writeValueAsString(response);
            System.out.println("Sending Response : " + responseString);
            responseSender.accept(responseString);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void handle(ItemGridCountsRequest request, Consumer<String> responseSender) {

        ItemGridCountsResponse response = ItemGridCountsResponse.builder()
                .requestId(request.getRequestId())
                .counts(itemGrids.get(request.getGridName()).getCounts())
                .build();

        try {
            String responseString = jackson.writeValueAsString(response);
            System.out.println("Sending Response : " + responseString);
            responseSender.accept(responseString);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    */
}
