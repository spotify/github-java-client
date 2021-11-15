package com.spotify.github.semanticmetrics;

import okhttp3.Call;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;

public interface Metrics {

    void recordMetric(String toString, String method, CompletableFuture<Response> future);
}
