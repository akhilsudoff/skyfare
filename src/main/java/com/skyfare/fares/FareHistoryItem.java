package com.skyfare.fares;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import java.math.BigDecimal;

@DynamoDbBean
public class FareHistoryItem {
    private String watchId;
    private String observedAt;
    private BigDecimal price;
    private String currency;
    private String carrier;
    private Long expiresAt;

    @DynamoDbPartitionKey
    public String getWatchId() { return watchId; }
    public void setWatchId(String watchId) { this.watchId = watchId; }
    @DynamoDbSortKey
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = observedAt; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
