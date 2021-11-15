package com.spotify.github.semanticmetrics;

import com.codahale.metrics.Metric;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SemanticGithubMetricsTest {

    private SemanticMetricRegistry registry = new SemanticMetricRegistry();
    Request request = new Request.Builder().url("http://someUrl").build();
    Response.Builder response = new Response.Builder().message("response").code(200).protocol(Protocol.HTTP_1_1).request(request);

    @Test
    public void succeed() {
        SemanticGithubMetrics semanticGithubMetrics = new SemanticGithubMetrics(registry, MetricId.build("base", "mybase"));

        CompletableFuture<Response> responseSettableFuture = CompletableFuture.completedFuture(response.build());
        semanticGithubMetrics.recordMetric("/some/path", "PUT", responseSettableFuture);
        Map<MetricId, Metric> metrics = registry.getMetrics();
        assertThat(metrics.size(), is(1));
        MetricId key = metrics.keySet().iterator().next();
        assertThat(key.getTags(), is(Map.of(
                "component", "github-client",
                "http-url", "/some/path",
                "method", "PUT",
                "result", "success",
                "what", "github-results",
                "status-code", "200")));
    }


    @Test
    public void fail() {
        SemanticGithubMetrics semanticGithubMetrics = new SemanticGithubMetrics(registry, MetricId.build("base", "mybase"));
        CompletableFuture<Response> responseSettableFuture = CompletableFuture.failedFuture(new Throwable("Failed"));
        semanticGithubMetrics.recordMetric("/some/path", "PUT", responseSettableFuture);
        Map<MetricId, Metric> metrics = registry.getMetrics();
        assertThat(metrics.size(), is(1));
        MetricId key = metrics.keySet().iterator().next();
        assertThat(key.getTags(), is(Map.of(
                "component", "github-client",
                "http-url", "/some/path",
                "method", "PUT",
                "result", "failure",
                "what", "github-results",
                "status-code", "-1")));
    }


}