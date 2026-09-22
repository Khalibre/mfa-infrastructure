package com.khalibre.keycloak.provider.telegram;

import static com.khalibre.keycloak.provider.telegram.TelegramIdentityProvider.TELEGRAM_BOT_TOKEN_KEY;
import static com.khalibre.keycloak.provider.telegram.TelegramIdentityProvider.TELEGRAM_BOT_USERNAME_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

/**
 * JAX-RS resource exposed by the Telegram identity provider.
 *
 * <p>Generates a Telegram bot start deeplink URL for the configured bot.
 * The QR code is rendered client-side using qr-code-styling.
 *
 * <p>Endpoints (mounted at /realms/{realm}/telegram-auth/...):
 * <ul>
 *   <li>POST /{alias}/init - Initialize bot (start polling or set webhook)</li>
 *   <li>GET /{alias}/qr - QR code data (auth state ID + deeplink)</li>
 *   <li>POST /{alias}/webhook - Telegram update receiver (webhook mode)</li>
 * </ul>
 */
public class TelegramAuthResource implements RealmResourceProvider {

  private final KeycloakSession session;
  private final ObjectMapper objectMapper;
  public static final String KEY_AUTH_STATE_ID = "authStateId";

  @Context
  private UriInfo uriInfo;

  public TelegramAuthResource(KeycloakSession session) {
    this.session = session;
    this.objectMapper = new ObjectMapper();
  }

  @GET
  @Path("{alias}/qr")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQrCode(@PathParam("alias") String alias) {
    IdentityProviderModel identityProvider = getIdentityProvider(alias);
    if (identityProvider == null) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(Map.of("error", "Telegram bot not configured",
          "alias", alias))
        .build();
    }

    String botUsername = getConfigValue(identityProvider, TELEGRAM_BOT_USERNAME_KEY);
    if (botUsername == null) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(Map.of("error", "Telegram bot not configured"))
        .build();
    }
    AuthState authState = AuthStateSession.create(session, getAuthSessionId());
    String deepLink = getDeepLink(botUsername, authState.getId());

    Map<String, Object> result = new HashMap<>();
    result.put("authStateId", authState.getId());
    result.put("deepLink", deepLink);
    result.put("botUsername", botUsername);

    return Response.ok(result).build();
  }

  private String getAuthSessionId() {
    RootAuthenticationSessionModel authSession = getAuthSession();
    if (authSession == null) {
      return null;
    }
    return authSession.getId();
  }

  private RootAuthenticationSessionModel getAuthSession() {
    AuthenticationSessionManager authSessionManager = new AuthenticationSessionManager(session);
    return authSessionManager.getCurrentRootAuthenticationSession(session.getContext().getRealm());
  }

  private String getDeepLink(String botUsername, String authStateId) {
    return "https://t.me/" + botUsername + "?start=login_" + authStateId;
  }

  private IdentityProviderModel getIdentityProvider(String alias) {
    try {
      if (session == null || session.getContext() == null
        || session.getContext().getRealm() == null) {
        return null;
      }

      if (alias != null && !alias.trim().isEmpty()) {
        try {
          IdentityProviderModel config = session.identityProviders().getByAlias(alias.trim());
          if (config != null && TelegramIdentityProviderFactory.PROVIDER_ID.equals(
            config.getProviderId())) {
            return config;
          }
        } catch (Exception ignored) {
          // Provider with this alias not found, fall through
        }
      }
    } catch (Exception ignored) {
      // Session not available
    }
    return null;
  }

  @Nullable
  private static String getConfigValue(IdentityProviderModel config, String key) {
    String username = config.getConfig().get(key);
    if (username != null && !username.trim().isEmpty()) {
      return username.trim();
    }
    return null;
  }

  @POST
  @Path("{alias}/init")
  @Produces(MediaType.APPLICATION_JSON)
  public Response initBot(@PathParam("alias") String alias) {
    IdentityProviderModel identityProvider = getIdentityProvider(alias);
    if (identityProvider == null) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(Map.of("ok", false,
          "error", "Telegram bot not configured",
          "alias", alias))
        .build();
    }

    String botToken = getConfigValue(identityProvider, TELEGRAM_BOT_TOKEN_KEY);
    if (botToken == null) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(Map.of("ok", false,
          "error", "Telegram bot not configured",
          "alias", alias))
        .build();
    }

    TelegramBotManager manager = TelegramBotManager.getInstance();

    if (manager.getMode() == TelegramBotMode.POLLING) {
      manager.startPolling(alias, botToken);
      return Response.ok(Map.of("ok", true,
          "mode", "polling",
          "alias", alias))
        .build();
    } else {
      String realmName = session.getContext().getRealm().getName();
      String webhookUrl = uriInfo.getBaseUriBuilder()
        .path("realms")
        .path(realmName)
        .path("telegram-auth")
        .path(alias)
        .path("webhook")
        .build()
        .toString();
      manager.setWebhook(alias, botToken, webhookUrl);
      return Response.ok(Map.of("ok", true,
          "mode", "webhook",
          "alias", alias,
          "webhookUrl", webhookUrl))
        .build();
    }
  }

  @POST
  @Path("{alias}/webhook")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleWebhook(@PathParam("alias") String alias, String payload) {
    try {
      IdentityProviderModel identityProvider = getIdentityProvider(alias);
      if (identityProvider == null) {
        return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("ok", false,
            "error", "Telegram bot not configured",
            "alias", alias))
          .build();
      }

      String botToken = getConfigValue(identityProvider, TELEGRAM_BOT_TOKEN_KEY);
      if (botToken == null) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(Map.of("ok", false,
            "error", "Telegram bot not configured",
            "alias", alias))
          .build();
      }

      TelegramWebhookPayload update = objectMapper.readValue(payload,
        TelegramWebhookPayload.class);
      TelegramUpdateHandler handler = new TelegramUpdateHandler(botToken);
      handler.handleUpdate(update);

      return Response.ok(Map.of("ok", true)).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(Map.of("ok", false, "error", e.getMessage())).build();
    }
  }

  @GET
  @Path("status")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatus() {
    AuthState state = AuthStateSession.get(session, getAuthSessionId());
    if (state == null) {
      return Response.status(Status.NOT_FOUND)
        .entity(Map.of("status", "EXPIRED"))
        .build();
    }

    Map<String, Object> result = new HashMap<>();
    result.put("status", state.getStatus());
    result.put("authStateId", state.getId());
    return Response.ok(result).build();
  }

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
    // No resources to close
  }
}