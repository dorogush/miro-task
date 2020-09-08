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
package com.adorogush.mirotask.mvc;

import static com.adorogush.mirotask.WidgetUtil.randomWidget;
import static com.adorogush.mirotask.controller.RateLimitHeadersUtil.X_NANOS_UNTIL_REFILL;
import static com.adorogush.mirotask.controller.RateLimitHeadersUtil.X_REQUESTS_AVAILABLE;
import static com.adorogush.mirotask.controller.RateLimitHeadersUtil.X_REQUESTS_PER_MINUTE;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adorogush.mirotask.WidgetUtil;
import com.adorogush.mirotask.controller.WidgetsController;
import com.adorogush.mirotask.exception.TooManyRequestsException;
import com.adorogush.mirotask.model.Page;
import com.adorogush.mirotask.model.RateLimitStat;
import com.adorogush.mirotask.model.ServiceResponse;
import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.service.WidgetService;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** This is a MockMVC test covering {@link WidgetsController}. */
@WebMvcTest(WidgetsController.class)
@ActiveProfiles("dev")
@TestPropertySource(
    properties = {"logging.level.org.springframework.test.web.servlet.result: debug"})
class WidgetsControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private WidgetService widgetService;

  @BeforeEach
  void beforeEach() {
    reset(widgetService);
  }

  @Test
  void testPostWorks() throws Exception {
    // given
    final Widget widget = randomWidget(1);
    when(widgetService.createOne(any())).thenReturn(new ServiceResponse<>(widget));
    final String body =
        String.format(
            ""
                + "{"
                + "  \"x\": %d,"
                + "  \"y\": %d,"
                + "  \"width\": %d,"
                + "  \"height\": %d"
                + "}",
            widget.x(), widget.y(), widget.width(), widget.height());
    // when
    final ResultActions resultActions =
        mockMvc.perform(
            post("/widgets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body));
    // then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id", equalTo(widget.id())))
        .andExpect(jsonPath("x", equalTo(widget.x())))
        .andExpect(jsonPath("y", equalTo(widget.y())))
        .andExpect(jsonPath("z", equalTo(widget.z())))
        .andExpect(jsonPath("width", equalTo(widget.width())))
        .andExpect(jsonPath("height", equalTo(widget.height())))
        .andExpect(jsonPath("lastModified", equalTo(widget.lastModified().toEpochMilli())));
  }

  @Test
  void testGetOneWorks() throws Exception {
    // given
    final Widget widget = randomWidget(1);
    when(widgetService.readOne(widget.id())).thenReturn(new ServiceResponse<>(widget));
    // when
    final ResultActions resultActions =
        mockMvc.perform(get("/widgets/{id}", widget.id()).accept(MediaType.APPLICATION_JSON));
    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", equalTo(widget.id())))
        .andExpect(jsonPath("x", equalTo(widget.x())))
        .andExpect(jsonPath("y", equalTo(widget.y())))
        .andExpect(jsonPath("z", equalTo(widget.z())))
        .andExpect(jsonPath("width", equalTo(widget.width())))
        .andExpect(jsonPath("height", equalTo(widget.height())))
        .andExpect(jsonPath("lastModified", equalTo(widget.lastModified().toEpochMilli())));
  }

  @Test
  void testPaginationWorks() throws Exception {
    // given
    // given we mock call to service method
    // when it will be asked for page size 15 and starting from z=-2
    // it will return 15 widgets with z values from -2 to 12
    // here we generate 15 + 1
    final List<Widget> widgets =
        IntStream.range(-2, 14).mapToObj(WidgetUtil::randomWidget).collect(toUnmodifiableList());
    final Page<Widget> widgetPage = Page.of(widgets, 15);
    when(widgetService.readAll(15, -2)).thenReturn(new ServiceResponse<>(widgetPage));
    // when
    // when we request a page of size 15 and starting from z=-2
    final ResultActions resultActions =
        mockMvc.perform(
            get("/widgets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("fromZ", String.valueOf(-2))
                .queryParam("perPage", String.valueOf(15)));
    // then
    // then in response body we get 15 widgets with z values from -2 to 12
    // and Link header refers to the next page starting with z=13
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", equalTo(15)))
        .andExpect(jsonPath("$[0].z", equalTo(-2)))
        .andExpect(jsonPath("$[14].z", equalTo(12)))
        .andExpect(header().string("Link", containsString("fromZ=13")));
  }

  @Test
  void testPutWorks() throws Exception {
    // given
    final Widget widget = randomWidget(1);
    when(widgetService.updateOne(eq(widget.id()), any())).thenReturn(new ServiceResponse<>(widget));
    final String body =
        String.format(
            ""
                + "{"
                + "  \"x\": %d,"
                + "  \"y\": %d,"
                + "  \"width\": %d,"
                + "  \"height\": %d"
                + "}",
            widget.x(), widget.y(), widget.width(), widget.height());
    // when
    final ResultActions resultActions =
        mockMvc.perform(
            put("/widgets/{id}", widget.id())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body));
    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", equalTo(widget.id())))
        .andExpect(jsonPath("x", equalTo(widget.x())))
        .andExpect(jsonPath("y", equalTo(widget.y())))
        .andExpect(jsonPath("z", equalTo(widget.z())))
        .andExpect(jsonPath("width", equalTo(widget.width())))
        .andExpect(jsonPath("height", equalTo(widget.height())))
        .andExpect(jsonPath("lastModified", equalTo(widget.lastModified().toEpochMilli())));
  }

  @Test
  void testDeleteWorks() throws Exception {
    // given
    final Widget widget = randomWidget(1);
    when(widgetService.deleteOne(widget.id())).thenReturn(new ServiceResponse<>(widget));
    // when
    final ResultActions resultActions =
        mockMvc.perform(delete("/widgets/{id}", widget.id()).accept(MediaType.APPLICATION_JSON));
    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("id", equalTo(widget.id())))
        .andExpect(jsonPath("x", equalTo(widget.x())))
        .andExpect(jsonPath("y", equalTo(widget.y())))
        .andExpect(jsonPath("z", equalTo(widget.z())))
        .andExpect(jsonPath("width", equalTo(widget.width())))
        .andExpect(jsonPath("height", equalTo(widget.height())))
        .andExpect(jsonPath("lastModified", equalTo(widget.lastModified().toEpochMilli())));
  }

  @Test
  void testPostWithMissingFieldReturns400() throws Exception {
    mockMvc
        .perform(
            post("/widgets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"x\": 123}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Field y cannot be empty."));
  }

  @Test
  void testInvalidEndpointReturns404() throws Exception {
    mockMvc
        .perform(get("/invalid").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testInvalidMethodReturns405() throws Exception {
    mockMvc
        .perform(patch("/widgets").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void testRateLimitedNotConsumedReturns429() throws Exception {
    // given
    when(widgetService.readOne("1"))
        .thenThrow(new TooManyRequestsException(new RateLimitStat(false, 1000, 0, 999)));
    // when
    final ResultActions resultActions =
        mockMvc.perform(get("/widgets/{id}", "1").accept(MediaType.APPLICATION_JSON));
    // then
    resultActions
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string(X_REQUESTS_PER_MINUTE, equalTo("1000")))
        .andExpect(header().string(X_REQUESTS_AVAILABLE, equalTo("0")))
        .andExpect(header().string(X_NANOS_UNTIL_REFILL, equalTo("999")));
  }

  @Test
  void testRateLimitedConsumedReturnsHeaders() throws Exception {
    // given
    final Widget widget = randomWidget(1);
    final RateLimitStat rateLimitStat = new RateLimitStat(false, 1000, 0, 999);
    when(widgetService.deleteOne("1")).thenReturn(new ServiceResponse<>(widget, rateLimitStat));
    // when
    final ResultActions resultActions =
        mockMvc.perform(delete("/widgets/{id}", "1").accept(MediaType.APPLICATION_JSON));
    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(header().string(X_REQUESTS_PER_MINUTE, equalTo("1000")))
        .andExpect(header().string(X_REQUESTS_AVAILABLE, equalTo("0")))
        .andExpect(header().string(X_NANOS_UNTIL_REFILL, equalTo("999")));
  }
}
