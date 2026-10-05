package com.skyfare.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@ConditionalOnProperty(prefix = "skyfare.aws", name = "create-resources", havingValue = "true")
public class AwsResourceInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AwsResourceInitializer.class);
    private final DynamoDbClient dynamo;
    private final SqsClient sqs;
    private final AwsProps props;

    public AwsResourceInitializer(DynamoDbClient dynamo, SqsClient sqs, AwsProps props) {
        this.dynamo = dynamo; this.sqs = sqs; this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureTable();
        ensureQueue();
    }

    private void ensureTable() {
        String table = props.dynamodbTable();
        try {
            dynamo.describeTable(b -> b.tableName(table));
            log.info("DynamoDB table {} already exists", table);
        } catch (ResourceNotFoundException e) {
            log.info("Creating DynamoDB table {}", table);
            try {
                dynamo.createTable(b -> b.tableName(table)
                        .attributeDefinitions(
                                AttributeDefinition.builder().attributeName("watchId").attributeType(ScalarAttributeType.S).build(),
                                AttributeDefinition.builder().attributeName("observedAt").attributeType(ScalarAttributeType.S).build())
                        .keySchema(
                                KeySchemaElement.builder().attributeName("watchId").keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder().attributeName("observedAt").keyType(KeyType.RANGE).build())
                        .billingMode(BillingMode.PAY_PER_REQUEST));
                dynamo.waiter().waitUntilTableExists(b -> b.tableName(table));
            } catch (ResourceInUseException race) {
                log.info("Table {} was created concurrently by another instance", table);
            }
        }
        try {
            dynamo.updateTimeToLive(b -> b.tableName(table)
                    .timeToLiveSpecification(s -> s.attributeName("expiresAt").enabled(true)));
        } catch (DynamoDbException e) {
            log.debug("TTL already configured on {}: {}", table, e.getMessage());
        }
    }

    private void ensureQueue() {
        sqs.createQueue(b -> b.queueName(props.sqsQueue()));
        log.info("SQS queue {} ready", props.sqsQueue());
    }
}
