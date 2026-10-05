package com.skyfare.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skyfare.aws")
public record AwsProps(String region, String endpoint, boolean createResources,
                       String dynamodbTable, String sqsQueue) { }
