package com.example.demo.view;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
@Document(collection = "devices")
public class DeviceView {
  @Id
  private String id;
  private String userId;
  private String type;
  private String manufacturer;
  private String model;
  private String serialNumber;

}