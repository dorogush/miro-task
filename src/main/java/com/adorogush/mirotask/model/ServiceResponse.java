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
package com.adorogush.mirotask.model;

/**
 * Immutable data class to hold the pair: model returned from the service layer and current rate
 * limit stats.
 */
public class ServiceResponse<T> {

  private final T model;
  private final RateLimitStat rateLimitStat;

  public ServiceResponse(final T model, final RateLimitStat rateLimitStat) {
    this.model = model;
    this.rateLimitStat = rateLimitStat;
  }

  public ServiceResponse(final T model) {
    this(model, null);
  }

  public T model() {
    return model;
  }

  public RateLimitStat rateLimitStat() {
    return rateLimitStat;
  }
}
