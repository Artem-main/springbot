package springbot.mybotg.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import springbot.mybotg.models.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {


    @NotNull Optional<Account> findById (@NotNull Long chatId);

    @Query("SELECT a.nameExercise FROM Account a WHERE a.chatId = :chatId")
    String getUserText(@Param("chatId") Long chatId);

    @Query("SELECT a.accountStatus FROM Account a WHERE a.chatId = :chatId")
    String checkAccountPremium (@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.nameExercise = :text WHERE a.chatId = :chatId AND a.muscleGroup = :muscleGroup")
    void setNameExercise(@Param("chatId") Long chatId, @Param("text") String text, @Param("muscleGroup") String muscleGroup);

    @Transactional
    @Query("UPDATE Account a SET a.weightExercise = :weight WHERE a.chatId = :chatId")
    void setWeightExercise (@Param("chatId") Long chatId, @Param ("weight") int weight);


    Optional<Account> findByChatIdAndMuscleGroupAndNameExercise(Long chatId, String muscleGroup, String nameExercise);
}


