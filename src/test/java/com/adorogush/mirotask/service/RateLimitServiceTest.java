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
package com.adorogush.mirotask.service;

import static com.adorogush.mirotask.service.RateLimitService.GLOBAL_ENABLED;
import static com.adorogush.mirotask.service.RateLimitService.GLOBAL_RPM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adorogush.mirotask.model.RateLimitOperation;
import com.adorogush.mirotask.model.RateLimitStat;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.core.env.Environment;

/** Tests for {@link RateLimitService}. */
class RateLimitServiceTest {

  @Test
  void testScenario() {
    // prepare config properties
    // all rate limits are disabled
    final Environment environment = mock(Environment.class);
    when(environment.getProperty(GLOBAL_ENABLED)).thenReturn("false");
    when(environment.getProperty(GLOBAL_RPM)).thenReturn("2");
    for (final RateLimitOperation rateLimitOperation : RateLimitOperation.values()) {
      when(environment.getProperty(rateLimitOperation.propertyEnabled())).thenReturn("false");
      when(environment.getProperty(rateLimitOperation.propertyRpm())).thenReturn("123");
    }
    final RateLimitService rateLimitService = new RateLimitService(environment);

    // try consume and expect no rate limits
    assertThat(rateLimitService.tryConsume(RateLimitOperation.CREATE).isEmpty(), equalTo(true));

    // change config property, enable "create" rate limit
    changeProperty(
        environment, rateLimitService, RateLimitOperation.CREATE.propertyEnabled(), "true");

    // try consume and expect rate limit works
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.CREATE), true, 123L, 122L, true);

    // change config property, enable "global" rate limit
    changeProperty(environment, rateLimitService, GLOBAL_ENABLED, "true");

    // try consume 1 and expect rate limit works
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.READ_ONE), true, 2L, 1L, true);

    // try consume 2 and expect rate limit works
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.READ_ONE), true, 2L, 0L, true);

    // try consume 3 and expect not consumed
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.READ_ONE), false, 2L, 0L, false);

    // change config property, change "global" rate limit to 100
    changeProperty(environment, rateLimitService, GLOBAL_RPM, "100");

    // try consume 1
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.READ_ONE), true, 100L, 99L, true);

    // change config property, change "create" rate limit to 10
    changeProperty(environment, rateLimitService, RateLimitOperation.CREATE.propertyRpm(), "10");

    // try consume 1
    assertRateLimitStat(
        rateLimitService.tryConsume(RateLimitOperation.CREATE), true, 10L, 9L, true);
  }

  private static void changeProperty(
      final Environment environment,
      final RateLimitService rateLimitService,
      final String key,
      final String value) {
    when(environment.getProperty(key)).thenReturn(value);
    final EnvironmentChangeEvent environmentChangeEvent = mock(EnvironmentChangeEvent.class);
    when(environmentChangeEvent.getKeys()).thenReturn(Set.of(key));
    rateLimitService.handlePropertyChanged(environmentChangeEvent);
  }

  private static void assertRateLimitStat(
      final Optional<RateLimitStat> rateLimitStat,
      final boolean isConsumed,
      final long rpm,
      final long available,
      final boolean expectZeroNanosToWaitForRefill) {
    assertThat(rateLimitStat.isPresent(), equalTo(true));
    assertThat(rateLimitStat.get().isConsumed(), equalTo(isConsumed));
    assertThat(rateLimitStat.get().rpm(), equalTo(rpm));
    assertThat(rateLimitStat.get().available(), equalTo(available));
    if (expectZeroNanosToWaitForRefill) {
      assertThat(rateLimitStat.get().nanosToWaitForRefill(), equalTo(0L));
    } else {
      assertThat(rateLimitStat.get().nanosToWaitForRefill(), greaterThan(0L));
    }
  }
}
