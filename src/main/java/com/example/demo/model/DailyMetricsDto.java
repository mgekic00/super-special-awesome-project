package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
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
public class DailyMetricsDto {
  @JsonProperty("day")
  String day;
  @JsonProperty("min")
  BigDecimal min;
  @JsonProperty("max")
  BigDecimal max;
  @JsonProperty("average")
  BigDecimal average;
}
