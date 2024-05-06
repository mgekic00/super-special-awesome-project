package com.example.demo.query.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
@Document(collection = "users")
public class UserView {
  @Id
  private String id;
  private String firstName;
  private String lastName;
  private LocalDate dateOfBirth;
  private String email;
  private String phoneNumber;

}