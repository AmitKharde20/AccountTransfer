package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferRequest;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferValidatorImpl implements TransferValidator {

    public void validate(final Account currAccountFrom, final Account currAccountTo, final TransferRequest transferRequest)
            throws AccountNotFoundException, NotEnoughFundsException, TransferBetweenSameAccountException{

        if (currAccountFrom == null){
            throw new AccountNotFoundException("Account from which the amount to be debited with ID as " + transferRequest.getAccountFromId() + " not found.");
        }

        if (currAccountTo == null) {
            throw new AccountNotFoundException("Account to which the amount to be transfer with ID as " + transferRequest.getAccountToId() + " not found.");
        }

        if (sameAccount(transferRequest)){
            throw new TransferBetweenSameAccountException("Transfer to same account not allowed.");
        }

        if (!enoughFunds(currAccountFrom, transferRequest.getAmount())){
            throw new NotEnoughFundsException("Not enough funds on account " + currAccountFrom.getAccountId() + " Current available balance is: "+currAccountFrom.getBalance());
        }
    }

    private boolean sameAccount(final TransferRequest transferRequest) {
        return transferRequest.getAccountFromId().equals(transferRequest.getAccountToId());
    }


    private boolean enoughFunds(final Account account, final BigDecimal amount) {
        return account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

}
