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
package com.adorogush.mirotask.service;

import static com.adorogush.mirotask.WidgetUtil.randomWidgetToCreate;
import static com.adorogush.mirotask.WidgetUtil.widgetToUpdate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.adorogush.mirotask.exception.NotFoundException;
import com.adorogush.mirotask.exception.TooManyRequestsException;
import com.adorogush.mirotask.model.RateLimitOperation;
import com.adorogush.mirotask.model.RateLimitStat;
import com.adorogush.mirotask.repository.WidgetRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link WidgetService}. */
class WidgetServiceTest {

  private final WidgetRepository repository = mock(WidgetRepository.class);
  private final RateLimitService rateLimitService = mock(RateLimitService.class);
  private final WidgetService widgetService = new WidgetService(repository, rateLimitService);

  @BeforeEach
  void beforeEach() {
    reset(repository, rateLimitService);
  }

  @Test
  void testCreateThrowsTooManyRequestsException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.CREATE))
        .thenReturn(Optional.of(new RateLimitStat(false, 1000, 0, 9999)));
    assertThrows(
        TooManyRequestsException.class, () -> widgetService.createOne(randomWidgetToCreate(1)));
  }

  @Test
  void testReadOneThrowsTooManyRequestsException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.READ_ONE))
        .thenReturn(Optional.of(new RateLimitStat(false, 1000, 0, 9999)));
    assertThrows(TooManyRequestsException.class, () -> widgetService.readOne("1"));
  }

  @Test
  void testReadOneThrowsNotFoundException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.READ_ONE))
        .thenReturn(Optional.of(new RateLimitStat(true, 1000, 0, 9999)));
    when(repository.readOne("1")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> widgetService.readOne("1"));
  }

  @Test
  void testReadAllThrowsTooManyRequestsException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.READ_ALL))
        .thenReturn(Optional.of(new RateLimitStat(false, 1000, 0, 9999)));
    assertThrows(TooManyRequestsException.class, () -> widgetService.readAll(1, null));
  }

  @Test
  void testUpdateThrowsTooManyRequestsException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.UPDATE))
        .thenReturn(Optional.of(new RateLimitStat(false, 1000, 0, 9999)));
    assertThrows(
        TooManyRequestsException.class,
        () -> widgetService.updateOne("1", widgetToUpdate(null, 1)));
  }

  @Test
  void testUpdateThrowsNotFoundException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.UPDATE))
        .thenReturn(Optional.of(new RateLimitStat(true, 1000, 0, 9999)));
    when(repository.updateOne(eq("1"), any())).thenReturn(Optional.empty());
    assertThrows(
        NotFoundException.class, () -> widgetService.updateOne("1", widgetToUpdate(null, 1)));
  }

  @Test
  void testDeleteThrowsTooManyRequestsException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.DELETE))
        .thenReturn(Optional.of(new RateLimitStat(false, 1000, 0, 9999)));
    assertThrows(TooManyRequestsException.class, () -> widgetService.deleteOne("1"));
  }

  @Test
  void testDeleteThrowsNotFoundException() {
    // given
    when(rateLimitService.tryConsume(RateLimitOperation.DELETE))
        .thenReturn(Optional.of(new RateLimitStat(true, 1000, 0, 9999)));
    when(repository.deleteOne("1")).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> widgetService.deleteOne("1"));
  }
}
