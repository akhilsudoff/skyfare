package com.skyfare.fares;

import com.skyfare.aws.AwsProps;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import java.util.List;
import java.util.UUID;

@Repository
public class FareHistoryRepository {
    private final DynamoDbTable<FareHistoryItem> table;

    public FareHistoryRepository(DynamoDbEnhancedClient client, AwsProps props) {
        this.table = client.table(props.dynamodbTable(), TableSchema.fromBean(FareHistoryItem.class));
    }

    public void save(FareHistoryItem item) { table.putItem(item); }

    public List<FareHistoryItem> latest(UUID watchId, int limit) {
        QueryConditional byWatch = QueryConditional.keyEqualTo(Key.builder().partitionValue(watchId.toString()).build());
        return table.query(r -> r.queryConditional(byWatch).scanIndexForward(false).limit(limit))
                .items().stream().limit(limit).toList();
    }
}
