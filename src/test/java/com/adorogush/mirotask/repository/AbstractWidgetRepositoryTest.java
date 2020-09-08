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
package com.adorogush.mirotask.repository;

import static com.adorogush.mirotask.WidgetUtil.randomWidgetToCreate;
import static com.adorogush.mirotask.WidgetUtil.widgetToUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.service.IdProvider;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class AbstractWidgetRepositoryTest {

  private static final Instant INSTANT = Instant.now();

  @BeforeEach
  void beforeEach() {
    clearRepo();
    reset(idProviderMock(), clockMock());
  }

  @Test
  void testCreateWithEmptyZEmptyRepoWorks() {
    // given
    nextIdProviderReturn("1");
    nextClockReturn(INSTANT);
    final WidgetToCreate widgetToCreate = randomWidgetToCreate(null);
    // when
    final Widget created = repository().createOne(widgetToCreate);
    // then
    assertTotalSize(1);
    assertThat(created, equalTo(readWidget("1")));
    assertCreatedEqualsToProvided(created, widgetToCreate, Integer.MIN_VALUE, INSTANT);
  }

  @Test
  void testCreateWithEmptyZWorks() {
    // given
    createWidget("1", 1);
    nextIdProviderReturn("2");
    nextClockReturn(INSTANT);
    final WidgetToCreate widgetToCreate = randomWidgetToCreate(null);
    // when
    final Widget created = repository().createOne(widgetToCreate);
    // then
    assertTotalSize(2);
    assertWidgetExists("1", 1);
    assertThat(created, equalTo(readWidget("2")));
    assertCreatedEqualsToProvided(created, widgetToCreate, 2, INSTANT);
  }

  @Test
  void testCreateWithProvidedZWorks() {
    // given
    createWidget("1", 1);
    createWidget("3", 3);
    nextIdProviderReturn("2");
    nextClockReturn(INSTANT);
    final WidgetToCreate widgetToCreate = randomWidgetToCreate(2);
    // when
    final Widget created = repository().createOne(widgetToCreate);
    // then
    assertTotalSize(3);
    assertWidgetExists("1", 1);
    assertWidgetExists("3", 3);
    assertThat(created, equalTo(readWidget("2")));
    assertCreatedEqualsToProvided(created, widgetToCreate, 2, INSTANT);
  }

  @Test
  void testCreateWithProvidedZShifts() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("4", 4);
    nextIdProviderReturn("3");
    nextClockReturn(INSTANT);
    final WidgetToCreate widgetToCreate = randomWidgetToCreate(2);
    // when
    final Widget created = repository().createOne(widgetToCreate);
    // then
    assertTotalSize(4);
    assertWidgetExists("1", 1);
    assertWidgetExists("2", 3);
    assertWidgetExists("3", 2);
    assertWidgetExists("4", 5);
    assertThat(created, equalTo(readWidget("3")));
    assertCreatedEqualsToProvided(created, widgetToCreate, 2, INSTANT);
  }

  @Test
  void testReadOneFoundWorks() {
    // given
    createWidget("1", 1);
    // when
    final Optional<Widget> widgetFound = repository().readOne("1");
    // then
    assertThat(widgetFound.isPresent(), equalTo(true));
    assertThat(widgetFound.get().id(), equalTo("1"));
    assertThat(widgetFound.get().z(), equalTo(1));
  }

  @Test
  void testReadOneNotFound() {
    // when
    final Optional<Widget> widgetFound = repository().readOne("1");
    // then
    assertThat(widgetFound.isEmpty(), equalTo(true));
  }

  @Test
  void testReadAllEmptyFromZWorks() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("3", 3);
    // when
    final List<Widget> widgetsFound = repository().readAll(2, null);
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
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("3", 3);
    createWidget("4", 4);
    // when
    final List<Widget> widgetsFound = repository().readAll(2, 2);
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
    final List<Widget> widgetsFound = repository().readAll(2, null);
    // then
    assertThat(widgetsFound.isEmpty(), equalTo(true));
  }

  @Test
  void testReadAllFromZNotFoundWorks() {
    // given
    createWidget("1", 1);
    // when
    final List<Widget> widgetsFound = repository().readAll(2, 2);
    // then
    assertThat(widgetsFound.isEmpty(), equalTo(true));
  }

  @Test
  void testUpdateXWorks() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("3", 3);
    nextClockReturn(INSTANT);
    // when
    final Optional<Widget> widgetUpdated = repository().updateOne("2", widgetToUpdate(10, null));
    // then
    assertTotalSize(3);
    assertWidgetExists("1", 1);
    assertWidgetExists("2", 2);
    assertWidgetExists("3", 3);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().x(), equalTo(10));
    assertThat(widgetUpdated.get().z(), equalTo(2));
    assertThat(widgetUpdated.get().lastModified(), equalTo(INSTANT));
  }

  @Test
  void testUpdateZWithAvailableZWorks() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("4", 4);
    nextClockReturn(INSTANT);
    // when
    final Optional<Widget> widgetUpdated = repository().updateOne("2", widgetToUpdate(null, 3));
    // then
    assertTotalSize(3);
    assertWidgetExists("1", 1);
    assertWidgetExists("2", 3);
    assertWidgetExists("4", 4);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().z(), equalTo(3));
    assertThat(widgetUpdated.get().lastModified(), equalTo(INSTANT));
  }

  @Test
  void testUpdateZWithUnavailableZShiftWorks() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    createWidget("3", 3);
    createWidget("5", 5);
    nextClockReturn(INSTANT);
    // when
    final Optional<Widget> widgetUpdated = repository().updateOne("2", widgetToUpdate(null, 3));
    // then
    assertTotalSize(4);
    assertWidgetExists("1", 1);
    assertWidgetExists("2", 3);
    assertWidgetExists("3", 4);
    assertWidgetExists("5", 6);
    assertThat(widgetUpdated.isPresent(), equalTo(true));
    assertThat(widgetUpdated.get().id(), equalTo("2"));
    assertThat(widgetUpdated.get().z(), equalTo(3));
    assertThat(widgetUpdated.get().lastModified(), equalTo(INSTANT));
  }

  @Test
  void testUpdateNotFoundWorks() {
    // when
    final Optional<Widget> widgetUpdated = repository().updateOne("1", widgetToUpdate(10, null));
    // then
    assertThat(widgetUpdated.isEmpty(), equalTo(true));
  }

  @Test
  void testDeleteWorks() {
    // given
    createWidget("1", 1);
    createWidget("2", 2);
    // when
    final Optional<Widget> widgetDeleted = repository().deleteOne("1");
    // then
    assertTotalSize(1);
    assertWidgetExists("2", 2);
    assertThat(widgetDeleted.isPresent(), equalTo(true));
    assertThat(widgetDeleted.get().id(), equalTo("1"));
    assertThat(widgetDeleted.get().z(), equalTo(1));
  }

  @Test
  void testDeleteNotFoundWorks() {
    // when
    final Optional<Widget> widgetDeleted = repository().deleteOne("1");
    // then
    assertThat(widgetDeleted.isEmpty(), equalTo(true));
  }

  protected abstract IdProvider idProviderMock();

  protected abstract Clock clockMock();

  protected abstract WidgetRepository repository();

  protected abstract void assertTotalSize(final int size);

  protected abstract void clearRepo();

  private void nextIdProviderReturn(final String id) {
    when(idProviderMock().get()).thenReturn(id);
  }

  private void nextClockReturn(final Instant instant) {
    when(clockMock().instant()).thenReturn(instant);
  }

  private void assertWidgetExists(final String id, final int z) {
    assertThat(readWidget(id).z(), equalTo(z));
  }

  private void createWidget(final String id, final Integer z) {
    nextIdProviderReturn(id);
    nextClockReturn(Instant.now());
    repository().createOne(randomWidgetToCreate(z));
  }

  private Widget readWidget(final String id) {
    return repository().readOne(id).orElseThrow(IllegalStateException::new);
  }

  private static void assertCreatedEqualsToProvided(
      final Widget created,
      final WidgetToCreate widgetToCreate,
      final int z,
      final Instant lastModified) {
    assertThat(created.x(), equalTo(widgetToCreate.x()));
    assertThat(created.y(), equalTo(widgetToCreate.y()));
    assertThat(created.z(), equalTo(z));
    assertThat(created.width(), equalTo(widgetToCreate.width()));
    assertThat(created.height(), equalTo(widgetToCreate.height()));
    assertThat(created.lastModified(), equalTo(lastModified));
  }
}
