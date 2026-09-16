package com.khalibre.keycloak.provider.telegram;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

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
  public Object callback(RealmModel realm, IdentityProvider.AuthenticationCallback callback,
    EventBuilder event) {
    return null;
  }

  @Override
  public Response performLogin(AuthenticationRequest request) {
    return null;
  }
}