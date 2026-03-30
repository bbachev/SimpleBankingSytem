package banking;

import java.time.LocalDateTime;

public record Transaction (
        LocalDateTime timestamp,
        TransactionType type,
        long amount,
        BankAccount fromAccount,
        BankAccount toAccount,
        long resultingBalance
) {
}
