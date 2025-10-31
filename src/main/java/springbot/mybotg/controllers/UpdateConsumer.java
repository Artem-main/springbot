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
import springbot.mybotg.constants.MuscleGroupButton;
import springbot.mybotg.config.BotConfig;
import springbot.mybotg.models.Account;
import springbot.mybotg.models.Exercise;
import springbot.mybotg.service.AccountService;
import springbot.mybotg.service.DataImportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    public DataImportService dataImportService;

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getFrom().getUserName();

            if (text.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Ваш текст " + text);
            }
        } else if (update.hasCallbackQuery()) {
            handleShowAndSaveExercises(update.getCallbackQuery());
        }
    }

    private void handleShowAndSaveExercises(CallbackQuery callbackQuery) throws TelegramApiException {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var userName = callbackQuery.getFrom().getUserName();
        switch (data) {
            case "button1" -> handleViewExerciseMenu(chatId, data);
            case "getText" -> handleGetTextCommand(chatId);
//            case "createTraining" -> handleCreateExercises (chatId);
            case "viewMuscleGroup" -> handleSetMuscleGroup(chatId);
            case "chest",
                 "legs",
                 "backMuscle",
                 "shoulders",
                 "triceps",
                 "biceps"-> handleSaveMuscleGroup(chatId, userName, data);
            default -> handleShowAndSaveExercises(data,chatId);
        }
    }

    @SneakyThrows
    private void handleSaveMuscleGroup(Long chatId, String userName, String muscleGroup) {
        for (MuscleGroupButton button : MuscleGroupButton.values()) {
            if (button.getNameCallbackData().equals(muscleGroup)) {
                muscleGroup = button.getNameGroup();
            }
        }

        List<Exercise> exercises = dataImportService.getTraining(muscleGroup);

        List<InlineKeyboardRow> rows = new ArrayList<>();
        StringBuilder response = new StringBuilder("Упражнения для: " + muscleGroup + "\n");

        for (Exercise exercise : exercises) {
            response.append(exercise.getExercise()).append("\n");
            String callbackData = "exercise_" + exercise.getId();

            accountService.saveOrUpdateExercise(chatId, userName, muscleGroup, exercise.getExercise());

            InlineKeyboardButton menuButton = InlineKeyboardButton.builder()
                    .text(exercise.getExercise())
                    .callbackData(callbackData)
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(menuButton);
            rows.add(row);
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите упражнение")
                .replyMarkup(keyboard)
                .build();
        telegramClient.execute(sendMessage);
    }

    @SneakyThrows
    public void handleShowAndSaveExercises(String callbackData, Long chatId) {
        if (callbackData.startsWith("exercise_")) {
            Long exerciseId = Long.parseLong(callbackData.replace("exercise_", ""));
            Exercise exercise = dataImportService.findExerciseById(exerciseId);

            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("Вы выбрали упражнение: " + exercise.getExercise())
                    .build();
            telegramClient.execute(response);
        }
    }

    @SneakyThrows
    private void handleViewExerciseMenu (Long chatId, String muscleGroup) {
        List<InlineKeyboardRow> rows = new ArrayList<>();


        // Формируем клавиатуру
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        // Отправляем сообщение с клавиатурой
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите упражнение для группы «" + muscleGroup + "»:")
                .replyMarkup(keyboard)
                .build();

        telegramClient.execute(sendMessage);
    }

    @SneakyThrows
    private void handleSetMuscleGroup(Long chatId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (MuscleGroupButton button : MuscleGroupButton.values()) {
            // Создаём кнопку
            InlineKeyboardButton menuButton = InlineKeyboardButton.builder()
                    .text(button.getNameGroup())
                    .callbackData(button.getNameCallbackData())
                    .build();

            // Добавляем кнопку в строку (создаём новую строку для каждой кнопки)
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(menuButton);
            rows.add(row);
        }

        // Собираем клавиатуру
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите группу мышц")
                .replyMarkup(keyboard)
                .build();
        telegramClient.execute(sendMessage);
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

        var mainMenuButton3 = InlineKeyboardButton.builder()
                .text("Создать тренировку")
                .callbackData("createTraining")
                .build();

        var mainMenuButton2 = InlineKeyboardButton.builder()
                .text("Получить текст")
                .callbackData("getText")
                .build();

        var pingButton = InlineKeyboardButton.builder() // Новая кнопка для ping
                .text("Меню группы мышц")
                .callbackData("viewMuscleGroup")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(mainMenuButton),
                new InlineKeyboardRow(mainMenuButton2),
                new InlineKeyboardRow(mainMenuButton3),
                new InlineKeyboardRow(pingButton)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }
}