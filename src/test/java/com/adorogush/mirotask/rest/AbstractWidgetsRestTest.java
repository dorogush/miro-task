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
package com.adorogush.mirotask.rest;

import static com.adorogush.mirotask.WidgetUtil.randomWidgetToCreate;
import static com.adorogush.mirotask.WidgetUtil.widgetToUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.adorogush.mirotask.model.Widget;
import com.adorogush.mirotask.model.WidgetToCreate;
import com.adorogush.mirotask.model.WidgetToUpdate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/** Integration test. Starts the whole app and sends real rest requests. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
abstract class AbstractWidgetsRestTest {

  private final TestRestTemplate testRestTemplate;

  public AbstractWidgetsRestTest(
      final int localPort, final RestTemplateBuilder restTemplateBuilder) {
    testRestTemplate =
        new TestRestTemplate(
            restTemplateBuilder
                .rootUri("http://localhost:" + localPort)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));
  }

  @Test
  void basicScenario() {
    // create 3 widgets
    final Widget widget1 = create(1);
    final Widget widget2 = create((Integer) null);
    assertThat(widget2.z(), equalTo(2));
    final Widget widget3 = create((Integer) null);
    assertThat(widget3.z(), equalTo(3));

    // create one more widget, will shift others
    final Widget widget4 = create(2);
    assertThat(widget4.z(), equalTo(2));

    // read by id
    final Widget widget2Read = readOne(widget2.id());
    assertThat(widget2Read.z(), equalTo(3));
    final Widget widget3Read = readOne(widget3.id());
    assertThat(widget3Read.z(), equalTo(4));

    // create one more (5 total)
    final Widget widget5 = create(6);

    // request first page. perPage=2
    final Page page1 = readAll(2, null);
    assertThat(page1.nextPageUrl(), notNullValue());
    final List<Widget> widgetsFound1 = page1.widgets();
    assertThat(widgetsFound1.size(), equalTo(2));
    assertThat(widgetsFound1.get(0).id(), equalTo(widget1.id()));
    assertThat(widgetsFound1.get(0).z(), equalTo(1));
    assertThat(widgetsFound1.get(1).id(), equalTo(widget4.id()));
    assertThat(widgetsFound1.get(1).z(), equalTo(2));

    // request next page
    final Page page2 = readAll(null, page1.nextPageUrl());
    assertThat(page2.nextPageUrl(), notNullValue());
    final List<Widget> widgetsFound2 = page2.widgets();
    assertThat(widgetsFound2.size(), equalTo(2));
    assertThat(widgetsFound2.get(0).id(), equalTo(widget2.id()));
    assertThat(widgetsFound2.get(0).z(), equalTo(3));
    assertThat(widgetsFound2.get(1).id(), equalTo(widget3.id()));
    assertThat(widgetsFound2.get(1).z(), equalTo(4));

    // request next page
    final Page page3 = readAll(null, page2.nextPageUrl());
    assertThat(page3.nextPageUrl(), nullValue());
    final List<Widget> widgetsFound3 = page3.widgets();
    assertThat(widgetsFound3.size(), equalTo(1));
    assertThat(widgetsFound3.get(0).id(), equalTo(widget5.id()));
    assertThat(widgetsFound3.get(0).z(), equalTo(6));

    // state at this point:
    // z widget
    // 1 widget1
    // 2 widget4
    // 3 widget2
    // 4 widget3
    // 6 widget5

    // update widget2, set z=4
    // must move widget3 and widget5
    final Widget widget2Update = update(widget2.id(), widgetToUpdate(null, 4));
    assertThat(widget2Update.z(), equalTo(4));

    // delete widget4
    final Widget widget4Deleted = delete(widget4.id());
    assertThat(widget4Deleted.z(), equalTo(2));

    // read all again, perPage = 99
    final Page page4 = readAll(99, null);
    assertThat(page4.nextPageUrl(), nullValue());
    final List<Widget> widgetsFound4 = page4.widgets();
    assertThat(widgetsFound4.size(), equalTo(4));
    assertThat(widgetsFound4.get(0).id(), equalTo(widget1.id()));
    assertThat(widgetsFound4.get(0).z(), equalTo(1));
    assertThat(widgetsFound4.get(1).id(), equalTo(widget2.id()));
    assertThat(widgetsFound4.get(1).z(), equalTo(4));
    assertThat(widgetsFound4.get(2).id(), equalTo(widget3.id()));
    assertThat(widgetsFound4.get(2).z(), equalTo(5));
    assertThat(widgetsFound4.get(3).id(), equalTo(widget5.id()));
    assertThat(widgetsFound4.get(3).z(), equalTo(7));
  }

  private Widget create(final Integer z) {
    return create(randomWidgetToCreate(z));
  }

  private Widget create(final WidgetToCreate widgetToCreate) {
    final ResponseEntity<Widget> response =
        testRestTemplate.postForEntity("/widgets", widgetToCreate, Widget.class);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
    return response.getBody();
  }

  private Widget readOne(final String id) {
    final ResponseEntity<Widget> response =
        testRestTemplate.getForEntity("/widgets/{id}", Widget.class, id);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    return response.getBody();
  }

  private Page readAll(final Integer perPage, final String pageLink) {
    final ResponseEntity<List<Widget>> response;
    if (pageLink == null) {
      if (perPage == null) {
        response =
            testRestTemplate.exchange(
                "/widgets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Widget>>() {});
      } else {
        response =
            testRestTemplate.exchange(
                "/widgets?perPage={perPage}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Widget>>() {},
                perPage);
      }
    } else {
      response =
          testRestTemplate.exchange(
              pageLink, HttpMethod.GET, null, new ParameterizedTypeReference<List<Widget>>() {});
    }
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    final Optional<String> nextPageLink = getNextPageLink(response.getHeaders().getFirst("Link"));
    return new Page(response.getBody(), nextPageLink.orElse(null));
  }

  private Widget update(final String id, final WidgetToUpdate widgetToUpdate) {
    final ResponseEntity<Widget> response =
        testRestTemplate.exchange(
            "/widgets/{id}", HttpMethod.PUT, new HttpEntity<>(widgetToUpdate), Widget.class, id);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    return response.getBody();
  }

  private Widget delete(final String id) {
    final ResponseEntity<Widget> response =
        testRestTemplate.exchange("/widgets/{id}", HttpMethod.DELETE, null, Widget.class, id);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    return response.getBody();
  }

  private static class Page {
    private final List<Widget> widgets;
    private final String nextPageUrl;

    public Page(final List<Widget> widgets, final String nextPageUrl) {
      this.widgets = widgets;
      this.nextPageUrl = nextPageUrl;
    }

    public List<Widget> widgets() {
      return widgets;
    }

    public String nextPageUrl() {
      return nextPageUrl;
    }
  }

  private static Optional<String> getNextPageLink(final String link) {
    if (link != null && !link.isBlank()) {
      final Pattern pattern = Pattern.compile("<([^>]+)>; rel=\"next\"");
      final Matcher matcher = pattern.matcher(link);
      if (matcher.matches()) {
        return Optional.of(matcher.group(1));
      }
    }
    return Optional.empty();
  }
}
