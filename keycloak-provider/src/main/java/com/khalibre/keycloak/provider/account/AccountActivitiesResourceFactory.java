package com.khalibre.keycloak.provider.account;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * Registers the account activity log REST endpoint under
 * /realms/{realm}/account-activities/...
 */
public class AccountActivitiesResourceFactory implements RealmResourceProviderFactory {

  public static final String PROVIDER_ID = "account-activities";

  @Override
  public AccountActivitiesResource create(KeycloakSession session) {
    return new AccountActivitiesResource(session);
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public void init(Config.Scope config) {
    // no-op
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }
}
