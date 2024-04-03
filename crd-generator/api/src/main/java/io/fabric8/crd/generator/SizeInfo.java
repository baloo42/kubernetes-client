/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.crd.generator;

import io.sundr.model.AnnotationRef;

import java.util.Objects;
import java.util.Optional;

public class SizeInfo {
  private final Long min;
  private final Long max;

  private SizeInfo(Long min, Long max) {
    this.min = min;
    this.max = max;
  }

  public Optional<Long> getMin() {
    return Optional.ofNullable(min);
  }

  public Optional<Long> getMax() {
    return Optional.ofNullable(max);
  }

  public static SizeInfo from(AnnotationRef annotationRef) {
    final Long min = (Long) annotationRef.getParameters().get("min");
    final Long max = (Long) annotationRef.getParameters().get("max");

    assertGreaterOrEqual(min, 0L, "min must be greater than 0");
    assertGreaterOrEqual(max, 0L, "max must be greater than 0");
    assertGreaterOrEqual(max, min, String.format("max (%s) must be greater or equal to min (%s)", max, min));

    return new SizeInfo(mapIfNotDefault(min, 0L), mapIfNotDefault(max, Long.MAX_VALUE));
  }

  private static void assertGreaterOrEqual(Long x, Long y, String message) {
    if (x >= y)
      return;
    throw new IllegalArgumentException(message);
  }

  private static Long mapIfNotDefault(Long value, Long defaultValue) {
    return Objects.equals(value, defaultValue) ? null : value;
  }

}
