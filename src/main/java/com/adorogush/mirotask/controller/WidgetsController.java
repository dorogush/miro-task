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

import static com.adorogush.mirotask.controller.ResponseEntityUtil.created;
import static com.adorogush.mirotask.controller.ResponseEntityUtil.ok;
import static com.adorogush.mirotask.controller.ResponseEntityUtil.page;

import com.adorogush.mirotask.model.Page;
import com.adorogush.mirotask.model.ServiceResponse;
import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import com.adorogush.mirotask.service.WidgetService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for {@code /widgets} endpoints. */
@RestController
@RequestMapping(path = "/widgets")
public class WidgetsController {

  private static final String PER_PAGE_QP = "perPage";
  private static final String FROM_Z_QP = "fromZ";
  private final WidgetService widgetService;
  private final int perPageDefault;
  private final int perPageMax;

  public WidgetsController(
      final WidgetService widgetService,
      @Value("${perPageDefault}") final int perPageDefault,
      @Value("${perPageMax}") final int perPageMax) {
    this.widgetService = widgetService;
    this.perPageDefault = perPageDefault;
    this.perPageMax = perPageMax;
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Widget> post(@RequestBody final WidgetToCreate widget) {
    return created(widgetService.createOne(widget));
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Widget> getOne(@PathVariable("id") final String id) {
    return ok(widgetService.readOne(id));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Widget>> getMany(
      @RequestParam(name = PER_PAGE_QP, required = false) final Integer perPageUser,
      @RequestParam(name = FROM_Z_QP, required = false) final Integer fromZ,
      @Autowired final HttpServletRequest request) {
    final int perPage = getPerPage(perPageUser);
    final ServiceResponse<Page<Widget>> widgetsFound = widgetService.readAll(perPage, fromZ);
    return page(widgetsFound, request, FROM_Z_QP, widget -> String.valueOf(widget.z()));
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Widget> putOne(
      @PathVariable("id") final String id, @RequestBody final WidgetToUpdate widget) {
    return ok(widgetService.updateOne(id, widget));
  }

  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Widget> deleteOne(@PathVariable("id") final String id) {
    return ok(widgetService.deleteOne(id));
  }

  private int getPerPage(final Integer perPageUser) {
    if (perPageUser == null) {
      return perPageDefault;
    }
    return Math.min(perPageUser, perPageMax);
  }
}
