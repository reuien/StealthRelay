package com.example.gateway.session;

import usrs.DataConsumer;
import usrs.DataOwnerClient;

import java.util.Date;

public class UserSession {
    private final String token;
    private final String number;   // 账号
    private final String usrName;  // 用户名
    private final String role;     // "owner" / "consumer"
    private volatile long lastAccess;

    private volatile DataOwnerClient ownerClient;
    private volatile DataConsumer consumer;

    public UserSession(String token, String number, String usrName, String role) {
        this.token = token;
        this.number = number;
        this.usrName = usrName;
        this.role = role;
        this.lastAccess = System.currentTimeMillis();
    }

    public void touch() {
        this.lastAccess = System.currentTimeMillis();
    }

    public String getToken() { return token; }
    public String getNumber() { return number; }
    public String getUsrName() { return usrName; }
    public String getRole() { return role; }
    public long getLastAccess() { return lastAccess; }
    public Date getLastAccessDate() { return new Date(lastAccess); }

    public DataOwnerClient getOwnerClient() { return ownerClient; }
    public void setOwnerClient(DataOwnerClient ownerClient) { this.ownerClient = ownerClient; }

    public DataConsumer getConsumer() { return consumer; }
    public void setConsumer(DataConsumer consumer) { this.consumer = consumer; }
}
