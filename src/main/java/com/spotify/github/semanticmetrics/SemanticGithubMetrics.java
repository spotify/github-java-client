package com.spotify.github.semanticmetrics;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class SemanticGithubMetrics implements Metrics {

    private final MetricId baseMetric;

    private final SemanticMetricRegistry registry;

    public SemanticGithubMetrics(final SemanticMetricRegistry registry, final MetricId baseMetricId) {
        this.registry = registry;
        this.baseMetric =
                baseMetricId.tagged(
                        "what", "github-results",
                        "component", "github-client");

    }

    @Override
    public void recordMetric(
            final String path,
            final String method,
            final CompletableFuture<Response> future) {
        requireNonNull(path);
        requireNonNull(future);

        MetricId tagged = baseMetric.tagged().tagged("http-url", path).tagged("method", method);
        Timer.Context githubLatency = registry.timer(tagged).time();

        future.whenComplete(
                (result, t) -> {
                    githubLatency.stop();
                    if (t == null) {
                        registry.meter(tagged.tagged("result", "success").tagged("status-code", extractCode(result))).mark();

                    } else {
                        registry.meter(tagged.tagged("result", "failure").tagged("status-code", extractCode(result))).mark();
                    }
                });
    }

    private String extractCode(Response result) {
        if(result!= null) {
            return ""+result.code();
        }
        return "-1";
    }
}



