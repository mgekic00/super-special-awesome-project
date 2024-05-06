package com.example.demo.service;

import com.example.demo.model.DailyMetricsDto;
import com.example.demo.query.view.DeviceReadingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CalendarService {

  private final ReactiveMongoTemplate mongoTemplate;

  public CalendarService(ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Mono<List<DailyMetricsDto>> calculateMetrics(String userId, String startDate, String endDate) {
    return findByUserIdAndTimestampBetween(userId, normalizeDateTime(startDate), normalizeDateTime(endDate))
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

  public Flux<DeviceReadingView> findByUserIdAndTimestampBetween(String userId, Instant startDate, Instant endDate) {
    log.info("Querying for user {}, beginDate: {}, endDate: {}",userId,startDate,endDate);
    Query query = new Query();
    query.addCriteria(Criteria.where("userId").is(userId)
        .and("timestamp").gte(startDate.toString()).lte(endDate.toString()));
    return mongoTemplate.find(query, DeviceReadingView.class, "deviceReadings");
  }

  private Instant normalizeDateTime(String dateString ) {
    LocalDateTime localDateTime = LocalDateTime.parse(dateString);
    return localDateTime.toInstant(ZoneOffset.UTC);
  }


}
