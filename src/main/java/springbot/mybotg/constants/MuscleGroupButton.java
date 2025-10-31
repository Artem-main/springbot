package springbot.mybotg.constants;

import lombok.Getter;

@Getter
public enum MuscleGroupButton {
    CHEST("Грудь", "chest"),
    BACK("Спина", "backMuscle"),
    LEGS("Ноги","legs"),
    SHOULDERS("Плечи","shoulders"),
    TRICERS("Трицепс","triceps"),
    BICEPS("Бицепс","biceps");

    private final String nameGroup;
    private final String nameCallbackData;

    MuscleGroupButton(String nameGroup, String nameCallbackData) {
        this.nameGroup = nameGroup;
        this.nameCallbackData = nameCallbackData;
    }

}
