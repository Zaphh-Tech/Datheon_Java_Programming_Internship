package banking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The bank's books: its customers, its accounts and the operations that touch
 * more than one account.
 */
public class Bank {

    private final String name;
    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public Bank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Customer addCustomer(Customer customer) {
        if (customers.containsKey(customer.getId())) {
            throw new IllegalArgumentException("customer " + customer.getId() + " already exists");
        }
        customers.put(customer.getId(), customer);
        return customer;
    }

    public <T extends Account> T open(T account) {
        if (accounts.containsKey(account.getNumber())) {
            throw new IllegalArgumentException("account " + account.getNumber() + " already exists");
        }
        if (!customers.containsKey(account.getOwner().getId())) {
            addCustomer(account.getOwner());
        }
        accounts.put(account.getNumber(), account);
        return account;
    }

    public Account account(String number) throws AccountNotFoundException {
        Account account = accounts.get(number == null ? "" : number.trim().toUpperCase());
        if (account == null) {
            throw new AccountNotFoundException(number);
        }
        return account;
    }

    public Collection<Account> allAccounts() {
        return accounts.values();
    }

    public List<Account> accountsOf(Customer customer) {
        List<Account> owned = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.getOwner().getId().equals(customer.getId())) {
                owned.add(account);
            }
        }
        return owned;
    }

    /**
     * Moves money between two accounts. The debit is attempted first: it is the
     * leg that can be refused, and it either posts in full or throws before any
     * balance has moved, so the credit can never be left stranded on its own.
     */
    public void transfer(Account from, Account to, BigDecimal amount)
            throws InvalidAmountException, InsufficientFundsException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("both accounts are required for a transfer");
        }
        if (from.getNumber().equals(to.getNumber())) {
            throw new InvalidAmountException("cannot transfer an account to itself");
        }
        from.transferOut(amount, to);
        to.transferIn(amount, from);
    }

    /** Runs the month end posting on every account and returns what was posted. */
    public List<String> runMonthEnd() {
        List<String> postings = new ArrayList<>();
        for (Account account : accounts.values()) {
            postings.add(account.monthEnd());
        }
        return postings;
    }

    public BigDecimal totalDeposits() {
        BigDecimal total = Money.ZERO;
        for (Account account : accounts.values()) {
            total = total.add(account.getBalance());
        }
        return Money.round(total);
    }
}
