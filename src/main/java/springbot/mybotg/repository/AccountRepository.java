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

    @NotNull
    @Query("SELECT a FROM Account a WHERE a.id = :chatId")
    Optional<Account> findById(@NotNull @Param("chatId") Long chatId);

    @Query("SELECT a.sendUserText FROM Account a WHERE a.id = :chatId")
    String getUserText(@Param("chatId") Long chatId);

    @Query("SELECT a.accountStatus FROM Account a WHERE a.id = :chatId")
    String checkAccountPremium (@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.sendUserText = :text WHERE a.id = :chatId")
    void updateUserText(@Param("chatId") Long chatId, @Param("text") String text);
}


