package com.glotrush.services.streak;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.constants.StreakConstants;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.entities.Accounts;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StreakService implements IStreakService {

    private final AccountsRepository accountsRepository;
    private final MessageSource messageSource;
    private final NotificationDispatcher notificationDispatcher;

    @Transactional
    @Override
    public void updateStreakForUser(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId)
            .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        LocalDate now = LocalDate.now();

        if (now.equals(account.getLastActivityDate())) {
            return;
        }

        if (account.getLastActivityDate() != null && account.getLastActivityDate().equals(now.minusDays(1))) {
            account.setCurrentStreak(account.getCurrentStreak() + 1);
        } else {
            account.setCurrentStreak(1);
        }

        account.setLastActivityDate(now);

        if (account.getCurrentStreak() > account.getLongestStreak()) {
            account.setLongestStreak(account.getCurrentStreak());
        }

        accountsRepository.save(account);

        int streak = account.getCurrentStreak();
        if (streak == StreakConstants.SEVEN_DAYS || streak == StreakConstants.THIRTY_DAYS || streak == StreakConstants.ONE_HUNDRED_DAYS) {
            notificationDispatcher.sendStreakMilestone(account, streak);
        }
    }
}
