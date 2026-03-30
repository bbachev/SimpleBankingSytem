package banking;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class User {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final CopyOnWriteArrayList<BankAccount> accounts;


    public User(UUID id, String firstName, String lastName, CopyOnWriteArrayList<BankAccount> accounts) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(accounts, user.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, accounts);
    }
}
