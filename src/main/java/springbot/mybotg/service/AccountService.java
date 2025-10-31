package springbot.mybotg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbot.mybotg.models.Account;
import springbot.mybotg.repository.AccountRepository;

import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public void createNewRecord(Long chatId, String name, String muscleGroup, String nameExercise) {
        Account record = new Account();
        record.setChatId(chatId);
        record.setName(name);
        record.setMuscleGroup(muscleGroup);
        record.setNameExercise(nameExercise);
        accountRepository.save(record);
    }

    public String getUserText(Long chatId) {
        return accountRepository.getUserText(chatId);
    }

    public Optional<Account> findById (Long chatId) {
        return accountRepository.findById(chatId);
    }

    public void setNameExercise(Long chatId, String text, String muscleGroup) {
        accountRepository.setNameExercise(chatId, text, muscleGroup);
    }

    public void saveAccount(Long chatId, String name) {
        if (!accountRepository.existsById(chatId)) {
            Account account = new Account(chatId, name);
            accountRepository.save(account);
        }
    }

    public void saveOrUpdateExercise(Long chatId, String name, String muscleGroup, String nameExercise) {
        Optional<Account> existing = accountRepository
                .findByChatIdAndMuscleGroupAndNameExercise(chatId, muscleGroup, nameExercise);

        if (existing.isPresent()) {
            // Обновляем поля при необходимости
            Account record = existing.get();
            record.setName(name); // например, обновляем имя
            accountRepository.save(record);
        } else {
            // Создаём новую запись
            Account newRecord = new Account();
            newRecord.setChatId(chatId);
            newRecord.setName(name);
            newRecord.setMuscleGroup(muscleGroup);
            newRecord.setNameExercise(nameExercise);
            accountRepository.save(newRecord);
        }
    }


    public String checkAccountPremium (Long chatId) {
        return accountRepository.checkAccountPremium(chatId);
    }
}



