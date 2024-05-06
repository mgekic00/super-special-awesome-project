package com.example.demo.service;

import com.example.demo.query.view.UserView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

  private final ReactiveMongoTemplate mongoTemplate;

  public UserService(ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Mono<UserView> getUserById(String userId) {
    return mongoTemplate.findById(userId, UserView.class,"users")
        .doOnSuccess(result -> log.info(String.valueOf(result)));
  }

}

