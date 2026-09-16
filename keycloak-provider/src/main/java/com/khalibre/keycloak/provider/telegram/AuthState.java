package com.khalibre.keycloak.provider.telegram;

import java.time.Instant;
import java.util.UUID;

public class AuthState {

    private final String id;
    private String telegramUserId;
    private String firstName;
    private String lastName;
    private String username;
    private String phoneNumber;
    private String status;
    private final long createdAt;
    private final long expiresAt;

    public AuthState(String telegramUserId, String firstName, String lastName,
                     String username, String phoneNumber) {
        this.id = UUID.randomUUID().toString();
        this.telegramUserId = telegramUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = "WAITING_SCAN";
        this.createdAt = Instant.now().getEpochSecond();
        this.expiresAt = createdAt + 300;
    }

    public AuthState(String id, String telegramUserId, String firstName, String lastName,
                     String username, String phoneNumber, String status) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.createdAt = Instant.now().getEpochSecond();
        this.expiresAt = createdAt + 300;
    }

    public String getId() { return id; }
    public String getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(String telegramUserId) { this.telegramUserId = telegramUserId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
}
