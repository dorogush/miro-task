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
package com.adorogush.mirotask.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;

import com.adorogush.mirotask.service.IdProvider;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;

/** Unit test covering {@link H2BasedWidgetRepository}. */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {"widgetRepositoryImplementation: h2"})
class H2BasedWidgetRepositoryTest extends AbstractWidgetRepositoryTest {

  private static final String TABLE_NAME = "widget";

  @MockBean private IdProvider idProvider;
  @MockBean private Clock clock;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private H2BasedWidgetRepository repository;

  @Override
  protected IdProvider idProviderMock() {
    return idProvider;
  }

  @Override
  protected Clock clockMock() {
    return clock;
  }

  @Override
  protected WidgetRepository repository() {
    return repository;
  }

  @Override
  protected void assertTotalSize(final int size) {
    assertThat(countRowsInTable(jdbcTemplate, TABLE_NAME), equalTo(size));
  }

  @Override
  protected void clearRepo() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_NAME);
  }
}
