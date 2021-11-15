package com.spotify.github.semanticmetrics;

import okhttp3.Response;

import java.util.concurrent.CompletableFuture;

public class NoopMetrics implements Metrics{


    public static NoopMetrics INSTANCE = new NoopMetrics();

    @Override
    public void recordMetric(String toString, String method, CompletableFuture<Response> future) {

    }
}
