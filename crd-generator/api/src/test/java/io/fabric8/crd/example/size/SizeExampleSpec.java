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
package io.fabric8.crd.example.size;

import io.fabric8.generator.annotation.Size;

import java.util.List;
import java.util.Map;

public class SizeExampleSpec {
  @Size(min = 1, max = 3)
  private String stringWithLowerAndUpperLimits;

  @Size(min = 1, max = 3)
  private List<String> listWithLowerAndUpperLimits;

  @Size(min = 1, max = 3)
  private String[] arrayWithLowerAndUpperLimits;

  @Size(min = 1, max = 3)
  private Map<String, String> mapWithLowerAndUpperLimits;

  @Size(min = 1)
  private String stringWithLowerLimit;

  @Size(min = 1)
  private List<String> listWithLowerLimit;

  @Size(min = 1)
  private String[] arrayWithLowerLimit;

  @Size(min = 1)
  private Map<String, String> mapWithLowerLimit;

  @Size(max = 3)
  private String stringWithUpperLimit;

  @Size(max = 3)
  private List<String> listWithUpperLimit;

  @Size(max = 3)
  private String[] arrayWithUpperLimit;

  @Size(max = 3)
  private Map<String, String> mapWithUpperLimit;

  @Size(min = 1, max = 3) // ignored, because int is not supported (use @Min and/or @Max)
  private int integerWithIgnoredSizeLimits;

}
