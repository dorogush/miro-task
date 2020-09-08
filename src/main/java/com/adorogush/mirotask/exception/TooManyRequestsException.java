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
package com.adorogush.mirotask.exception;

import com.adorogush.mirotask.model.RateLimitStat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Exception class to represent client error Too Many Requests. */
public class TooManyRequestsException extends ResponseStatusException {

  private final RateLimitStat rateLimitStat;

  public TooManyRequestsException(final RateLimitStat rateLimitStat) {
    super(HttpStatus.TOO_MANY_REQUESTS, "Too many requests.");
    this.rateLimitStat = rateLimitStat;
  }

  public RateLimitStat rateLimitStat() {
    return rateLimitStat;
  }
}
