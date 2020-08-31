/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.adorogush.mirotask.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Immutable data class to hold the set of fields representing the update Widget request. */
public class WidgetToUpdate {

  private final Integer x;
  private final Integer y;
  private final Integer z;
  private final Integer width;
  private final Integer height;

  public WidgetToUpdate(
      @JsonProperty("x") final Integer x,
      @JsonProperty("y") final Integer y,
      @JsonProperty("z") final Integer z,
      @JsonProperty("width") final Integer width,
      @JsonProperty("height") final Integer height) {
    if (x == null && y == null && z == null && width == null && height == null) {
      throw new IllegalArgumentException("Must provide at least one field for update.");
    }
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
  }

  @JsonProperty("x")
  public Integer x() {
    return x;
  }

  @JsonProperty("y")
  public Integer y() {
    return y;
  }

  @JsonProperty("z")
  public Integer z() {
    return z;
  }

  @JsonProperty("width")
  public Integer width() {
    return width;
  }

  @JsonProperty("height")
  public Integer height() {
    return height;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WidgetToUpdate that = (WidgetToUpdate) o;
    return Objects.equals(x, that.x)
        && Objects.equals(y, that.y)
        && Objects.equals(z, that.z)
        && Objects.equals(width, that.width)
        && Objects.equals(height, that.height);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z, width, height);
  }

  @Override
  public String toString() {
    return "WidgetToCreateOrUpdate{"
        + "x="
        + x
        + ", y="
        + y
        + ", z="
        + z
        + ", width="
        + width
        + ", height="
        + height
        + '}';
  }
}
