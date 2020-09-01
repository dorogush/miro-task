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
package com.adorogush.mirotask.repository;

import static com.adorogush.mirotask.exception.ConflictException.checkOverflow;
import static java.util.Objects.requireNonNull;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import com.adorogush.mirotask.service.IdProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Implementation of {@link WidgetRepository} based on H2 in-memory database. */
@Repository
@ConditionalOnProperty(name = "widgetRepositoryImplementation", havingValue = "h2")
public class H2BasedWidgetRepository implements WidgetRepository {

  private static final Logger log = LogManager.getLogger();
  private final IdProvider idProvider;
  private final Clock clock;
  private final TransactionTemplate transactionTemplate;
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert widgetTableInsert;

  public H2BasedWidgetRepository(
      final IdProvider idProvider,
      final Clock clock,
      final TransactionTemplate transactionTemplate,
      final JdbcTemplate jdbcTemplate) {
    this.idProvider = idProvider;
    this.clock = clock;
    this.transactionTemplate = transactionTemplate;
    this.jdbcTemplate = jdbcTemplate;
    this.widgetTableInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("widget");
    log.info("H2 based WidgetRepository initialized.");
  }

  @Override
  public Widget createOne(final WidgetToCreate widgetToCreate) {
    return transactionTemplate.execute(
        status -> {
          // generate unique id
          final String id = idProvider.get();

          final Widget widget;
          if (widgetToCreate.z() == null) {
            final int z =
                getHighestZ().map(lastZ -> checkOverflow(lastZ) + 1).orElse(Integer.MIN_VALUE);
            widget = widgetToCreateToWidget(widgetToCreate, id, z);
          } else {
            widget = widgetToCreateToWidget(widgetToCreate, id, widgetToCreate.z());
            if (getIfWidgetExistsByZ(widget.z())) {
              shift(widget.z());
            }
          }
          widgetTableInsert.execute(widgetToJdbcMap(widget));
          return widget;
        });
  }

  @Override
  public Optional<Widget> readOne(final String id) {
    return transactionTemplate.execute(status -> readOneInternal(id));
  }

  private Optional<Widget> readOneInternal(final String id) {
    final List<Widget> found =
        jdbcTemplate.query(
            "select * from widget where id = ?", H2BasedWidgetRepository::widgetRowMapper, id);
    return Optional.of(found).filter(l -> !l.isEmpty()).map(l -> l.get(0));
  }

  private Optional<Integer> getHighestZ() {
    final List<Integer> found =
        jdbcTemplate.query(
            "select z from widget order by z desc limit 1", (rs, rowNum) -> rs.getInt(1));
    return Optional.of(found).filter(l -> !l.isEmpty()).map(l -> l.get(0));
  }

  private boolean getIfWidgetExistsById(final String id) {
    return requireNonNull(
            jdbcTemplate.queryForObject(
                "select count(*) from widget where id = ?", Integer.class, id))
        > 0;
  }

  private boolean getIfWidgetExistsByZ(final int z) {
    return requireNonNull(
            jdbcTemplate.queryForObject(
                "select count(*) from widget where z = ?", Integer.class, z))
        > 0;
  }

  @Override
  public List<Widget> readAll(final int perPage, final Integer fromZ) {
    return transactionTemplate.execute(
        status -> {
          if (fromZ == null) {
            return jdbcTemplate.query(
                "select * from widget order by z limit ?",
                H2BasedWidgetRepository::widgetRowMapper,
                perPage);
          } else {
            return jdbcTemplate.query(
                "select * from widget where z >= ? order by z limit ?",
                H2BasedWidgetRepository::widgetRowMapper,
                fromZ,
                perPage);
          }
        });
  }

  @Override
  public Optional<Widget> updateOne(final String id, final WidgetToUpdate widgetToUpdate) {
    return transactionTemplate.execute(
        status -> {
          if (widgetToUpdate.z() != null && getIfWidgetExistsByZ(widgetToUpdate.z())) {
            // we must explicitly check if widget exists by id before shifting
            if (!getIfWidgetExistsById(id)) {
              return Optional.empty();
            }
            shift(widgetToUpdate.z());
          }
          final List<Object> values = new ArrayList<>();
          final String sql = buildUpdateSql(id, widgetToUpdate, values);
          final int updated = jdbcTemplate.update(sql, values.toArray());
          if (updated < 1) {
            return Optional.empty();
          }
          return readOneInternal(id);
        });
  }

  private void shift(final int z) {
    jdbcTemplate.update("update widget set z = z + 1 where z >= ?", z);
  }

  @Override
  public Optional<Widget> deleteOne(final String id) {
    return transactionTemplate.execute(
        status -> {
          final Optional<Widget> widgetFound = readOneInternal(id);
          widgetFound.ifPresent(w -> jdbcTemplate.update("delete from widget where id = ?", id));
          return widgetFound;
        });
  }

  private static Widget widgetRowMapper(final ResultSet rs, final int rowNum) throws SQLException {
    return new Widget(
        rs.getString(1),
        rs.getInt(2),
        rs.getInt(3),
        rs.getInt(4),
        rs.getInt(5),
        rs.getInt(6),
        rs.getObject(7, Instant.class));
  }

  private String buildUpdateSql(
      final String id, final WidgetToUpdate widgetToUpdate, final List<Object> values) {
    final StringJoiner updateSql =
        new StringJoiner(" = ?, ", "update widget set ", " = ? where id = ?");
    addColumnToUpdateSql("x", widgetToUpdate.x(), updateSql, values);
    addColumnToUpdateSql("y", widgetToUpdate.y(), updateSql, values);
    addColumnToUpdateSql("z", widgetToUpdate.z(), updateSql, values);
    addColumnToUpdateSql("width", widgetToUpdate.width(), updateSql, values);
    addColumnToUpdateSql("height", widgetToUpdate.height(), updateSql, values);
    addColumnToUpdateSql("lastModified", clock.instant(), updateSql, values);
    values.add(id);
    return updateSql.toString();
  }

  private void addColumnToUpdateSql(
      final String column,
      final Object value,
      final StringJoiner updateSql,
      final List<Object> objects) {
    if (value != null) {
      updateSql.add(column);
      objects.add(value);
    }
  }

  private static Map<String, Object> widgetToJdbcMap(final Widget widget) {
    return Map.of(
        "id",
        widget.id(),
        "x",
        widget.x(),
        "y",
        widget.y(),
        "z",
        widget.z(),
        "width",
        widget.width(),
        "height",
        widget.height(),
        "lastModified",
        widget.lastModified());
  }

  private Widget widgetToCreateToWidget(
      final WidgetToCreate widgetToCreate, final String id, final int z) {
    return new Widget(
        id,
        widgetToCreate.x(),
        widgetToCreate.y(),
        z,
        widgetToCreate.width(),
        widgetToCreate.height(),
        clock.instant());
  }
}
