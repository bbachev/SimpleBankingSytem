package banking;

import java.util.List;

public record Statement(long startingBalance, long EndingBalance, List<Transaction> transactions) {
}
