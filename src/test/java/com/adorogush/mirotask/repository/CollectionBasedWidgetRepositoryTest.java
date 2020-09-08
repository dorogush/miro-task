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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.mock;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.service.IdProvider;
import java.time.Clock;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.jupiter.api.TestInstance;

/** Unit test covering {@link CollectionBasedWidgetRepository}. */
@TestInstance(value = PER_CLASS)
class CollectionBasedWidgetRepositoryTest extends AbstractWidgetRepositoryTest {

  private final IdProvider idProvider = mock(IdProvider.class);
  private final Clock clock = mock(Clock.class);
  private final Map<String, Widget> idToWidget = new ConcurrentHashMap<>();
  private final SortedMap<Integer, Widget> zToWidget = new ConcurrentSkipListMap<>();
  private final WidgetRepository repository =
      new CollectionBasedWidgetRepository(idProvider, clock, idToWidget, zToWidget);

  @Override
  protected IdProvider idProviderMock() {
    return idProvider;
  }

  @Override
  protected Clock clockMock() {
    return clock;
  }

  @Override
  protected WidgetRepository repository() {
    return repository;
  }

  @Override
  protected void assertTotalSize(final int size) {
    assertThat(idToWidget.size(), equalTo(size));
    assertThat(zToWidget.size(), equalTo(size));
  }

  @Override
  protected void clearRepo() {
    idToWidget.clear();
    zToWidget.clear();
  }
}
