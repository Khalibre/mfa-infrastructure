package com.khalibre.keycloak.provider.account;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.events.Event;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * Backs the "Account activities" page of the account console.
 *
 * <p>Mounted at <code>/realms/{realm}/account-activities/...</code> by
 * {@link AccountActivitiesResourceFactory}.
 *
 * <p>The endpoint only ever returns events of the caller: the user id comes from the verified
 * bearer token, never from a request parameter, so one user cannot read another user's activity
 * log. The token is required to be issued for the <code>account</code> client, which is what
 * the account console (client <code>account-console</code>) uses, and service accounts are
 * rejected. This mirrors the checks performed by Keycloak's own
 * {@code AccountRestService} so that a token which the account REST API would reject cannot
 * read the activity log either.
 */
public class AccountActivitiesResource implements RealmResourceProvider {

  /** Detail keys that are safe to render for the end user. */
  private static final Set<String> ALLOWED_DETAIL_KEYS =
      Set.of("auth_method", "identity_provider", "identity_provider_auth_method", "auth_method_details");

  private static final int DEFAULT_MAX_RESULTS = 25;
  private static final int MAX_ALLOWED_RESULTS = 100;

  private final KeycloakSession session;

  @Context
  private UriInfo uriInfo;

  @Context
  private HttpHeaders headers;

  public AccountActivitiesResource(KeycloakSession session) {
    this.session = session;
  }

  @GET
  @Path("events")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEvents(
      @QueryParam("first") Integer first, @QueryParam("max") Integer max, @Context HttpHeaders headers) {
    RealmModel realm = session.getContext().getRealm();
    ClientModel accountClient = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
    if (accountClient == null || !accountClient.isEnabled()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    UserModel user = authenticate(accountClient, headers);
    if (user == null) {
      throw new NotAuthorizedException("Bearer token required");
    }

    int firstResult = first == null ? 0 : Math.max(first, 0);
    int maxResults = max == null ? DEFAULT_MAX_RESULTS : Math.min(Math.max(max, 1), MAX_ALLOWED_RESULTS);

    EventStoreProvider store = session.getProvider(EventStoreProvider.class);
    List<AccountActivity> activities = new ArrayList<>();
    if (store != null) {
      // Scoped to the caller's own user id on purpose.
      try (var events =
          store
              .createQuery()
              .realm(realm.getId())
              .user(user.getId())
              .orderByDescTime()
              .firstResult(firstResult)
              .maxResults(maxResults)
              .getResultStream()) {
        events.map(this::toAccountActivity).forEach(activities::add);
      }
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("first", firstResult);
    result.put("max", maxResults);
    result.put("events", activities);
    return Response.ok(result).build();
  }

  /**
   * Verifies the bearer token and resolves the user it belongs to.
   *
   * @return the authenticated user, or {@code null} if the token is missing, not valid, not
   *     issued for the account client, or belongs to a service account.
   */
  private UserModel authenticate(ClientModel accountClient, HttpHeaders headers) {
    String tokenString = AppAuthManager.extractAuthorizationHeaderTokenOrReturnNull(headers);
    if (tokenString == null) {
      return null;
    }

    AuthenticationManager.AuthResult authResult =
        new AppAuthManager.BearerTokenAuthenticator(session)
            .setUriInfo(uriInfo)
            .setHeaders(headers)
            .setConnection(session.getContext().getConnection())
            .setTokenString(tokenString)
            .authenticate();

    if (authResult == null || authResult.getUser() == null) {
      return null;
    }

    AccessToken accessToken = authResult.getToken();
    if (accessToken == null || !accessToken.hasAudience(accountClient.getClientId())) {
      return null;
    }

    if (authResult.getUser().getServiceAccountClientLink() != null) {
      return null;
    }

    return authResult.getUser();
  }

  private AccountActivity toAccountActivity(Event event) {
    return new AccountActivity(
        event.getTime(),
        event.getType() == null ? EventType.LOGIN.name() : event.getType().name(),
        event.getClientId(),
        event.getIpAddress(),
        event.getError(),
        filterDetails(event.getDetails()));
  }

  private Map<String, String> filterDetails(Map<String, String> details) {
    if (details == null || details.isEmpty()) {
      return null;
    }

    Map<String, String> filtered = new LinkedHashMap<>();
    for (String key : ALLOWED_DETAIL_KEYS) {
      String value = details.get(key);
      if (value != null) {
        filtered.put(key, value);
      }
    }
    return filtered.isEmpty() ? null : filtered;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
    // Nothing to release.
  }
}
