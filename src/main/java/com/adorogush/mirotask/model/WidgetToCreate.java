/*
* Copyright 2020 Aleksandr Dorogush
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.adorogush.mirotask.model;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Immutable data class to hold the set of fields representing the create Widget request. */
public class WidgetToCreate {

  private final int x;
  private final int y;
  private final Integer z;
  private final int width;
  private final int height;

  public WidgetToCreate(
      @JsonProperty("x") final Integer x,
      @JsonProperty("y") final Integer y,
      @JsonProperty("z") final Integer z,
      @JsonProperty("width") final Integer width,
      @JsonProperty("height") final Integer height) {
    this.x = requireNonNull(x, "Field x cannot be empty.");
    this.y = requireNonNull(y, "Field y cannot be empty.");
    this.z = z;
    this.width = requireNonNull(width, "Field width cannot be empty.");
    this.height = requireNonNull(height, "Field height cannot be empty.");
  }

  @JsonProperty("x")
  public int x() {
    return x;
  }

  @JsonProperty("y")
  public int y() {
    return y;
  }

  @JsonProperty("z")
  public Integer z() {
    return z;
  }

  @JsonProperty("width")
  public int width() {
    return width;
  }

  @JsonProperty("height")
  public int height() {
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
    WidgetToCreate that = (WidgetToCreate) o;
    return x == that.x
        && y == that.y
        && width == that.width
        && height == that.height
        && Objects.equals(z, that.z);
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
