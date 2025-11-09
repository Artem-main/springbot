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
import springbot.mybotg.service.AccountPremiumService;
import springbot.mybotg.service.AccountService;
import springbot.mybotg.service.DataImportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static springbot.mybotg.constants.MuscleGroupButton.*;

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

    @Autowired
    public AccountPremiumService accountPremiumService;

//    Хранение состояния ожидания ввода веса
    private final Map<Long, String> pendingWeightExercises = new ConcurrentHashMap<>();
//    Хранение состояния ожидания ввода наименования тренировки
    private final Map<Long, String> pendingNewExercisesForPremiumUser = new ConcurrentHashMap<>();

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();

            if (text.equals("/start")) {
                sendMainMenu(chatId);
            } else if (pendingWeightExercises.containsKey(chatId)) {
                // Если ждём вес — обрабатываем ввод
                handleWeightInput(chatId, text);
            } else if (pendingNewExercisesForPremiumUser.containsKey(chatId)) {
//                Если ждем ввод упражнения - обрабатываем ввод
                handleInputExerciseForPremiumUser (chatId, userName, text);
            }

        } else if (update.hasCallbackQuery()) {
            handleShowAndSaveExercises(update.getCallbackQuery());
        }
    }

    private void handleShowAndSaveExercises(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var userName = callbackQuery.getFrom().getUserName();

        switch (data) {
            case "viewListExercisesCurrentUser" -> menuForShowListExercisesCurrentUser(chatId);
            case "createTraining" -> createTraining(chatId);
            case "viewMuscleGroup" -> handleSetMuscleGroup(chatId);
            case "chest", "legs", "backMuscle", "shoulders", "triceps", "biceps" ->
                    handleSaveMuscleGroup(chatId, data);
            case "Грудь", "Ноги", "Плечи", "Бицепс", "Трицепс", "Спина" ->
                    viewAllExerciseThisUser(chatId, data);
            case "chestPremium", "legsPremium", "backMusclePremium", "shouldersPremium", "tricepsPremium", "bicepsPremium" ->
                    handleSaveExercisesForPremiumUser (chatId, data);
            default -> handleExerciseSelection(data, chatId, userName);
        }
    }

    @SneakyThrows
    private void handleInputExerciseForPremiumUser(Long chatId, String userName, String nameExercise) {
//        Реализация обработки ввода упражнения премиум пользователя
        String muscleGroup = pendingNewExercisesForPremiumUser.get(chatId);
        accountPremiumService.saveOrUpdateExercise(chatId, userName, muscleGroup, nameExercise);
        sendMessage(chatId, "Упражнение : \n" + nameExercise + " сохранено в базу");
        pendingNewExercisesForPremiumUser.remove(chatId);
    }

    @SneakyThrows
    private void handleSaveExercisesForPremiumUser(Long chatId, String muscleGroup) {
        if (muscleGroup.equals(BACK.getNameCallbackDataPremium())) {
            muscleGroup = BACK.getNameGroup();
        } else if (muscleGroup.equals(CHEST.getNameCallbackDataPremium())) {
            muscleGroup = CHEST.getNameGroup();
        } else if (muscleGroup.equals(LEGS.getNameCallbackDataPremium())) {
            muscleGroup = LEGS.getNameGroup();
        } else if (muscleGroup.equals(SHOULDERS.getNameCallbackDataPremium())) {
            muscleGroup = SHOULDERS.getNameGroup();
        } else if (muscleGroup.equals(TRICERS.getNameCallbackDataPremium())) {
            muscleGroup = TRICERS.getNameGroup();
        } else if (muscleGroup.equals(BICEPS.getNameCallbackDataPremium())) {
            muscleGroup = BICEPS.getNameGroup();
        }

        pendingNewExercisesForPremiumUser.put(chatId, muscleGroup);

        sendMessage(chatId, "Выбрана группа мышц " +
                muscleGroup +
                "\nВведите название упражнения");
    }


    @SneakyThrows
    private void createTraining(Long chatId) {
        if (accountPremiumService.checkAccountPremium(chatId).isPresent() || chatId == 155522892) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            for (MuscleGroupButton button : MuscleGroupButton.values()) {
                // Создаём кнопку
                InlineKeyboardButton menuButton = InlineKeyboardButton.builder()
                        .text(button.getNameGroup())
                        .callbackData(button.getNameCallbackDataPremium())
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
                    .text("Выберите группу мышц для которой необходимо создать упражнения")
                    .replyMarkup(keyboard)
                    .build();
            telegramClient.execute(sendMessage);

        } else sendMessage(chatId,"Необходимо приобрести премиум аккаунт для создания своих тренировок");
    }

    @SneakyThrows
    private void handleSaveMuscleGroup(Long chatId, String muscleGroup) {
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
    private void handleExerciseSelection(String callbackData, Long chatId, String userName) {
        if (callbackData.startsWith("exercise_")) {
            Long exerciseId = Long.parseLong(callbackData.replace("exercise_", ""));
            Exercise exercise = dataImportService.findExerciseById(exerciseId);

            // Сохраняем упражнение в БД
            accountService.saveOrUpdateExercise(
                    chatId,
                    userName,
                    exercise.getMuscleGroup(),
                    exercise.getExercise()
            );

            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("Вы выбрали упражнение: " + exercise.getExercise() + "\n" +
                            "Введите вес (в кг):")
                    .build();
            telegramClient.execute(response);

            // Запоминаем, что этот chatId ждёт ввод веса для данного упражнения
            pendingWeightExercises.put(chatId, exercise.getExercise());
        }
    }

    @SneakyThrows
    private void handleWeightInput(Long chatId, String input) {
        try {
            int weight = Integer.parseInt(input);

            // Получаем название упражнения, для которого ждали вес
            String exerciseName = pendingWeightExercises.get(chatId);

            // Сохраняем вес в БД
            accountService.setWeightExercise(chatId, weight, exerciseName);

            // Отправляем подтверждение
            sendMessage(chatId, "Вес сохранён: " + weight + " кг для упражнения «" + exerciseName + "»");

            // Очищаем состояние
            pendingWeightExercises.remove(chatId);

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите число (вес в кг).");
        }
    }

    @SneakyThrows
    public void menuForShowListExercisesCurrentUser (Long chatId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (MuscleGroupButton button : MuscleGroupButton.values()) {
            // Создаём кнопку
            InlineKeyboardButton menuButton = InlineKeyboardButton.builder()
                    .text(button.getNameGroup())
                    .callbackData(button.getNameGroup())
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

    @SneakyThrows
    private void viewAllExerciseThisUser(Long chatId, String muscleGroup) {
        List<String> allExercisesList = accountService.AllExercise(chatId, muscleGroup);

        if (!allExercisesList.isEmpty()) {
            StringBuilder text = new StringBuilder("Список упражнений:\n");

            for (String exercise : allExercisesList) {
                Optional<Integer> weight = accountService.getWeightExercise(chatId, exercise);
                text.append("• ").append(exercise).append(" (вес: ").append(weight.get()).append(" кг)\n");
            }

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(text.toString())
                    .build();
            telegramClient.execute(message);
        } else {
            sendMessage(chatId, "Список упражнений пуст. \n Пора начать тренировки! Выбери свою первую тренировку!");
        }
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

    private void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendMainMenu(Long chatId) throws TelegramApiException {

        SendMessage sendMessage = SendMessage.builder()
                .text("Выберите пункт меню")
                .chatId(chatId)
                .build();

        var mainMenuButton = InlineKeyboardButton.builder()
                .text("Показать мои результаты")
                .callbackData("viewListExercisesCurrentUser")
                .build();

        var mainMenuButton3 = InlineKeyboardButton.builder()
                .text("Создать тренировку")
                .callbackData("createTraining")
                .build();


        var pingButton = InlineKeyboardButton.builder()
                .text("Выбрать тренировку")
                .callbackData("viewMuscleGroup")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(mainMenuButton3),
                new InlineKeyboardRow(pingButton),
                new InlineKeyboardRow(mainMenuButton)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }
}