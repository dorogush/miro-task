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

import static com.adorogush.mirotask.exception.ConflictException.checkOverflow;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

/** Immutable data class to hold complete Widget. */
public class Widget {

  private final String id;
  private final int x;
  private final int y;
  private final int z;
  private final int width;
  private final int height;
  private final Instant lastModified;

  public Widget(
      @JsonProperty("id") final String id,
      @JsonProperty("x") final Integer x,
      @JsonProperty("y") final Integer y,
      @JsonProperty("z") final Integer z,
      @JsonProperty("width") final Integer width,
      @JsonProperty("height") final Integer height,
      @JsonProperty("lastModified") final Instant lastModified) {
    this.id = requireNonNull(id, "Field id cannot be empty.");
    this.x = requireNonNull(x, "Field x cannot be empty.");
    this.y = requireNonNull(y, "Field y cannot be empty.");
    this.z = requireNonNull(z, "Field z cannot be empty.");
    this.width = requireNonNull(width, "Field width cannot be empty.");
    this.height = requireNonNull(height, "Field height cannot be empty.");
    this.lastModified = requireNonNull(lastModified, "Field lastModified cannot be empty.");
  }

  @JsonProperty("id")
  public String id() {
    return id;
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
  public int z() {
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

  @JsonProperty("lastModified")
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  public Instant lastModified() {
    return lastModified;
  }

  public Widget incZ() {
    return new Widget(id, x, y, checkOverflow(z) + 1, width, height, lastModified);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Widget widget = (Widget) o;
    return x == widget.x
        && y == widget.y
        && z == widget.z
        && width == widget.width
        && height == widget.height
        && id.equals(widget.id)
        && lastModified.equals(widget.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, x, y, z, width, height, lastModified);
  }

  @Override
  public String toString() {
    return "Widget{"
        + "id='"
        + id
        + '\''
        + ", x="
        + x
        + ", y="
        + y
        + ", z="
        + z
        + ", width="
        + width
        + ", height="
        + height
        + ", lastModified="
        + lastModified
        + '}';
  }
}
