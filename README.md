Design a small system that models bank accounts.

Requirements – Part 1 (Core)
1. Create a BankAccount class with:
    - owner (string)
    - balance (number, starts at 0)

2. Add methods:
    - deposit(amount) → increases balance
    - withdraw(amount) → decreases balance (reject if insufficient funds)
    - getBalance() → returns current balance

3. Create a SavingsAccount class that:
    - extends BankAccount
    - has an additional interestRate
    - adds a method applyInterest() that increases balance based on the rate

4. Ensure:
    - Negative deposits are not allowed
    - Withdrawals cannot exceed balance

Bonus:
- Make balance private
- Add a transferTo(otherAccount, amount) method
- Log every operation in a transaction history

What this focuses on:
- Classes & constructors
- Inheritance (extends, super)
- Encapsulation
- Method design & validation
- Data modeling

--------------------------------------------------
Part 2 – Advanced Requirements
--------------------------------------------------

5. Add a Bank class that:
    - Manages multiple accounts (e.g. stored by unique accountNumber).
    - openAccount(owner, type, …) → creates and returns a new account.
    - closeAccount(accountNumber) → removes an existing account.
    - findAccount(accountNumber) → returns the matching account or indicates if it does not exist.
    - listAccounts() → returns all currently open accounts.

6. Add more account types:
    - Create a CheckingAccount class that extends BankAccount and adds:
        - overdraftLimit (number, can be 0 or positive).
        - Overdraft rule: balance may go below 0 but not below -overdraftLimit.
    - SavingsAccount must not allow overdraft at all (any withdrawal that would make balance negative must be rejected).

7. Introduce a Transaction class:
    - Fields: timestamp, type (deposit, withdraw, transfer, interest, fee), amount, fromAccount (optional), toAccount (optional), resultingBalance.
    - Each operation (deposit, withdraw, transferTo, applyInterest, any bank fee) must create and store a Transaction object in the account’s transaction history.

8. Add system rules and limits:
    - Implement a dailyWithdrawalLimit per account (configurable).
    - Track how much has been withdrawn in the current day and reject withdrawals that would exceed the limit.
    - SavingsAccount may have a maximumBalance limit; deposits or interest that would exceed this limit must be rejected.

9. Interest and fees:
    - Extend applyInterest() to support a compoundingMode (e.g. "monthly" vs "yearly") and adjust the calculation accordingly.
    - The Bank may charge:
        - A per-withdrawal fee.
        - An overdraft fee whenever a CheckingAccount balance goes below 0 (define the exact rule, e.g. first time per day).
    - All fees must be recorded as Transaction objects and applied to the account balance.

10. Error handling and encapsulation:
    - Keep balance private and only modify it through methods that enforce the rules.
    - Define and use clear error/exception types (or error codes) for:
        - Invalid amount (negative or zero).
        - Insufficient funds.
        - Overdraft limit exceeded.
        - Daily limit exceeded.
        - Unknown or closed account.
    - Public methods should validate inputs and either throw/return meaningful errors or safely reject the operation.

11. Optional extra:
    - Add a Customer (or User) class with id, name, and a list of accounts.
    - Link each BankAccount to a Customer instead of just an owner string.
    - Add generateStatement(accountNumber, fromDate, toDate) that returns all Transactions in that period plus starting and ending balance.