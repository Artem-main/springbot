package springbot.mybotg.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account_premium")
public class AccountPremium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_exercise")
    private String nameExercise;

    @Column(name = "weight_exercise")
    private int weightExercise;

    @Column (name = "muscle_Group")
    private String muscleGroup;

    @Column (name = "number_sets_and_repeat")
    private String numberSetsAndRepeat;

    public AccountPremium(Long id, Long chatId, String name, String nameExercise, int weightExercise, String muscleGroup, String numberSetsAndRepeat) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
        this.nameExercise = nameExercise;
        this.weightExercise = weightExercise;
        this.muscleGroup = muscleGroup;
        this.numberSetsAndRepeat = numberSetsAndRepeat;
    }

    public AccountPremium(Long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public AccountPremium () {}

}
