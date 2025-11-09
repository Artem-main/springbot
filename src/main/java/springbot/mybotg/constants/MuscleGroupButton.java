package springbot.mybotg.constants;

import lombok.Getter;

@Getter
public enum MuscleGroupButton {
    CHEST("Грудь", "chest", "chestPremium"),
    BACK("Спина", "backMuscle", "backMusclePremium"),
    LEGS("Ноги","legs", "legsPremium"),
    SHOULDERS("Плечи","shoulders", "shouldersPremium"),
    TRICERS("Трицепс","triceps", "tricepsPremium"),
    BICEPS("Бицепс","biceps", "bicepsPremium");

    private final String nameGroup;
    private final String nameCallbackData;
    private final String nameCallbackDataPremium;


    MuscleGroupButton(String nameGroup, String nameCallbackData, String nameCallbackDataPremium) {
        this.nameGroup = nameGroup;
        this.nameCallbackData = nameCallbackData;
        this.nameCallbackDataPremium = nameCallbackDataPremium;
    }

}
