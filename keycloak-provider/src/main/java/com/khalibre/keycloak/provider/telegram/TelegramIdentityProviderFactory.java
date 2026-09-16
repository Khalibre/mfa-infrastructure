package com.khalibre.keycloak.provider.telegram;

import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class TelegramIdentityProviderFactory extends
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
    return new IdentityProviderModel();
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ProviderConfigurationBuilder.create()
        .property()
        .name(TelegramIdentityProvider.TELEGRAM_BOT_USERNAME_KEY)
        .label("Bot Username")
        .helpText("Telegram bot username (without @).")
        .type(ProviderConfigProperty.STRING_TYPE)
        .add()
        .property()
        .name(TelegramIdentityProvider.TELEGRAM_BOT_TOKEN_KEY)
        .label("Bot Token")
        .helpText("Telegram bot token obtained from BotFather.")
        .type(ProviderConfigProperty.PASSWORD)
        .add()
        .build();
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}