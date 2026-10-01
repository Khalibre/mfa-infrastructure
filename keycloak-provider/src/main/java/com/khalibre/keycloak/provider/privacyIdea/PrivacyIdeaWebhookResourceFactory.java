package com.khalibre.keycloak.provider.privacyIdea;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class PrivacyIdeaWebhookResourceFactory implements RealmResourceProviderFactory {

  public static final String PROVIDER_ID = "privacyidea";

  private String baseUrl;
  private String adminUsername;
  private String adminPassword;
  private int spassExpiryMinutes;

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    return new PrivacyIdeaWebhookResource(session, baseUrl, adminUsername, adminPassword,
        spassExpiryMinutes);
  }

  @Override
  public void init(Config.Scope config) {
    // Read configuration settings with environment variable fallbacks
    this.baseUrl = config.get("baseUrl",
        System.getenv().getOrDefault("PRIVACYIDEA_URL", "http://mfa-privacyidea:8080"));
    this.adminUsername = config.get("adminUsername",
        System.getenv().getOrDefault("PI_ADMIN_USER", "admin"));
    this.adminPassword = config.get("adminPassword",
        System.getenv().getOrDefault("PI_ADMIN_PASSWORD", "secret"));
    this.spassExpiryMinutes = config.getInt("spassExpiryMinutes", 5);
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public List<ProviderConfigProperty> getConfigMetadata() {
    return ProviderConfigurationBuilder.create()
        .property()
        .name("baseUrl")
        .label("privacyIDEA Base URL")
        .type(ProviderConfigProperty.STRING_TYPE)
        .defaultValue("http://mfa-privacyidea:8080")
        .helpText("Base URL of privacyIDEA server")
        .add()
        .property()
        .name("adminUsername")
        .label("Admin Username")
        .type(ProviderConfigProperty.STRING_TYPE)
        .defaultValue("admin")
        .helpText("Service account username to invoke REST APIs")
        .add()
        .property()
        .name("adminPassword")
        .label("Admin Password")
        .type(ProviderConfigProperty.PASSWORD)
        .helpText("Service account password")
        .add()
        .property()
        .name("spassExpiryMinutes")
        .label("SPASS Validity (Minutes)")
        .type(ProviderConfigProperty.STRING_TYPE)
        .defaultValue("5")
        .helpText("Expiry time in minutes for generated OTP PIN")
        .add()
        .build();
  }
}
