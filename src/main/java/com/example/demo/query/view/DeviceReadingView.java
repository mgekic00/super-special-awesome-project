package com.example.demo.query.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
@Document(collection = "deviceReadings")
public class DeviceReadingView {
  @Id
  private String id;
  private String deviceId;
  @Indexed
  private String userId;
  @Indexed
  private LocalDateTime timestamp;
  private int glucoseValue;
  private String unit;

}