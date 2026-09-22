package com.khalibre.keycloak.provider.telegram;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuthStateCache {

  private static final ConcurrentHashMap<String, AuthState> cache = new ConcurrentHashMap<>();
  private static final ScheduledExecutorService scheduler =
    Executors.newSingleThreadScheduledExecutor();

  static {
    scheduler.scheduleAtFixedRate(() -> {
      long now = Instant.now().getEpochSecond();
      cache.entrySet().removeIf(entry ->
        entry.getValue().getExpiresAt() < now);
    }, 30, 30, TimeUnit.SECONDS);
  }

  public static AuthState createEmpty() {
    AuthState state = new AuthState(null, null, null, null, null);
    cache.put(state.getId(), state);
    return state;
  }

  public static AuthState create(String telegramUserId, String firstName,
    String lastName, String username, String phoneNumber) {
    AuthState state = new AuthState(telegramUserId, firstName, lastName, username, phoneNumber);
    cache.put(state.getId(), state);
    return state;
  }

  public static void store(String id, AuthState state) {
    cache.put(id, state);
  }

  public static AuthState get(String id) {
    if (id == null) {
      return null;
    }
    AuthState state = cache.get(id);
    if (state == null) {
      return null;
    }
    if (state.getExpiresAt() < Instant.now().getEpochSecond()) {
      cache.remove(id);
      return null;
    }
    return state;
  }

  public static AuthState findByTelegramUserId(String telegramUserId) {
    if (telegramUserId == null) {
      return null;
    }
    for (AuthState state : cache.values()) {
      if (telegramUserId.equals(state.getTelegramUserId())) {
        return state;
      }
    }
    return null;
  }

  public static void remove(String id) {
    cache.remove(id);
  }

  public static void shutdown() {
    scheduler.shutdownNow();
  }
}
