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
package com.adorogush.mirotask;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Collection of utility methods to create widget related test samples. */
public final class WidgetUtil {

  private WidgetUtil() {}

  public static Widget randomWidget(final int z) {
    return randomWidget(UUID.randomUUID().toString(), z);
  }

  public static Widget randomWidget(final String id, final int z) {
    final ThreadLocalRandom current = ThreadLocalRandom.current();
    return new Widget(
        id,
        current.nextInt(),
        current.nextInt(),
        z,
        current.nextInt(),
        current.nextInt(),
        Instant.ofEpochMilli(current.nextLong()));
  }

  public static WidgetToCreate randomWidgetToCreate(final Integer z) {
    final ThreadLocalRandom current = ThreadLocalRandom.current();
    return new WidgetToCreate(
        current.nextInt(), current.nextInt(), z, current.nextInt(), current.nextInt());
  }

  public static WidgetToUpdate widgetToUpdate(final Integer x, final Integer z) {
    return new WidgetToUpdate(x, null, z, null, null);
  }
}
