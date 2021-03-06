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
package com.adorogush.mirotask.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** This class contains utility methods to build a Link header value for pagination. */
public final class LinkHeaderUtil {

  private LinkHeaderUtil() {}

  public static void addLinkHeader(
      final HttpHeaders headers,
      final HttpServletRequest request,
      final String nextPageTokenKey,
      final String nextPageTokenValue) {
    headers.add("Link", buildLinkHeader(request, nextPageTokenKey, nextPageTokenValue));
  }

  private static String buildLinkHeader(
      final HttpServletRequest request,
      final String nextPageTokenKey,
      final String nextPageTokenValue) {
    final String uriString =
        ServletUriComponentsBuilder.fromRequest(request)
            .replaceQueryParam(nextPageTokenKey, nextPageTokenValue)
            .toUriString();
    return "<" + uriString + ">; rel=\"next\"";
  }
}
