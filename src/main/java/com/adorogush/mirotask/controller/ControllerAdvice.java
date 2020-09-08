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
package com.adorogush.mirotask.controller;

import static com.adorogush.mirotask.controller.ResponseEntityUtil.responseEntity;

import com.adorogush.mirotask.exception.TooManyRequestsException;
import com.adorogush.mirotask.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** A centralized handler for exceptions. */
@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {
  private static final Logger log = LogManager.getLogger();
  private static final String SERVER_ERROR_MSG =
      "The server encountered an error when trying to fulfill the request.";

  /**
   * Best practice is to have immutable value classes that have all required validation in it's
   * constructor, throwing {@link IllegalArgumentException} or {@link IllegalStateException} or
   * using {@link java.util.Objects#requireNonNull}. <br>
   * When using such class as {@code @RequestBody} of MVC Controller method, any validation error
   * occurred, will by default be rendered by Spring in a non friendly way.<br>
   * This exception handler will detect such validation errors and redirect them to {@link
   * #statusCode(ResponseStatusException)} method for processing.
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final Optional<ValidationException> requestBodyDeserializationErrorMessage =
        Optional.of(ex)
            .map(Throwable::getCause)
            .filter(e -> e instanceof JsonProcessingException)
            .map(Throwable::getCause)
            .filter(
                e ->
                    e instanceof IllegalArgumentException
                        || e instanceof IllegalStateException
                        || e instanceof NullPointerException)
            .map(Throwable::getMessage)
            .map(ValidationException::new);
    if (requestBodyDeserializationErrorMessage.isPresent()) {
      return statusCode(requestBodyDeserializationErrorMessage.get());
    }
    return super.handleHttpMessageNotReadable(ex, headers, status, request);
  }

  @ExceptionHandler
  public ResponseEntity<Object> tooManyRequests(final TooManyRequestsException ex) {
    log.debug("Returning {} {}", ex.getStatus(), ex.getReason());
    return responseEntity(ex.getStatus(), ex.getReason(), ex.rateLimitStat());
  }

  @ExceptionHandler
  public ResponseEntity<Object> statusCode(final ResponseStatusException ex) {
    log.debug("Returning {} {}", ex.getStatus(), ex.getReason());
    return responseEntity(ex.getStatus(), ex.getReason());
  }

  @ExceptionHandler
  public ResponseEntity<Object> internalError(final Exception ex) {
    log.error("Returning 500", ex);
    return responseEntity(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR_MSG);
  }
}
