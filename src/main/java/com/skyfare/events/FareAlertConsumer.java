package com.skyfare.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@Profile("worker")
public class FareAlertConsumer {
    private static final Logger log = LoggerFactory.getLogger(FareAlertConsumer.class);
    private final SqsClient sqs;
    private final ObjectMapper mapper;
    private final FareEventPublisher publisher;
    private final FareAlertService alerts;

    public FareAlertConsumer(SqsClient sqs, ObjectMapper mapper, FareEventPublisher publisher, FareAlertService alerts) {
        this.sqs = sqs; this.mapper = mapper; this.publisher = publisher; this.alerts = alerts;
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 10000)
    public void drain() {
        String url = publisher.queueUrl();
        ReceiveMessageResponse response = sqs.receiveMessage(b -> b.queueUrl(url).maxNumberOfMessages(10).waitTimeSeconds(5));
        for (Message m : response.messages()) {
            try {
                FareChangedEvent event = mapper.readValue(m.body(), FareChangedEvent.class);
                alerts.handle(event);
                sqs.deleteMessage(b -> b.queueUrl(url).receiptHandle(m.receiptHandle()));
            } catch (Exception e) {
                log.error("Failed to process message {}; it will be retried", m.messageId(), e);
            }
        }
    }
}
