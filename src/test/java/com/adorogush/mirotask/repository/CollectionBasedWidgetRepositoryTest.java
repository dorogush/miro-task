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
package com.adorogush.mirotask.repository;

import static com.adorogush.mirotask.WidgetUtil.randomWidget;
import static com.adorogush.mirotask.WidgetUtil.randomWidgetToCreate;
import static com.adorogush.mirotask.WidgetUtil.widgetToUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.service.IdProvider;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test covering {@link CollectionBasedWidgetRepository}. */
class CollectionBasedWidgetRepositoryTest {

  private final IdProvider idProvider = mock(IdProvider.class);
  private final Clock clock = mock(Clock.class);
  private final Map<String, Widget> idToWidget = new ConcurrentHashMap<>();
  private final SortedMap<Integer, Widget> zToWidget = new ConcurrentSkipListMap<>();
  private final WidgetRepository repository =
      new CollectionBasedWidgetRepository(idProvider, clock, idToWidget, zToWidget);

  @BeforeEach
  void beforeEach() {
    idToWidget.clear();
    zToWidget.clear();
    reset(idProvider, clock);
  }

  @Test
  void testCreateWithEmptyZWorks() {
    // given
    putWidget(randomWidget("1", 1));
    when(idProvider.get()).thenReturn("2");
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Widget created = repository.createOne(randomWidgetToCreate(null));
    // then
    assertSize(2);
    assertWidgetInMaps("2", 2);
    assertThat(created.id(), equalTo("2"));
    assertThat(created.z(), equalTo(2));
    assertThat(created.lastModified(), equalTo(instant1));
  }

