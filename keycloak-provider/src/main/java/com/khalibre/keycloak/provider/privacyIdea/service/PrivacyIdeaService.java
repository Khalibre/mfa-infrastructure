package com.khalibre.keycloak.provider.privacyIdea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

public class PrivacyIdeaService {

  private static final Logger log = Logger.getLogger(PrivacyIdeaService.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final HttpClient httpClient = HttpClient.newHttpClient();

  private final String baseUrl;
  private final String adminUsername;
  private final String adminPassword;

  public PrivacyIdeaService(String baseUrl, String adminUsername, String adminPassword) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  public String getSpassTokenSerial(String username, String adminToken) throws Exception {
    String endpoint = String.format("%s/token/?user=%s&type=spass", baseUrl, username);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Authorization", adminToken)
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == HttpStatus.SC_OK) {
      JsonNode root = objectMapper.readTree(response.body());
      JsonNode tokens = root.path("result").path("value").path("tokens");
      if (tokens.isArray() && !tokens.isEmpty()) {
        // Return serial of the first active token
        return tokens.get(0).path("serial").asText(null);
      }
    }
    log.errorf("method=getSpassTokenSerial httpStatus=%d body=%s", response.statusCode(),
        response.body());
    return null;
  }

  public String getPrivacyIdeaAuthToken() throws Exception {
    Map<String, String> creds = Map.of(
        "username", adminUsername,
        "password", adminPassword
    );
    String requestBody = objectMapper.writeValueAsString(creds);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/auth"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == HttpStatus.SC_OK) {
      JsonNode root = objectMapper.readTree(response.body());
      return root.path("result").path("value").path("token").asText(null);
    }
    log.errorf("method=getPrivacyIdeaAuthToken httpStatus=%d body=%s", response.statusCode(),
        response.body());
    return null;
  }
}