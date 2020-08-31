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

import java.util.List;
import java.util.Objects;

/** Immutable data class to hold a page of found elements. */
public class Page<T> {

  private final List<T> elements;
  private final T next;

  private Page(final List<T> elements, final T next) {
    this.elements = elements;
    this.next = next;
  }

  public static <T> Page<T> of(final List<T> elements, final int perPage) {
    boolean hasNext = elements.size() > perPage;
    if (!hasNext) {
      return new Page<>(elements, null);
    }
    return new Page<>(elements.subList(0, perPage), elements.get(perPage));
  }

  public List<T> elements() {
    return elements;
  }

  public T next() {
    return next;
  }

  public boolean hasNext() {
    return next != null;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Page<?> page = (Page<?>) o;
    return Objects.equals(elements, page.elements) && Objects.equals(next, page.next);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elements, next);
  }

  @Override
  public String toString() {
    return "Page{" + "elements=" + elements + ", next=" + next + '}';
  }
}
