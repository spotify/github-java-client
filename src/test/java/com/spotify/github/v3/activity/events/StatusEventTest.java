package com.spotify.github.v3.activity.events;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.Test;

public class StatusEventTest {
  @Test
  public void testDeserialization() throws IOException {
    String fixture =
        Resources.toString(
            getResource(this.getClass(), "fixtures/status_event.json"), defaultCharset());
    final StatusEvent statusEvent = Json.create().fromJson(fixture, StatusEvent.class);
    assertThat(statusEvent.context(), is("default"));
    assertThat(statusEvent.sha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
    assertThat(statusEvent.state(), is("success"));
  }
}