  @Test
  void testCreateWithAvailableZWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("3", 3));
    when(idProvider.get()).thenReturn("2");
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Widget created = repository.createOne(randomWidgetToCreate(2));
    // then
    assertSize(3);
    assertWidgetInMaps("1", 1);
    assertWidgetInMaps("2", 2);
    assertWidgetInMaps("3", 3);
    assertThat(created.id(), equalTo("2"));
    assertThat(created.z(), equalTo(2));
    assertThat(created.lastModified(), equalTo(instant1));
  }

  @Test
  void testCreateWithUnavailableZShiftWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("4", 4));
    when(idProvider.get()).thenReturn("3");
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Widget created = repository.createOne(randomWidgetToCreate(2));
    // then
    assertSize(4);
    assertWidgetInMaps("1", 1);
    assertWidgetInMaps("2", 3);
    assertWidgetInMaps("3", 2);
    assertWidgetInMaps("4", 5);
    assertThat(created.id(), equalTo("3"));
    assertThat(created.z(), equalTo(2));
    assertThat(created.lastModified(), equalTo(instant1));
  }

  @Test
  void testReadOneFoundWorks() {
    // given
    putWidget(randomWidget("1", 1));
    // when
    final Optional<Widget> widgetFound = repository.readOne("1");
    // then
    assertThat(widgetFound.isPresent(), equalTo(true));
    assertThat(widgetFound.get().id(), equalTo("1"));
    assertThat(widgetFound.get().z(), equalTo(1));
  }

  @Test
  void testReadOneNotFoundWorks() {
    // when
    final Optional<Widget> widgetFound = repository.readOne("1");
    // then
    assertThat(widgetFound.isEmpty(), equalTo(true));
  }

  @Test
  void testReadAllEmptyFromZWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("3", 3));
    // when
    final List<Widget> widgetsFound = repository.readAll(2, null);
    // then
    assertThat(widgetsFound.size(), equalTo(2));
    assertThat(widgetsFound.get(0).id(), equalTo("1"));
    assertThat(widgetsFound.get(0).z(), equalTo(1));
    assertThat(widgetsFound.get(1).id(), equalTo("2"));
    assertThat(widgetsFound.get(1).z(), equalTo(2));
  }

  @Test
  void testReadAllFromZWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("3", 3));
    putWidget(randomWidget("4", 4));
    // when
    final List<Widget> widgetsFound = repository.readAll(2, 2);
    // then
    assertThat(widgetsFound.size(), equalTo(2));
    assertThat(widgetsFound.get(0).id(), equalTo("2"));
    assertThat(widgetsFound.get(0).z(), equalTo(2));
    assertThat(widgetsFound.get(1).id(), equalTo("3"));
    assertThat(widgetsFound.get(1).z(), equalTo(3));
  }

  @Test
  void testReadAllEmptyFromZNotFoundWorks() {
    // when
    final List<Widget> widgetsFound = repository.readAll(2, null);
    // then
    assertThat(widgetsFound.isEmpty(), equalTo(true));
  }

  @Test
  void testReadAllFromZNotFoundWorks() {
    // given
    putWidget(randomWidget("1", 1));
    // when
    final List<Widget> widgetsFound = repository.readAll(2, 2);
    // then
    assertThat(widgetsFound.isEmpty(), equalTo(true));
  }

  @Test
  void testUpdateXWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("3", 3));
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Optional<Widget> widgetUpdated = repository.updateOne("2", widgetToUpdate(10, null));
    // then
    assertSize(3);
    assertWidgetInMaps("1", 1);
    assertWidgetInMaps("2", 2);
    assertWidgetInMaps("3", 3);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().x(), equalTo(10));
    assertThat(widgetUpdated.get().z(), equalTo(2));
    assertThat(widgetUpdated.get().lastModified(), equalTo(instant1));
  }

  @Test
  void testUpdateZWithAvailableZWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("4", 4));
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Optional<Widget> widgetUpdated = repository.updateOne("2", widgetToUpdate(null, 3));
    // then
    assertSize(3);
    assertWidgetInMaps("1", 1);
    assertWidgetInMaps("2", 3);
    assertWidgetInMaps("4", 4);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().z(), equalTo(3));
    assertThat(widgetUpdated.get().lastModified(), equalTo(instant1));
  }

  @Test
  void testUpdateZWithUnavailableZShiftWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    putWidget(randomWidget("3", 3));
    putWidget(randomWidget("5", 5));
    final Instant instant1 = Instant.now();
    when(clock.instant()).thenReturn(instant1);
    // when
    final Optional<Widget> widgetUpdated = repository.updateOne("2", widgetToUpdate(null, 3));
    // then
    assertSize(4);
    assertWidgetInMaps("1", 1);
    assertWidgetInMaps("2", 3);
    assertWidgetInMaps("3", 4);
    assertWidgetInMaps("5", 6);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().z(), equalTo(3));
    assertThat(widgetUpdated.get().lastModified(), equalTo(instant1));
  }

  @Test
  void testUpdateNotFoundWorks() {
    // when
    final Optional<Widget> widgetUpdated = repository.updateOne("1", widgetToUpdate(10, null));
    // then
    assertThat(widgetUpdated.isEmpty(), equalTo(true));
  }

  @Test
  void testDeleteWorks() {
    // given
    putWidget(randomWidget("1", 1));
    putWidget(randomWidget("2", 2));
    // when
    final Optional<Widget> widgetDeleted = repository.deleteOne("1");
    // then
    assertSize(1);
    assertWidgetInMaps("2", 2);
    assertThat(widgetDeleted.isPresent(), equalTo(true));
    assertThat(widgetDeleted.get().id(), equalTo("1"));
    assertThat(widgetDeleted.get().z(), equalTo(1));
  }

  @Test
  void testDeleteNotFoundWorks() {
    // when
    final Optional<Widget> widgetDeleted = repository.deleteOne("1");
    // then
    assertThat(widgetDeleted.isEmpty(), equalTo(true));
  }

  private void putWidget(final Widget widget) {
    idToWidget.put(widget.id(), widget);
    zToWidget.put(widget.z(), widget);
  }

  private void assertSize(final int expectedSize) {
    assertThat(idToWidget.size(), equalTo(expectedSize));
    assertThat(zToWidget.size(), equalTo(expectedSize));
  }

  private void assertWidgetInMaps(final String id, final int z) {
    final Widget widgetFoundById = idToWidget.get(id);
    assertThat(widgetFoundById, notNullValue());
    assertThat(widgetFoundById.id(), equalTo(id));
    assertThat(widgetFoundById.z(), equalTo(z));
    final Widget widgetFoundByZ = zToWidget.get(z);
    assertThat(widgetFoundByZ, notNullValue());
    assertThat(widgetFoundByZ.id(), equalTo(id));
    assertThat(widgetFoundByZ.z(), equalTo(z));
  }
}
