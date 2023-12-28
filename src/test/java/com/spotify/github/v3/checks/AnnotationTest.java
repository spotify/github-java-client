/*-
 * -\-\-
 * github-client
 * --
 * Copyright (C) 2016 - 2022 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.github.v3.checks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.ImmutableAnnotation.Builder;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationTest {
   private Builder builder() {
     return ImmutableAnnotation.builder()
         .title("title")
         .message("message")
         .rawDetails("rawDetails")
         .path("path")
         .startLine(1)
         .endLine(2)
         .startColumn(1)
         .endColumn(9)
         .annotationLevel(AnnotationLevel.notice);
   }

  @Test
  public void allowsCreationWithinLimits(){
    builder().build();

    builder()
        .title("a".repeat(255))
        .message("a".repeat(64000))
        .rawDetails("a".repeat(64000))
        .build();
  }

  @Test
  public void failsCreationWhenMaxLengthExceeded(){
    assertThrows(IllegalStateException.class, () ->
    builder().title("a".repeat(256)).build()
    );
    assertThrows(IllegalStateException.class, () ->
        builder().message("a".repeat(66000)).build()
    );
    assertThrows(IllegalStateException.class, () ->
        builder().rawDetails("a".repeat(66000)).build()
    );
  }

  @Test
  public void serializesWithEmptyFields() {
    Annotation annotationWithEmptyStringFields = ImmutableAnnotation.builder()
        .message("")
        .path("")
        .title("")
        .startLine(1)
        .endLine(2)
        .annotationLevel(AnnotationLevel.notice)
        .build();

    String serializedAnnotation = Json.create().toJsonUnchecked(annotationWithEmptyStringFields);
    String expected = "{\"path\":\"\",\"annotation_level\":\"notice\",\"message\":\"\",\"title\":\"\",\"start_line\":1,\"end_line\":2}";
    assertThat(serializedAnnotation, is(expected));
  }

  @Test
  public void clearsColumnFieldsForMultiLineAnnotation() {
    Annotation multiLineAnnotation = builder().startLine(1).endLine(2).build();
    Assertions.assertTrue(multiLineAnnotation.startColumn().isEmpty());
    Assertions.assertTrue(multiLineAnnotation.endColumn().isEmpty());

    Annotation anotherMultiLineAnnotation = builder().startLine(1).endLine(2).startColumn(Optional.empty()).endColumn(1).build();
    Assertions.assertTrue(anotherMultiLineAnnotation.startColumn().isEmpty());
    Assertions.assertTrue(anotherMultiLineAnnotation.endColumn().isEmpty());

    Annotation yetAnotherMultiLineAnnotation = builder().startLine(1).endLine(2).startColumn(1).endColumn(Optional.empty()).build();
    Assertions.assertTrue(yetAnotherMultiLineAnnotation.startColumn().isEmpty());
    Assertions.assertTrue(yetAnotherMultiLineAnnotation.endColumn().isEmpty());

    Annotation singleLineAnnotation = builder().startLine(1).endLine(1).build();
    assertEquals(1, singleLineAnnotation.startColumn().orElse(0));
    assertEquals(9, singleLineAnnotation.endColumn().orElse(0));
  }
}
