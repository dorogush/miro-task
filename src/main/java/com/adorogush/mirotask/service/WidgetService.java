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

import com.adorogush.mirotask.exception.NotFoundException;
import com.adorogush.mirotask.exception.TooManyRequestsException;
import com.adorogush.mirotask.model.Page;
import com.adorogush.mirotask.model.RateLimitOperation;
import com.adorogush.mirotask.model.RateLimitStat;
import com.adorogush.mirotask.model.ServiceResponse;
import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import com.adorogush.mirotask.repository.WidgetRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** A Service for CRUD Widget operations. */
@Service
public class WidgetService {

  private final WidgetRepository widgetRepository;
  private final RateLimitService rateLimitService;

  public WidgetService(
      final WidgetRepository widgetRepository, final RateLimitService rateLimitService) {
    this.widgetRepository = widgetRepository;
    this.rateLimitService = rateLimitService;
  }

  public ServiceResponse<Widget> createOne(final WidgetToCreate widgetToCreate) {
    final RateLimitStat rateLimitStat = tryConsume(RateLimitOperation.CREATE);
    final Widget created = widgetRepository.createOne(widgetToCreate);
    return new ServiceResponse<>(created, rateLimitStat);
  }

  public ServiceResponse<Widget> readOne(final String id) {
    final RateLimitStat rateLimitStat = tryConsume(RateLimitOperation.READ_ONE);
    final Widget widgetFound = widgetRepository.readOne(id).orElseThrow(() -> widgetNotFound(id));
    return new ServiceResponse<>(widgetFound, rateLimitStat);
  }

  public ServiceResponse<Page<Widget>> readAll(final int perPage, final Integer fromZ) {
    final RateLimitStat rateLimitStat = tryConsume(RateLimitOperation.READ_ALL);
    final List<Widget> widgetsFound = widgetRepository.readAll(perPage + 1, fromZ);
    return new ServiceResponse<>(Page.of(widgetsFound, perPage), rateLimitStat);
  }

  public ServiceResponse<Widget> updateOne(final String id, final WidgetToUpdate widgetToUpdate) {
    final RateLimitStat rateLimitStat = tryConsume(RateLimitOperation.UPDATE);
    final Widget updated =
        widgetRepository.updateOne(id, widgetToUpdate).orElseThrow(() -> widgetNotFound(id));
    return new ServiceResponse<>(updated, rateLimitStat);
  }

  public ServiceResponse<Widget> deleteOne(final String id) {
    final RateLimitStat rateLimitStat = tryConsume(RateLimitOperation.DELETE);
    final Widget deleted = widgetRepository.deleteOne(id).orElseThrow(() -> widgetNotFound(id));
    return new ServiceResponse<>(deleted, rateLimitStat);
  }

  private static NotFoundException widgetNotFound(final String id) {
    return new NotFoundException(String.format("Could not find Widget %s", id));
  }

  private RateLimitStat tryConsume(final RateLimitOperation operation) {
    return rateLimitService
        .tryConsume(operation)
        .map(
            rateLimitStat -> {
              if (!rateLimitStat.isConsumed()) {
                throw new TooManyRequestsException(rateLimitStat);
              }
              return rateLimitStat;
            })
        .orElse(null);
  }
}
