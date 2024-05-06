package com.example.demo.service;

import com.example.demo.model.CalendarDto;
import com.example.demo.model.DailyMetricsDto;
import com.example.demo.query.view.DeviceReadingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GraphService {

  private final ReactiveMongoTemplate mongoTemplate;

  public GraphService(ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Mono<List<DailyMetricsDto>> calculateMetrics(String userId, String startDate, String endDate) {
    final var dates = resolveDatesForMetrics(startDate, endDate);
    return findByUserIdAndTimestampBetween(userId, dates.getFirst(), dates.getSecond())
        .collectList()
        .flatMap(list -> {
          Map<LocalDate, List<DeviceReadingView>> groupedByDay = list.stream()
              .collect(Collectors.groupingBy(reading -> reading.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate()));

          List<DailyMetricsDto> metricsDtoList = groupedByDay.entrySet().stream()
              .map(entry -> {
                List<Integer> glucoseValues = entry.getValue().stream()
                    .map(DeviceReadingView::getGlucoseValue).toList();

                final var min = glucoseValues.stream().map(BigDecimal::valueOf).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                final var max = glucoseValues.stream().map(BigDecimal::valueOf).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                double average = glucoseValues.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                final var avgBigDecimal = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
                return new DailyMetricsDto(entry.getKey().toString(), min, max, avgBigDecimal);
              })
              .collect(Collectors.toList());
          return Mono.just(metricsDtoList);
        });
  }

  public Mono<List<CalendarDto>> constructCalendar(String userId) {
    final var dates = calculateDates(30);
    return fetchDataAndCollectToMap(dates, userId)
        .transform(withDataProcessingAndConstructResult());
  }

  private Mono<Map<LocalDate, Collection<String>>> fetchDataAndCollectToMap(Pair<String,String> dates, String userId) {
    return findByUserIdAndTimestampBetween(userId, dates.getFirst(), dates.getSecond())
        .collectMultimap(deviceReadingView -> {
          LocalDateTime timestamp = deviceReadingView.getTimestamp();
          ZonedDateTime zonedDateTime = timestamp.atZone(ZoneId.of("UTC"));
          return zonedDateTime.toLocalDate();
        }, DeviceReadingView::getDeviceId);
  }

  Function<Mono<Map<LocalDate,Collection<String>>>,Mono<List<CalendarDto>>> withDataProcessingAndConstructResult() {
    return upstream -> upstream
        .map(map -> {
          final List<CalendarDto> result = new ArrayList<>();
          map.forEach((date, deviceIds) -> {
            Map<String, Integer> deviceIdCountMap = new HashMap<>();
            deviceIds.forEach(deviceId -> deviceIdCountMap.merge(deviceId, 1, Integer::sum));

            List<CalendarDto.CalendarEntry> entries = deviceIdCountMap.entrySet().stream()
                .map(entry -> new CalendarDto.CalendarEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            result.add(new CalendarDto(date, entries));
          });
          return result;
        });
  }

  //calculates begin and end dates for the provided number of days
  private Pair<String,String> calculateDates(int numOfDays) {
    LocalDate currentDate = LocalDate.now();

    LocalDate beginDate = currentDate.minusDays(numOfDays);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00'Z'");
    String formattedBeginDate = beginDate.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter);
    String formattedEndDate = currentDate.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter);


    return Pair.of(formattedBeginDate,formattedEndDate);
  }
  public Flux<DeviceReadingView> findByUserIdAndTimestampBetween(String userId, Instant startDate, Instant endDate) {
    log.info("Querying for user {}, beginDate: {}, endDate: {}",userId,startDate,endDate);
    Query query = new Query();
    query.addCriteria(Criteria.where("userId").is(userId)
        .and("timestamp").gte(startDate.toString()).lte(endDate.toString()));
    return mongoTemplate.find(query, DeviceReadingView.class, "deviceReadings");
  }

  private Pair<String,String> resolveDatesForMetrics(String startDate, String endDate) {
    String start = null;
    String end = null;
    if(Objects.isNull(startDate)) {
      final var dates = calculateDates(14);
      start = dates.getFirst();
      end = dates.getSecond();
    } else {
      start = normalizeDateTime(startDate).toString();
      end = normalizeDateTime(endDate).toString();
    }
    return Pair.of(start, end);
  }

  public Flux<DeviceReadingView> findByUserIdAndTimestampBetween(String userId, String startDate, String endDate) {
    log.info("Querying for user {}, beginDate: {}, endDate: {}",userId,startDate,endDate);
    Query query = new Query();
    query.addCriteria(Criteria.where("userId").is(userId)
        .and("timestamp").gte(startDate).lte(endDate));
    return mongoTemplate.find(query, DeviceReadingView.class, "deviceReadings");
  }


  private Instant normalizeDateTime(String dateString ) {
    LocalDateTime localDateTime = LocalDateTime.parse(dateString);
    return localDateTime.toInstant(ZoneOffset.UTC);
  }


}
