package com.khalibre.keycloak.provider.telegram;

import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import jakarta.ws.rs.core.Response;

public class TelegramIdentityProvider extends AbstractIdentityProvider<IdentityProviderModel> {

  public TelegramIdentityProvider(KeycloakSession session, IdentityProviderModel config) {
    super(session, config);
  }

  @Override
  public Response retrieveToken(KeycloakSession keycloakSession,
    FederatedIdentityModel federatedIdentityModel) {
    return null;
  }
}
