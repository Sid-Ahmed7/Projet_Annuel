package com.glotrush.websocket.challenge;

import org.springframework.transaction.support.TransactionSynchronization;

public class ChallengeSynchronization implements TransactionSynchronization {

    private final Runnable commit;

    public ChallengeSynchronization(Runnable commit) {
        this.commit = commit;
    }

    @Override
    public void afterCommit() {
        commit.run();
    }
    
}
