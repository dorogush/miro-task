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
package com.adorogush.mirotask.service;

import com.adorogush.mirotask.model.RateLimitOperation;
import com.adorogush.mirotask.model.RateLimitStat;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/** A service to support global rate limiting. */
@Service
public class RateLimitService {

  public static final String GLOBAL_ENABLED = "rateLimit.global.enabled";
  public static final String GLOBAL_RPM = "rateLimit.global.rpm";
  private final Environment env;
  private final AtomicReference<LocalBucket> globalBucketRef = new AtomicReference<>();
  private final Map<RateLimitOperation, LocalBucket> specificBucketMap = new ConcurrentHashMap<>();
  /**
   * This map's keys are all properties that we want to track. Map's values point to the refresh
   * method.
   */
  private final Map<String, Runnable> propertyKeyToRefreshMethod = new HashMap<>();

  public RateLimitService(final Environment env) {
    this.env = env;

    // populate propertyKeyToRefreshMethod
    propertyKeyToRefreshMethod.put(GLOBAL_ENABLED, this::refreshGlobal);
    propertyKeyToRefreshMethod.put(GLOBAL_RPM, this::refreshGlobal);
    for (final RateLimitOperation type : RateLimitOperation.values()) {
      propertyKeyToRefreshMethod.put(type.propertyEnabled(), () -> refreshSpecific(type));
      propertyKeyToRefreshMethod.put(type.propertyRpm(), () -> refreshSpecific(type));
    }

    // initial refresh
    refreshGlobal();
    for (final RateLimitOperation operation : RateLimitOperation.values()) {
      refreshSpecific(operation);
    }
  }

  /**
   * Will check if specified operation is allowed and rates are within limits.
   *
   * @return instance of {@link RateLimitStat} holding the current rate limit stats.
   */
  public Optional<RateLimitStat> tryConsume(final RateLimitOperation operation) {
    return Optional.ofNullable(specificBucketMap.get(operation))
        .or(() -> Optional.ofNullable(globalBucketRef.get()))
        .map(this::tryConsume);
  }

  private RateLimitStat tryConsume(final LocalBucket bucket) {
    final ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
    return new RateLimitStat(
        consumptionProbe.isConsumed(),
        getBucketRpm(bucket),
        consumptionProbe.getRemainingTokens(),
        consumptionProbe.getNanosToWaitForRefill());
  }

  private void refreshGlobal() {
    final Optional<Long> newRpmOp =
        getLongProperty(GLOBAL_RPM).filter(rpm -> getBooleanProperty(GLOBAL_ENABLED));
    if (newRpmOp.isEmpty()) {
      globalBucketRef.set(null);
      return;
    }
    final LocalBucket globalBucket = globalBucketRef.get();
    if (globalBucket != null) {
      final long newRpm = newRpmOp.get();
      if (getBucketRpm(globalBucket) != newRpm) {
        globalBucketRef.set(createNewBucket(newRpm));
      }
    } else {
      globalBucketRef.set(createNewBucket(newRpmOp.get()));
    }
  }

  private void refreshSpecific(final RateLimitOperation type) {
    final Optional<Long> newRpmOp =
        getLongProperty(type.propertyRpm())
            .filter(rpm -> getBooleanProperty(type.propertyEnabled()));
    if (newRpmOp.isEmpty()) {
      specificBucketMap.remove(type);
      return;
    }
    final LocalBucket specificBucket = specificBucketMap.get(type);
    if (specificBucket != null) {
      final long newRpm = newRpmOp.get();
      if (getBucketRpm(specificBucket) != newRpm) {
        specificBucketMap.put(type, createNewBucket(newRpm));
      }
    } else {
      specificBucketMap.put(type, createNewBucket(newRpmOp.get()));
    }
  }

  @EventListener
  public synchronized void handlePropertyChanged(final EnvironmentChangeEvent event) {
    for (final String key : event.getKeys()) {
      final Runnable runnable = propertyKeyToRefreshMethod.get(key);
      if (runnable != null) {
        runnable.run();
      }
    }
  }

  private LocalBucket createNewBucket(final long newRpm) {
    final Refill refill = Refill.intervally(newRpm, Duration.ofMinutes(1));
    final Bandwidth limit = Bandwidth.classic(newRpm, refill);
    return Bucket4j.builder().addLimit(limit).build();
  }

  private boolean getBooleanProperty(final String prop) {
    return Boolean.parseBoolean(env.getProperty(prop));
  }

  private Optional<Long> getLongProperty(final String prop) {
    final String property = env.getProperty(prop);
    if (property == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(Long.parseLong(property));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  private long getBucketRpm(final LocalBucket bucket) {
    final Bandwidth[] bandwidths = bucket.getConfiguration().getBandwidths();
    if (bandwidths == null || bandwidths.length != 1) {
      throw new IllegalStateException();
    }
    final Bandwidth bandwidth = bandwidths[0];
    return bandwidth.getRefillTokens();
  }
}
