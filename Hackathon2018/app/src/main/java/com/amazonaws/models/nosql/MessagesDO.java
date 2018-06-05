package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "hackathon-mobilehub-342417472-messages")

public class MessagesDO {
    private String _id;
    private String _message;
    private String _movie;
    private String _receiver;
    private String _sender;
    private String _senderName;
    private String _userId;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAttribute(attributeName = "id")
    public String getId() {
        return _id;
    }

    public void setId(final String _id) {
        this._id = _id;
    }
    @DynamoDBAttribute(attributeName = "message")
    public String getMessage() {
        return _message;
    }

    public void setMessage(final String _message) {
        this._message = _message;
    }
    @DynamoDBAttribute(attributeName = "movie")
    public String getMovie() {
        return _movie;
    }

    public void setMovie(final String _movie) {
        this._movie = _movie;
    }
    @DynamoDBAttribute(attributeName = "receiver")
    public String getReceiver() {
        return _receiver;
    }

    public void setReceiver(final String _receiver) {
        this._receiver = _receiver;
    }
    @DynamoDBAttribute(attributeName = "sender")
    public String getSender() {
        return _sender;
    }

    public void setSender(final String _sender) {
        this._sender = _sender;
    }
    @DynamoDBAttribute(attributeName = "senderName")
    public String getSenderName() {
        return _senderName;
    }

    public void setSenderName(final String _senderName) {
        this._senderName = _senderName;
    }
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

}
