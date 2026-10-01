package com.khalibre.keycloak.provider.account;

import java.util.Map;

/**
 * A single user event rendered by the "Account activities" page of the account console.
 *
 * <p>Only a small, allow-listed subset of {@link org.keycloak.events.Event#getDetails()} is
 * exposed: event details are free-form and may carry administrative data (token ids, raw
 * claims, impersonation info) that must never be rendered in a self-service page.
 */
public class AccountActivity {

  public long time;
  public String type;
  public String clientId;
  public String ipAddress;
  public String error;
  public Map<String, String> details;

  public AccountActivity() {
  }

  public AccountActivity(
      long time,
      String type,
      String clientId,
      String ipAddress,
      String error,
      Map<String, String> details) {
    this.time = time;
    this.type = type;
    this.clientId = clientId;
    this.ipAddress = ipAddress;
    this.error = error;
    this.details = details;
  }
}
