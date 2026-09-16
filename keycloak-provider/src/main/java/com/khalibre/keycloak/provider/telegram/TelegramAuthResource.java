package com.khalibre.keycloak.provider.telegram;

import static com.khalibre.keycloak.provider.telegram.TelegramIdentityProvider.TELEGRAM_BOT_TOKEN_KEY;
import static com.khalibre.keycloak.provider.telegram.TelegramIdentityProvider.TELEGRAM_BOT_USERNAME_KEY;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * JAX-RS resource exposed by the Telegram identity provider.
 *
 * <p>Generates a Telegram bot start deeplink URL for the configured bot.
 * The QR code is rendered client-side using qr-code-styling.
 *
 * <p>Endpoints (mounted at /realms/{realm}/telegram-auth/...):
 * <ul>
 *   <li>GET /{alias} - QR code HTML page</li>
 *   <li>GET /{alias}/deeplink - Deeplink URL JSON</li>
 * </ul>
 */
public class TelegramAuthResource implements RealmResourceProvider {

  private final KeycloakSession session;

  public TelegramAuthResource(KeycloakSession session) {
    this.session = session;
  }

  @GET
  @Path("{alias}/qr")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQrCode(@PathParam("alias") String alias) {
    IdentityProviderModel identityProvider = getIdentityProvider(alias);
    if (identityProvider == null) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(Map.of("error", "Telegram bot not configured",
          "alias", alias == null ? "" : alias))
        .build();
    }

    String botToken = getConfigValue(identityProvider, TELEGRAM_BOT_TOKEN_KEY);
    String botUsername = getConfigValue(identityProvider, TELEGRAM_BOT_USERNAME_KEY);
    if (botToken == null || botUsername == null) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(Map.of("error", "Telegram bot not configured"))
        .build();
    }

    AuthState authState = AuthStateCache.createEmpty();
    String deepLink = getDeepLink(botUsername, authState.getId());

    Map<String, Object> result = new HashMap<>();
    result.put("authStateId", authState.getId());
    result.put("deepLink", deepLink);
    result.put("botUsername", botUsername);

    return Response.ok(result).build();
  }

  public String getDeepLink(String botUsername, String authStateId) {
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

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
    // No resources to close
  }
}