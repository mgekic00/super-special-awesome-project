package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
@Builder(builderClassName = "Builder", toBuilder = true)
@EqualsAndHashCode
public class CalendarDto {
  @JsonProperty("date")
  LocalDate date;
  @JsonProperty("entries")
  List<CalendarEntry> entries;

  @Value
  @Jacksonized
  @AllArgsConstructor
  @Accessors(fluent = true, chain = false)
  @lombok.Builder(builderClassName = "Builder", toBuilder = true)
  @EqualsAndHashCode
  public static class CalendarEntry {
    @JsonProperty("deviceId")
    String deviceId;
    @JsonProperty("numOfReadings")
    int numOfReadings;
  }

}
