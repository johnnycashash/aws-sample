package com.example.demo.repositories;

import com.example.demo.model.AwsService;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@EnableScan
public interface AwsServiceRepository extends CrudRepository<AwsService, String> {
    Optional<List<AwsService>> findByServiceName(String serviceName);
}