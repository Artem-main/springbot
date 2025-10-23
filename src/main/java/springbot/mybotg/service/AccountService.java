package springbot.mybotg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbot.mybotg.models.Account;
import springbot.mybotg.repository.AccountRepository;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public String getUserText(Long chatId) {
        return accountRepository.getUserText(chatId);
    }

    public void saveUserText(Long chatId, String text) {
        accountRepository.updateUserText(chatId, text);
    }

    public void saveAccount(Long chatId, String name) {
        if (!accountRepository.existsById(chatId)) {
            Account account = new Account(chatId, name, "Первый текст");
            accountRepository.save(account);
        }
    }

    public String checkAccountPremium (Long chatId) {
        return accountRepository.checkAccountPremium(chatId);
    }
}



