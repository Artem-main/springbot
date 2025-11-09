package springbot.mybotg.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import springbot.mybotg.models.AccountPremium;

import java.util.List;
import java.util.Optional;

public interface AccountPremiumRepository extends JpaRepository<AccountPremium, Long> {

    @NotNull
    Optional <AccountPremium> findById (@NotNull Long chatId);

    @Query("SELECT a.accountStatus FROM Account a WHERE a.chatId = :chatId")
    Optional <String> checkAccountPremium (@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE AccountPremium a SET a.weightExercise = :weight WHERE a.chatId = :chatId AND a.nameExercise = :nameExercise")
    void setWeightExercise (@Param("chatId") Long chatId, @Param ("weight") int weight, @Param("nameExercise") String nameExercise);

    @Query("SELECT a.weightExercise FROM AccountPremium a WHERE a.chatId = :chatId AND a.nameExercise = :nameExercise")
    Optional<Integer>  getWeightExercise (@Param("chatId") Long chatId, @Param("nameExercise") String nameExercise);

    Optional<AccountPremium> findByChatIdAndMuscleGroupAndNameExercise(Long chatId, String muscleGroup, String nameExercise);

    @Query("SELECT a.nameExercise FROM AccountPremium a WHERE a.chatId = :chatId AND a.muscleGroup = :muscleGroup")
    List<String> viewAllExerciseThisUser (@Param("chatId") Long chatId, @Param("muscleGroup") String muscleGroup);
}
