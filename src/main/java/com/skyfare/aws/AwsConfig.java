package com.skyfare.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import java.net.URI;

@Configuration
public class AwsConfig {
    @Bean
    public DynamoDbClient dynamoDbClient(AwsProps props) {
        DynamoDbClientBuilder b = DynamoDbClient.builder().region(Region.of(props.region()));
        if (StringUtils.hasText(props.endpoint())) b.endpointOverride(URI.create(props.endpoint()));
        return b.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Bean
    public SqsClient sqsClient(AwsProps props) {
        SqsClientBuilder b = SqsClient.builder().region(Region.of(props.region()));
        if (StringUtils.hasText(props.endpoint())) b.endpointOverride(URI.create(props.endpoint()));
        return b.build();
    }
}
