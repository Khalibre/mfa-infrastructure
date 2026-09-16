package com.khalibre.keycloak.provider.telegram;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
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
 *   <li>GET /{alias}/deeplink - Deeplink URL JSON</li>
 * </ul>
 */
public class TelegramAuthResource implements RealmResourceProvider {

  private final KeycloakSession session;

  public TelegramAuthResource(KeycloakSession session) {
    this.session = session;
  }

  /**
   * GET /{alias}/deeplink - Returns the Telegram deeplink URL as JSON.
   *
   * <p>The alias path param selects which Telegram identity provider
   * configuration to use.
   */
  @GET
  @Path("{alias}/deeplink")
  public Response getDeeplink(
    @PathParam("alias") String alias) {

    String botUsername = resolveBotUsername(alias);

    if (botUsername == null) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("{\"error\":\"telegram_not_configured\", \"alias\":\"" + escapeJson(
          alias == null ? "" : alias) + "\"}")
        .type("application/json")
        .build();
    }

    String url = "https://t.me/" + botUsername;
    return Response.ok()
      .entity(
        "{\"deeplink\":\"" + escapeJson(url) + "\",\"bot\":\"" + escapeJson(botUsername) + "\"}")
      .type("application/json")
      .build();
  }

  /**
   * Resolve the bot username from the given alias, or fall back to any
   * configured telegram provider.
   */
  private String resolveBotUsername(String alias) {
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
            String username = config.getConfig()
              .get(TelegramIdentityProvider.TELEGRAM_BOT_USERNAME_KEY);
            if (username != null && !username.trim().isEmpty()) {
              return username.trim();
            }
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

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
    // No resources to close
  }

  private static String escapeJson(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}