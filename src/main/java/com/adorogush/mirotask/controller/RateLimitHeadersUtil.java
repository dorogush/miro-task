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
package com.adorogush.mirotask.controller;

import com.adorogush.mirotask.model.RateLimitStat;
import org.springframework.http.HttpHeaders;

/** Utility class that can populate extra response headers with current rate limiting stats. */
public final class RateLimitHeadersUtil {

  public static final String X_REQUESTS_PER_MINUTE = "X-Requests-Per-Minute";
  public static final String X_REQUESTS_AVAILABLE = "X-Requests-Available";
  public static final String X_NANOS_UNTIL_REFILL = "X-Nanos-Until-Refill";

  private RateLimitHeadersUtil() {}

  public static void addRateLimitHeaders(
      final HttpHeaders headers, final RateLimitStat rateLimitStat) {
    if (rateLimitStat == null) {
      return;
    }
    headers.add(X_REQUESTS_PER_MINUTE, String.valueOf(rateLimitStat.rpm()));
    headers.add(X_REQUESTS_AVAILABLE, String.valueOf(rateLimitStat.available()));
    headers.add(X_NANOS_UNTIL_REFILL, String.valueOf(rateLimitStat.nanosToWaitForRefill()));
  }
}
