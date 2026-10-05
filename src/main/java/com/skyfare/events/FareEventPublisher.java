package com.skyfare.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyfare.aws.AwsProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class FareEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(FareEventPublisher.class);
    private final SqsClient sqs;
    private final ObjectMapper mapper;
    private final AwsProps props;
    private volatile String queueUrl;

    public FareEventPublisher(SqsClient sqs, ObjectMapper mapper, AwsProps props) {
        this.sqs = sqs; this.mapper = mapper; this.props = props;
    }

    public void publish(FareChangedEvent event) {
        try {
            String body = mapper.writeValueAsString(event);
            sqs.sendMessage(b -> b.queueUrl(queueUrl()).messageBody(body));
            log.debug("Published fare event for watch {}", event.watchId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise fare event", e);
        }
    }

    String queueUrl() {
        if (queueUrl == null) queueUrl = sqs.getQueueUrl(b -> b.queueName(props.sqsQueue())).queueUrl();
        return queueUrl;
    }
}
