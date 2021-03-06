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
 * Enum to mention all possible types of operations that must be rate limited.<br>
 * Each entry also has a pair of property keys: {@code enabled} and {@code rpm value}.
 */
public enum RateLimitOperation {
  CREATE("rateLimit.create.enabled", "rateLimit.create.rpm"),
  READ_ONE("rateLimit.readOne.enabled", "rateLimit.readOne.rpm"),
  READ_ALL("rateLimit.readAll.enabled", "rateLimit.readAll.rpm"),
  UPDATE("rateLimit.update.enabled", "rateLimit.update.rpm"),
  DELETE("rateLimit.delete.enabled", "rateLimit.delete.rpm");
  private final String propertyEnabled;
  private final String propertyRpm;

  RateLimitOperation(final String propertyEnabled, final String propertyRpm) {
    this.propertyEnabled = propertyEnabled;
    this.propertyRpm = propertyRpm;
  }

  public String propertyEnabled() {
    return propertyEnabled;
  }

  public String propertyRpm() {
    return propertyRpm;
  }
}
