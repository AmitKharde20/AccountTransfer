package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferRequest;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class AccountsService extends Thread{

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final NotificationService notificationService;

    @Autowired
    private TransferValidator transferValidator;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void run(TransferRequest transferRequest){
        this.makeTransfer(transferRequest);
    }

    public void makeTransfer(TransferRequest transferRequest) throws AccountNotFoundException, NotEnoughFundsException, TransferBetweenSameAccountException {

        final Account accountFrom = accountsRepository.getAccount(transferRequest.getAccountFromId());
        final Account accountTo = accountsRepository.getAccount(transferRequest.getAccountToId());
        final BigDecimal amount = transferRequest.getAmount();

        transferValidator.validate(accountFrom, accountTo, transferRequest);

        boolean successful = accountsRepository.updateAccountsBatch(Arrays.asList(
                new Account(accountFrom.getAccountId(), amount.negate()),
                new Account(accountTo.getAccountId(), amount)
                ));

        if (successful){
            notificationService.notifyAboutTransfer(accountFrom, "The account as ID " + accountTo.getAccountId() + " debited with amount of " + transferRequest.getAmount() + ".");
            notificationService.notifyAboutTransfer(accountTo, "Dear account holder, the transaction for your account with ID " + accountFrom.getAccountId() + " has been credited with amount of " + transferRequest.getAmount() + " into your account.");
        }
    }

}
