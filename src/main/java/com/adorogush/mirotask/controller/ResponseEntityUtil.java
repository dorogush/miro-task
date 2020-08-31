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

import static com.adorogush.mirotask.controller.LinkHeaderUtil.addLinkHeader;
import static com.adorogush.mirotask.controller.RateLimitHeadersUtil.addRateLimitHeaders;

import com.adorogush.mirotask.model.Page;
import com.adorogush.mirotask.model.RateLimitStat;
import com.adorogush.mirotask.model.ServiceResponse;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

/** Utility class to build {@link ResponseEntity}. */
public final class ResponseEntityUtil {

  private ResponseEntityUtil() {}

  public static <T> ResponseEntity<T> created(final ServiceResponse<T> er) {
    return responseEntity(HttpStatus.CREATED, er);
  }

  public static <T> ResponseEntity<T> ok(final ServiceResponse<T> er) {
    return responseEntity(HttpStatus.OK, er);
  }

  public static <T> ResponseEntity<T> responseEntity(
      final HttpStatus status, final ServiceResponse<T> er) {
    return responseEntity(status, er.model(), er.rateLimitStat());
  }

  public static <T> ResponseEntity<T> responseEntity(final HttpStatus status, final T body) {
    return responseEntity(status, body, null, null);
  }

  public static <T> ResponseEntity<T> responseEntity(
      final HttpStatus status, final T body, final RateLimitStat rateLimitStat) {
    return responseEntity(status, body, rateLimitStat, null);
  }

  public static <T> ResponseEntity<List<T>> page(
      final ServiceResponse<Page<T>> page,
      final HttpServletRequest request,
      final String nextPageTokenKey,
      final Function<T, String> nextPageTokenValueExtractor) {
    if (!page.model().hasNext()) {
      return responseEntity(HttpStatus.OK, page.model().elements(), page.rateLimitStat());
    }
    return responseEntity(
        HttpStatus.OK,
        page.model().elements(),
        page.rateLimitStat(),
        bodyBuilder ->
            bodyBuilder.headers(
                headers ->
                    addLinkHeader(
                        headers,
                        request,
                        nextPageTokenKey,
                        nextPageTokenValueExtractor.apply(page.model().next()))));
  }

  public static <T> ResponseEntity<T> responseEntity(
      final HttpStatus status,
      final T body,
      final RateLimitStat rateLimitStat,
      final UnaryOperator<BodyBuilder> bodyBuilderMutator) {
    BodyBuilder bodyBuilder = ResponseEntity.status(status);
    if (rateLimitStat != null) {
      bodyBuilder =
          bodyBuilder.headers(httpHeaders -> addRateLimitHeaders(httpHeaders, rateLimitStat));
    }
    if (bodyBuilderMutator != null) {
      bodyBuilder = bodyBuilderMutator.apply(bodyBuilder);
    }
    if (body == null) {
      return bodyBuilder.build();
    }
    return bodyBuilder.body(body);
  }
}
