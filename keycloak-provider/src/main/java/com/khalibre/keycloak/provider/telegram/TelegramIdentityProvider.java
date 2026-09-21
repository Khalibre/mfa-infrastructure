package com.khalibre.keycloak.provider.telegram;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

public class TelegramIdentityProvider extends AbstractIdentityProvider<IdentityProviderModel> {

  public static final String TELEGRAM_BOT_USERNAME_KEY = "telegram_bot_username";
  public static final String TELEGRAM_BOT_TOKEN_KEY = "telegram_bot_token";

  public TelegramIdentityProvider(KeycloakSession session, IdentityProviderModel config) {
    super(session, config);
  }

  public String getBotUsername() {
    return getConfig().getConfig().get(TELEGRAM_BOT_USERNAME_KEY);
  }

  public String getBotToken() {
    return getConfig().getConfig().get(TELEGRAM_BOT_TOKEN_KEY);
  }

  @Override
  public Response retrieveToken(KeycloakSession keycloakSession,
    FederatedIdentityModel federatedIdentityModel) {
    return null;
  }

  @Override
  public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm,
    BrokeredIdentityContext context) {
  }

  @Override
  public void authenticationFinished(AuthenticationSessionModel authSession,
    BrokeredIdentityContext context) {
  }

  @Override
  public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
    BrokeredIdentityContext context) {
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
    BrokeredIdentityContext context) {
  }

  @Override
  public void backchannelLogout(KeycloakSession session, UserSessionModel userSession,
    UriInfo uriInfo, RealmModel realm) {
  }

  @Override
  public Response keycloakInitiatedBrowserLogout(KeycloakSession session,
    UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
    return null;
  }

  @Override
  public Response export(UriInfo uriInfo, RealmModel realm, String subject) {
    return null;
  }

  @Override
  public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
    return null;
  }

  @Override
  public Response performLogin(AuthenticationRequest request) {
    try {
      final UriBuilder uriBuilder = UriBuilder.fromUri(request.getRedirectUri());
      uriBuilder.queryParam("state", request.getState().getEncoded());
      URI callbackUrl = uriBuilder.build();

      if (hasTelegramQrData(request.getAuthenticationSession())) {
        return Response.temporaryRedirect(callbackUrl).build();
      }

      return renderPage(request, callbackUrl);
    } catch (Exception e) {
      throw new IdentityBrokerException("Could not create authentication request.", e);
    }
  }

  private Response renderPage(AuthenticationRequest request, URI callbackUrl) {
    LoginFormsProvider formProvider = session.getProvider(LoginFormsProvider.class);
    formProvider.setAuthenticationSession(request.getAuthenticationSession());
    UserModel user = request.getAuthenticationSession().getAuthenticatedUser();
    if (user != null) {
      formProvider.setUser(user);
    }
    boolean isLinkMode =
      request.getAuthenticationSession().getAuthNote("LINKING_IDENTITY_PROVIDER") != null;
    formProvider.setAttribute("linkMode", isLinkMode);
    formProvider.setAttribute("callbackUrl", callbackUrl.toString());
    formProvider.setAttribute("linkClientId", getLinkClientId(request.getAuthenticationSession()));
    formProvider.setAttribute("providerAlias", getConfig().getAlias());
    return formProvider.createForm("telegram-qr-link.ftl");
  }

  private String getLinkClientId(AuthenticationSessionModel authSession) {
    if (authSession != null && authSession.getParentSession() != null) {
      String userSessionId = authSession.getParentSession().getId();
      UserSessionModel userSession = session.sessions()
        .getUserSession(authSession.getRealm(), userSessionId);
      if (userSession != null) {
        for (AuthenticatedClientSessionModel cs : userSession.getAuthenticatedClientSessions()
          .values()) {
          ClientModel client = cs.getClient();
          String baseUrl = client.getBaseUrl();
          if (StringUtil.isNotBlank(baseUrl)
            && !Constants.ACCOUNT_MANAGEMENT_CLIENT_ID.equals(client.getClientId())) {
            return client.getClientId();
          }
        }
      }
    }
    ClientModel accountClient = session.getContext().getRealm()
      .getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
    return accountClient != null ? accountClient.getClientId() : "";
  }

  private boolean hasTelegramQrData(AuthenticationSessionModel authSession) {
    if (authSession == null || authSession.getParentSession() == null) {
      return false;
    }
    String sessionId = authSession.getParentSession().getId();
    Map<String, String> data = session.singleUseObjects().get(sessionId);
    return data != null && data.containsKey("user");
  }
}