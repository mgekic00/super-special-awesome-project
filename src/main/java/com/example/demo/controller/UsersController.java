package com.example.demo.controller;

import com.example.demo.exception.RequestValidationException;
import com.example.demo.exception.RequestValidationException.ValidationError;
import com.example.demo.model.CalendarDto;
import com.example.demo.model.DailyMetricsDto;
import com.example.demo.model.RestApiResponse;
import com.example.demo.query.view.UserView;
import com.example.demo.service.GraphService;
import com.example.demo.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {

  private final UserService userService;
  private final GraphService graphService;

  public UsersController(UserService userService, GraphService graphService) {
    this.userService = userService;
    this.graphService = graphService;
  }

  @GetMapping("/{userId}")
  public Mono<UserView> getUserById(@PathVariable String userId) {
    log.info("Fetching user {}",userId);
    return userService.getUserById(userId);
  }

  @GetMapping("graph/{userId}")
  public Mono<ResponseEntity<RestApiResponse<List<DailyMetricsDto>>>> getGraphByUserIdAndDates(@PathVariable String userId,
      @RequestParam(value = "startDate", required = false) String startDate,
      @RequestParam(value = "endDate", required = false) String endDate) {

    validateDates(startDate, endDate);
    return graphService.calculateMetrics(userId, startDate, endDate)
        .map(data -> RestApiResponse.<List<DailyMetricsDto>>builder().data(data).build())
        .map(ResponseEntity::ok)
        .doOnError(e -> log.info("Error occurred while obtaining graph data for user {}",userId, e));
  }

  @GetMapping("calendar/{userId}")
  public Mono<ResponseEntity<RestApiResponse<List<CalendarDto>>>> constructCalendar(@PathVariable String userId) {
    return graphService.constructCalendar(userId)
        .map(data -> RestApiResponse.<List<CalendarDto>>builder().data(data).build())
        .map(ResponseEntity::ok)
        .doOnError(e -> log.info("Error occurred while obtaining calendar data for user {}",userId, e));
  }

  private void validateDates(String startDate, String endDate) {
    Map<String, ValidationError> errorMap = new HashMap<>();

    Optional.ofNullable(startDate)
        .ifPresentOrElse(startDt -> {
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

              // Parse the string to LocalDateTime
              LocalDateTime dateTime = LocalDateTime.parse(startDt, formatter);
              Instant start = dateTime.atZone(ZoneId.of("UTC")).toInstant();
              Instant now = Instant.now();
              if(Duration.between(start, now).toDays() >= 90) {
                errorMap.put("The provided time span is older than 90 days",ValidationError.INVALID_FIELD_VALUE);
              }

              if (Objects.isNull(endDate)) {
            errorMap.put("Start date is provided, but no end date",ValidationError.FIELD_NULL);
          }
        },
            ()-> Optional.ofNullable(endDate)
            .ifPresent(endDt -> errorMap.put("End date is provided, but no start date",ValidationError.FIELD_NULL))
        );

    if (!errorMap.isEmpty()) {
      throw new RequestValidationException(errorMap);
    }

  }

  @ExceptionHandler({RequestValidationException.class})
  protected Mono<ResponseEntity<RestApiResponse<Map<String, ValidationError>>>> handleBadRequest(RequestValidationException e) {
    return Mono.just(ResponseEntity
        .badRequest()
        .body(RestApiResponse.<Map<String, ValidationError>>builder().errorResponse(e.getErrorToMessageMap())
            .build()));
  }


}
