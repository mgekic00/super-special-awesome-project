package com.example.demo.controller;

import com.example.demo.model.CalendarDto;
import com.example.demo.model.DailyMetricsDto;
import com.example.demo.query.view.UserView;
import com.example.demo.service.GraphService;
import com.example.demo.service.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
  public Mono<List<DailyMetricsDto>> getGraphByUserIdAndDates(@PathVariable String userId,
      @RequestParam(value = "startDate", required = false) String startDate,
      @RequestParam(value = "endDate", required = false) String endDate) {
    //Default timespan is the last 2 weeks, but any time span in the last 90 days
    //can be selected.//TODO validate
    return graphService.calculateMetrics(userId, startDate, endDate);
  }

  @GetMapping("calendar/{userId}")
  public Mono<List<CalendarDto>> constructCalendar(@PathVariable String userId) {
    return graphService.constructCalendar(userId);
  }

  private void validateDates(String startDate, String endDate) {

  }

  public static List<CalendarDto> mapToCalendarDtoList(Map<LocalDate, Map<String, Integer>> map) {
    return map.entrySet().stream()
        .map(entry -> {
          LocalDate date = entry.getKey();
          List<CalendarDto.CalendarEntry> entries = entry.getValue().entrySet().stream()
              .map(deviceEntry -> CalendarDto.CalendarEntry.builder()
                  .deviceId(deviceEntry.getKey())
                  .numOfReadings(deviceEntry.getValue())
                  .build())
              .collect(Collectors.toList());
          return CalendarDto.builder()
              .date(date)
              .entries(entries)
              .build();
        })
        .collect(Collectors.toList());
  }




}
