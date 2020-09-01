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

import static com.adorogush.mirotask.exception.ConflictException.checkOverflow;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import com.adorogush.mirotask.service.IdProvider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link WidgetRepository} based on concurrent collections.<br>
 * <b>Create</b>, <b>Update</b> and <b>Delete</b> operations will acquire shared {@link
 * #writesGlobalLock}, meaning no parallel writes are allowed.<br>
 * <b>Read one</b> operation is never blocked.<br>
 * <b>Read all</b> operation will not block in most cases. One exception is when <b>Create</b> or
 * <b>Update</b> operation caused the sequential shift operation. While shift is in progress, {@link
 * #writesShiftLock} is acquired by write operation, <b>Read all</b> will wait until {@link
 * #readsShiftLock} can be acquired.
 */
@Repository
@ConditionalOnProperty(name = "widgetRepositoryImplementation", havingValue = "collection")
public class CollectionBasedWidgetRepository implements WidgetRepository {

  private static final Logger log = LogManager.getLogger();
  private final Map<String, Widget> idToWidget;
  private final SortedMap<Integer, Widget> zToWidget;
  private final IdProvider idProvider;
  private final Clock clock;
  private final Lock writesGlobalLock = new ReentrantLock();
  private final Lock writesShiftLock;
  private final Lock readsShiftLock;

  @Autowired
  public CollectionBasedWidgetRepository(final IdProvider idProvider, final Clock clock) {
    this(idProvider, clock, new ConcurrentHashMap<>(), new ConcurrentSkipListMap<>());
  }

  /*for tests*/ CollectionBasedWidgetRepository(
      final IdProvider idProvider,
      final Clock clock,
      final Map<String, Widget> idToWidget,
      final SortedMap<Integer, Widget> zToWidget) {
    this.idProvider = idProvider;
    this.clock = clock;
    this.idToWidget = idToWidget;
    this.zToWidget = zToWidget;

    final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    writesShiftLock = rwLock.writeLock();
    readsShiftLock = rwLock.readLock();

    log.info("Collection based WidgetRepository initialized.");
  }

  @Override
  public Widget createOne(final WidgetToCreate widgetToCreate) {
    writesGlobalLock.lock();
    try {
      // generate unique id
      String id;
      do {
        id = idProvider.get();
      } while (idToWidget.get(id) != null);

      final Widget widget;
      if (widgetToCreate.z() == null) {
        final int z =
            zToWidget.isEmpty() ? Integer.MIN_VALUE : checkOverflow(zToWidget.lastKey()) + 1;
        widget = widgetToCreateToWidget(widgetToCreate, id, z);
      } else {
        widget = widgetToCreateToWidget(widgetToCreate, id, widgetToCreate.z());
        if (zToWidget.get(widget.z()) != null) {
          shift(widget.z());
        }
      }
      idToWidget.put(widget.id(), widget);
      zToWidget.put(widget.z(), widget);
      return widget;
    } finally {
      writesGlobalLock.unlock();
    }
  }

  @Override
  public Optional<Widget> readOne(final String id) {
    return Optional.ofNullable(idToWidget.get(id));
  }

  @Override
  public List<Widget> readAll(final int perPage, final Integer fromZ) {
    readsShiftLock.lock();
    try {
      final Collection<Widget> allWidgets;
      if (fromZ == null) {
        allWidgets = zToWidget.values();
      } else {
        allWidgets = zToWidget.tailMap(fromZ).values();
      }
      final int returnListSize = Math.min(allWidgets.size(), perPage);
      final List<Widget> returnList = new ArrayList<>(returnListSize);
      int i = 0;
      for (final Widget value : allWidgets) {
        if (i++ >= returnListSize) {
          break;
        }
        returnList.add(value);
      }
      return returnList;
    } finally {
      readsShiftLock.unlock();
    }
  }

  @Override
  public Optional<Widget> updateOne(final String id, final WidgetToUpdate widgetToUpdate) {
    writesGlobalLock.lock();
    try {
      final Widget widgetFound = idToWidget.get(id);
      if (widgetFound == null) {
        return Optional.empty();
      }
      final Widget widgetUpdated = widgetToUpdateToWidget(widgetFound, widgetToUpdate);
      if (widgetToUpdate.z() != null) {
        zToWidget.remove(widgetFound.z());
        if (zToWidget.get(widgetUpdated.z()) != null) {
          shift(widgetUpdated.z());
        }
      }
      idToWidget.put(widgetUpdated.id(), widgetUpdated);
      zToWidget.put(widgetUpdated.z(), widgetUpdated);
      return Optional.of(widgetUpdated);
    } finally {
      writesGlobalLock.unlock();
    }
  }

  @Override
  public Optional<Widget> deleteOne(final String id) {
    writesGlobalLock.lock();
    try {
      final Widget widgetFound = idToWidget.get(id);
      if (widgetFound == null) {
        return Optional.empty();
      }
      idToWidget.remove(widgetFound.id());
      zToWidget.remove(widgetFound.z());
      return Optional.of(widgetFound);
    } finally {
      writesGlobalLock.unlock();
    }
  }

  private Widget widgetToCreateToWidget(
      final WidgetToCreate widgetToCreate, final String id, final int z) {
    return new Widget(
        id,
        widgetToCreate.x(),
        widgetToCreate.y(),
        z,
        widgetToCreate.width(),
        widgetToCreate.height(),
        clock.instant());
  }

  private Widget widgetToUpdateToWidget(
      final Widget existing, final WidgetToUpdate widgetToUpdate) {
    return new Widget(
        existing.id(),
        widgetToUpdate.x() != null ? widgetToUpdate.x() : existing.x(),
        widgetToUpdate.y() != null ? widgetToUpdate.y() : existing.y(),
        widgetToUpdate.z() != null ? widgetToUpdate.z() : existing.z(),
        widgetToUpdate.width() != null ? widgetToUpdate.width() : existing.width(),
        widgetToUpdate.height() != null ? widgetToUpdate.height() : existing.height(),
        clock.instant());
  }

  private void shift(final int z) {
    final Map<Integer, Widget> tailMap = zToWidget.tailMap(z);
    if (tailMap.isEmpty()) {
      return;
    }
    writesShiftLock.lock();
    try {
      final Collection<Widget> values = tailMap.values();
      final Iterator<Widget> iterator = values.iterator();
      final List<Widget> removed = new ArrayList<>(values.size());
      while (iterator.hasNext()) {
        removed.add(iterator.next());
        iterator.remove();
      }
      for (final Widget removedWidget : removed) {
        final Widget newWidget = removedWidget.incZ();
        idToWidget.put(newWidget.id(), newWidget);
        zToWidget.put(newWidget.z(), newWidget);
      }
    } finally {
      writesShiftLock.unlock();
    }
  }
}
