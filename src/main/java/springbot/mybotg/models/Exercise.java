package springbot.mybotg.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "exercises")
public class Exercise {

    // Геттеры и сеттеры
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "muscle_group", nullable = false)
    private String muscleGroup;

    @Column(name = "exercise", nullable = false)
    private String exercise;


}
