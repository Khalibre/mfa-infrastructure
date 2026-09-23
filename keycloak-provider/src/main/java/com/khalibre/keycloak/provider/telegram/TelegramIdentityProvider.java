package com.khalibre.keycloak.provider.telegram;

import jakarta.annotation.Nonnull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
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
import org.keycloak.services.ErrorPage;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

public class TelegramIdentityProvider extends AbstractIdentityProvider<IdentityProviderModel> {

  public static final String TELEGRAM_BOT_USERNAME_KEY = "telegram_bot_username";
  public static final String TELEGRAM_BOT_TOKEN_KEY = "telegram_bot_token";
  public static final String ATTR_TG_FIRST_NAME = "telegram-first-name";
  public static final String ATTR_TG_LAST_NAME = "telegram-last-name";
  public static final String ATTR_TG_USERNAME = "telegram-username";
  public static final String ATTR_TG_USER_ID = "telegram-user-id";
  public static final String ATTR_TG_USER_PHONE_NUMBER = "telegram-phone-number";

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
    user.setSingleAttribute(ATTR_TG_USER_ID, context.getUserAttribute(ATTR_TG_USER_ID));
    user.setSingleAttribute(ATTR_TG_USERNAME, context.getUserAttribute(ATTR_TG_USERNAME));
    user.setSingleAttribute(ATTR_TG_USER_PHONE_NUMBER,
      context.getUserAttribute(ATTR_TG_USER_PHONE_NUMBER));
  }

  @Override
  public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
    BrokeredIdentityContext context) {
    user.setSingleAttribute(ATTR_TG_USER_ID, context.getUserAttribute(ATTR_TG_USER_ID));
    user.setSingleAttribute(ATTR_TG_USERNAME, context.getUserAttribute(ATTR_TG_USERNAME));
    user.setSingleAttribute(ATTR_TG_USER_PHONE_NUMBER,
      context.getUserAttribute(ATTR_TG_USER_PHONE_NUMBER));
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
    return new Endpoint(callback, event, session, this);
  }

  @Override
  public Response performLogin(AuthenticationRequest request) {
    try {
      final UriBuilder uriBuilder = UriBuilder.fromUri(request.getRedirectUri());
      uriBuilder.queryParam("state", request.getState().getEncoded());
      URI callbackUrl = uriBuilder.build();
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

  private static String sanitizeEmojiAndRareScript(String input) {
    if (StringUtil.isBlank(input)) {
      return input;
    }
    return input.replaceAll("[^\\u0000-\\uFFFF]", "");
  }

  private static class Endpoint {

    private final AuthenticationCallback callback;
    private final EventBuilder event;
    private final KeycloakSession session;
    private final TelegramIdentityProvider provider;

    private Endpoint(AuthenticationCallback callback, EventBuilder event, KeycloakSession session,
      TelegramIdentityProvider self) {
      this.callback = callback;
      this.event = event;
      this.session = session;
      this.provider = self;
    }

    @GET
    @Path("/")
    public Response authenticate(@QueryParam("state") String state) {
      try {
        AuthenticationSessionModel authSession = callback.getAndVerifyAuthenticationSession(state);
        session.getContext().setAuthenticationSession(authSession);
        String sessionId = authSession.getParentSession().getId();

        AuthState auth = AuthStateSession.get(session, sessionId);
        if (auth == null || !"COMPLETED".equals(auth.getStatus())) {
          return callback.error("telegram_auth_failed");
        }

        BrokeredIdentityContext context = buildContext(
          auth.getTelegramUserId(),
          getAutoLinkUsername(auth.getPhoneNumber()),
          auth.getUsername(),
          auth.getFirstName(),
          auth.getLastName(),
          auth.getPhoneNumber());

        AuthStateSession.remove(session, sessionId);
        context.setIdp(provider);
        context.setAuthenticationSession(authSession);
        return callback.authenticated(context);
      } catch (WebApplicationException wae) {
        throw wae;
      } catch (Exception e) {
        return errorIdentityProviderLogin(e.getMessage());
      }
    }

    private String getAutoLinkUsername(String phoneNumber) {
      UserModel user = provider.session.users()
        .searchForUserByUserAttributeStream(session.getContext().getRealm(),
          ATTR_TG_USER_PHONE_NUMBER, phoneNumber)
        .findFirst()
        .orElse(null);
      return user == null ? phoneNumber : user.getUsername();
    }

    @Nonnull
    private BrokeredIdentityContext buildContext(String telegramUserId, String autoLinkUsername,
      String username, String firstName, String lastName, String phoneNumber) {
      BrokeredIdentityContext context = new BrokeredIdentityContext(telegramUserId,
        provider.getConfig());
      context.setModelUsername(autoLinkUsername);
      context.setUsername(username);
      context.setFirstName(sanitizeEmojiAndRareScript(firstName));
      context.setLastName(sanitizeEmojiAndRareScript(lastName));
      context.setUserAttribute(ATTR_TG_USER_ID, telegramUserId);
      context.setUserAttribute(ATTR_TG_USERNAME, username);
      context.setUserAttribute(ATTR_TG_USER_PHONE_NUMBER, phoneNumber);
      context.setUserAttribute(ATTR_TG_FIRST_NAME, firstName);
      context.setUserAttribute(ATTR_TG_LAST_NAME, lastName);
      return context;
    }

    private Response errorIdentityProviderLogin(String message) {
      event.event(EventType.IDENTITY_PROVIDER_LOGIN);
      event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
      return ErrorPage.error(session, null, Status.BAD_REQUEST, message);
    }
  }
}