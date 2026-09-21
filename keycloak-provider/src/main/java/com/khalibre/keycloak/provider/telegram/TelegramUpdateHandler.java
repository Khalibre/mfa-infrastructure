package com.khalibre.keycloak.provider.telegram;

public class TelegramUpdateHandler {

  private static final String COMMAND_START_LOGIN = "/start login_";
  private final String botToken;

  public TelegramUpdateHandler(String botToken) {
    this.botToken = botToken;
  }

  public void handleUpdate(TelegramWebhookPayload update) {
    if (update.getMessage() == null) {
      return;
    }

    TelegramWebhookPayload.From from = update.getMessage().getFrom();
    TelegramWebhookPayload.Chat chat = update.getMessage().getChat();
    String chatId = chat != null ? chat.getId() : from.getId();
    String text = update.getMessage().getText();

    if (text != null && text.startsWith(COMMAND_START_LOGIN)) {
      handleStartLoginCommand(from, chatId, text);
    } else if (update.getMessage().getContact() != null) {
      handleContact(from, update.getMessage().getContact());
    }
  }

  private void handleStartLoginCommand(TelegramWebhookPayload.From from,
    String chatId, String text) {
    String authStateId = text.substring(COMMAND_START_LOGIN.length());

    AuthState state = AuthStateCache.get(authStateId);
    if (state == null) {
      return;
    }

    state.setTelegramUserId(from.getId());
    state.setFirstName(from.getFirstName());
    state.setLastName(from.getLastName());
    state.setUsername(from.getUsername());
    state.setStatus("BOT_STARTED");
    AuthStateCache.store(authStateId, state);

    TelegramBotClient botClient = new TelegramBotClient(botToken);
    botClient.requestPhoneNumber(chatId);
  }

  private void handleContact(TelegramWebhookPayload.From from,
    TelegramWebhookPayload.Contact contact) {
    AuthState state = AuthStateCache.findByTelegramUserId(from.getId());
    if (state != null) {
      if (contact.getFirstName() != null) {
        state.setFirstName(contact.getFirstName());
      }
      if (contact.getLastName() != null) {
        state.setLastName(contact.getLastName());
      }
      state.setPhoneNumber(contact.getPhoneNumber());
      state.setStatus("COMPLETED");
      AuthStateCache.store(state.getId(), state);
    }
  }
}
