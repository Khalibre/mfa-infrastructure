package com.khalibre.keycloak.provider.telegram;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
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
      .property()
      .name(TelegramIdentityProvider.AUTO_LINK_BY_PHONE_NUMBER_KEY)
      .label("Auto Link By Phone Number")
      .helpText(
        "Automatically link Telegram account to existing Keycloak user with matching phone number.")
      .type(ProviderConfigProperty.BOOLEAN_TYPE)
      .defaultValue("true")
      .add()
      .build();
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
    // Bot initialization is triggered via the /init endpoint, not at startup,
    // because identity providers may not be configured yet during postInit.
  }

  @Override
  public void close() {
    TelegramBotManager.getInstance().shutdown();
  }
}
