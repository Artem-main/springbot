package springbot.mybotg.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "name_exercise")
    private String nameExercise;

    @Column(name = "weight_exercise")
    private int weightExercise;

    @Column (name = "muscle_Group")
    private String muscleGroup;


    public Account(long chatId, String name, String muscleGroup, String nameExercise, int weightExercise, String accountStatus) {
        this.chatId = chatId;
        this.name = name;
        this.nameExercise = nameExercise;
        this.muscleGroup = muscleGroup;
        this.weightExercise = weightExercise;
        this.accountStatus = accountStatus;
    }

    public Account(Long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public Account() {
    }
}
