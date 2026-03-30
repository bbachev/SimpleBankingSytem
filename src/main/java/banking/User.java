package banking;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class User {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final CopyOnWriteArrayList<BankAccount> accounts;


    public User(UUID id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new CopyOnWriteArrayList<>();
    }
    void addAccount(BankAccount account){
        this.accounts.add(account);
    }
    void removeAccount(BankAccount account){
        this.accounts.remove(account);
    }

    protected List<BankAccount> getAccounts(){
        return Collections.unmodifiableList(this.accounts);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
