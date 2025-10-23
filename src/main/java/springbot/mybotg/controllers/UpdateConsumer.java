package springbot.mybotg.controllers;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import springbot.mybotg.config.BotConfig;
import springbot.mybotg.service.AccountService;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final Logger logger = LoggerFactory.getLogger(UpdateConsumer.class);
    private final TelegramClient telegramClient;

    public UpdateConsumer(BotConfig botConfig) {
        logger.info("Initializing UpdateConsumer with token: {}", botConfig.getToken());

        if (botConfig.getToken() == null || botConfig.getToken().isEmpty()) {
            throw new IllegalArgumentException("Bot token cannot be null or empty");
        }
        this.telegramClient = new OkHttpTelegramClient(botConfig.getToken());
    }

    @Autowired
    public AccountService accountService;

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getFrom().getUserName();

            if (text.equals("/start")) {
                accountService.saveAccount(chatId,name);
                sendMainMenu(chatId);
            } else {
                accountService.saveUserText(chatId, text);
                sendMessage(chatId, "Ваш текст сохранен");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
//        var user = callbackQuery.getFrom();
        switch (data) {
            case "button1" -> handleCheckAccountPremium(chatId);
            case "getText" -> handleGetTextCommand(chatId);
        }
    }

    private void handleGetTextCommand(Long chatId) throws TelegramApiException {
        try {
            logger.info("Получаем текст для chatId: {}", chatId);
            String savedText = accountService.getUserText(chatId);

            if (savedText != null && !savedText.isEmpty()) {
                logger.info("Текст найден: {}", savedText);
                sendMessage(chatId, "Ваш сохраненный текст: " + savedText);
            } else {
                logger.info("Текст не найден для chatId: {}", chatId);
                sendMessage(chatId, "У вас нет сохраненного текста");
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении текста", e);
            sendMessage(chatId, "Произошла ошибка при получении текста");
        }
    }

    public void handleCheckAccountPremium (Long chatId) throws TelegramApiException {
        if (accountService.checkAccountPremium(chatId).equals("premium")) {
            sendMessage(chatId, "У вас премиум аккаунт");
        } else {
            sendMessage(chatId, "PAY PREMIUM ACC");
        }
    }


    @SneakyThrows
    private void sendMenuButton1(Long chatId) {
        sendMessage(chatId, "You click 1 button");
    }

    private void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendMainMenu(Long chatId) throws TelegramApiException {

        SendMessage sendMessage = SendMessage.builder()
                .text("This is first menu")
                .chatId(chatId)
                .build();

        var mainMenuButton = InlineKeyboardButton.builder()
                .text("First button")
                .callbackData("button1")
                .build();

        var mainMenuButton2 = InlineKeyboardButton.builder()
                .text("Получить текст")
                .callbackData("getText")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(mainMenuButton),
                new InlineKeyboardRow(mainMenuButton2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }
}