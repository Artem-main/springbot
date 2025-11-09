package springbot.mybotg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbot.mybotg.models.AccountPremium;
import springbot.mybotg.repository.AccountPremiumRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AccountPremiumService {

    @Autowired
    public AccountPremiumRepository accountPremiumRepository;

    public Optional<Integer> getWeightExercise(Long chatId, String nameExercise) {
        return accountPremiumRepository.getWeightExercise(chatId, nameExercise);
    }

    public void setWeightExercise(Long chatId, int weight, String nameExercise) {
        accountPremiumRepository.setWeightExercise(chatId, weight, nameExercise);
    }

    public List<String> AllExercise (Long chatId, String muscleGroup) {
        return accountPremiumRepository.viewAllExerciseThisUser(chatId, muscleGroup);
    }

    public void saveOrUpdateExercise(Long chatId, String name, String muscleGroup, String nameExercise) {
        Optional<AccountPremium> existing = accountPremiumRepository
                .findByChatIdAndMuscleGroupAndNameExercise(chatId, muscleGroup, nameExercise);

        if (existing.isPresent()) {
            // Обновляем поля при необходимости
            AccountPremium record = existing.get();
            record.setName(name); // например, обновляем имя
            accountPremiumRepository.save(record);
        } else {
            // Создаём новую запись
            AccountPremium newRecord = new AccountPremium();
            newRecord.setChatId(chatId);
            newRecord.setName(name);
            newRecord.setMuscleGroup(muscleGroup);
            newRecord.setNameExercise(nameExercise);
            accountPremiumRepository.save(newRecord);
        }
    }

    public Optional<String> checkAccountPremium (Long chatId) {
        return accountPremiumRepository.checkAccountPremium(chatId);
    }
}
