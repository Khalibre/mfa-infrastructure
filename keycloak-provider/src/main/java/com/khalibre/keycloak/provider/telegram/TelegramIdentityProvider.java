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
import java.util.Iterator;
import java.util.stream.Stream;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

public class TelegramIdentityProvider extends
  AbstractIdentityProvider<OAuth2IdentityProviderConfig> {

  public static final String ATTR_TG_FIRST_NAME = "telegram-first-name";
  public static final String ATTR_TG_LAST_NAME = "telegram-last-name";
  public static final String ATTR_TG_USERNAME = "telegram-username";
  public static final String ATTR_TG_USER_ID = "telegram-user-id";
  public static final String ATTR_TG_USER_PHONE_NUMBER = "telegram-phone-number";
  public static final String AUTO_LINK_BY_PHONE_NUMBER_KEY = "autoLinkByPhoneNumber";

  public TelegramIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
    super(session, config);
  }

  private boolean isAutoLinkByPhoneNumberEnabled() {
    String value = getConfig().getConfig().get(AUTO_LINK_BY_PHONE_NUMBER_KEY);
    return "true".equalsIgnoreCase(value);
  }

  public String getBotUsername() {
    return getConfig().getClientId();
  }

  public String getBotToken() {
    return getConfig().getClientSecret();
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
    formProvider.setAttribute("providerAlias", getConfig().getAlias());
    return formProvider.createForm("telegram-qr-link.ftl");
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
          findAutoLinkUsername(auth.getPhoneNumber()),
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

    private String findAutoLinkUsername(String phoneNumber) {
      if (!provider.isAutoLinkByPhoneNumberEnabled() || StringUtil.isBlank(phoneNumber)) {
        return null;
      }

      RealmModel realm = session.getContext().getRealm();
      try (Stream<UserModel> matches = provider.session.users()
        .searchForUserByUserAttributeStream(realm, ATTR_TG_USER_PHONE_NUMBER, phoneNumber)) {
        Iterator<UserModel> iterator = matches.iterator();
        if (!iterator.hasNext()) {
          return null;
        }

        UserModel user = iterator.next();
        if (iterator.hasNext()) {
          return null;
        }

        String providerAlias = provider.getConfig().getAlias();
        if (provider.session.users().getFederatedIdentity(realm, user, providerAlias) != null) {
          return null;
        }
        return user.getUsername();
      }
    }

    @Nonnull
    private BrokeredIdentityContext buildContext(String telegramUserId, String autoLinkUsername,
      String username, String firstName, String lastName, String phoneNumber) {
      BrokeredIdentityContext context = new BrokeredIdentityContext(telegramUserId,
        provider.getConfig());
      if (autoLinkUsername != null) {
        context.setModelUsername(autoLinkUsername);
      } else {
        context.setModelUsername(phoneNumber);
      }
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