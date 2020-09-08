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

/** Immutable data class to hold the rate limit statistics for operation. */
public class RateLimitStat {

  private final boolean isConsumed;
  private final long rpm;
  private final long available;
  private final long nanosToWaitForRefill;

  public RateLimitStat(
      final boolean isConsumed,
      final long rpm,
      final long available,
      final long nanosToWaitForRefill) {
    this.isConsumed = isConsumed;
    this.rpm = rpm;
    this.available = available;
    this.nanosToWaitForRefill = nanosToWaitForRefill;
  }

  /** {@code true} if operation happened and was not rate limited. */
  public boolean isConsumed() {
    return isConsumed;
  }

  /** Returns current value of Requests Per Minute configured. */
  public long rpm() {
    return rpm;
  }

  /** Returns the available remaining operations. */
  public long available() {
    return available;
  }

  /**
   * Returns nanoseconds until the available remaining operations bucket will refill. {@code 0} if
   * consumed successfully.
   */
  public long nanosToWaitForRefill() {
    return nanosToWaitForRefill;
  }

  @Override
  public String toString() {
    return "Probe{"
        + "isConsumed="
        + isConsumed
        + ", rpm="
        + rpm
        + ", available="
        + available
        + ", nanosToWaitForRefill="
        + nanosToWaitForRefill
        + '}';
  }
}
