package com.khalibre.keycloak.provider.telegram;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class TelegramIdentityProviderFactory  extends
  AbstractIdentityProviderFactory<IdentityProvider> {
  public static final String PROVIDER_ID = "telegram";

  @Override
  public String getName() {
    return "Telegram";
  }

  @Override
  public IdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new TelegramIdentityProvider(session, model);
  }

  @Override
  public IdentityProviderModel createConfig() {
    return null;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
