package banking;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ATM front end for the banking engine.
 *
 * The flow is the real one: pick a card, prove the PIN, then work inside a
 * session that is bound to a single account. Every refusal the engine can make
 * surfaces here as its own exception, which is why the handlers below read as
 * specific messages instead of one generic "transaction failed".
 *
 * Usage:
 *   java banking.ATM
 */
public class ATM {

    private static final int MINI_STATEMENT_LINES = 5;

    private final Bank bank;
    private final Scanner input = new Scanner(System.in);

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public static void main(String[] args) {
        new ATM(demoBank()).run();
    }

    public void run() {
        System.out.println(bank.getName() + " cash machine");
        System.out.println("Demo cards are listed below, their PINs are in the README.");
        while (true) {
            StatementPrinter.printAccountList(bank);
            String number = readLine("Account number (blank to quit): ");
            if (number == null || number.isBlank()) {
                System.out.println("Session ended.");
                return;
            }
            try {
                Account account = bank.account(number);
                if (authenticate(account.getOwner())) {
                    session(account);
                }
            } catch (AccountNotFoundException ex) {
                System.out.println("Rejected: " + ex.getMessage());
            }
        }
    }

    /**
     * Three tries, then the card is locked by Customer itself. Returning false
     * rather than throwing keeps the machine running for the next customer.
     */
    private boolean authenticate(Customer customer) {
        while (true) {
            String pin = readLine("PIN: ");
            if (pin == null) {
                return false;
            }
            try {
                customer.authenticate(pin.trim());
                System.out.println("Welcome, " + customer.getName() + ".");
                return true;
            } catch (InvalidPINException ex) {
                System.out.println(ex.getMessage());
            } catch (CardLockedException ex) {
                System.out.println("Card retained: " + ex.getMessage());
                return false;
            }
        }
    }

    private void session(Account account) {
        while (true) {
            printSessionMenu(account);
            String choice = readLine("Choice: ");
            if (choice == null) {
                System.out.println();
                System.out.println("Input ended, card returned.");
                return;
            }
            switch (choice.trim()) {
                case "1" -> StatementPrinter.printBalance(account);
                case "2" -> deposit(account);
                case "3" -> withdraw(account);
                case "4" -> transfer(account);
                case "5" -> StatementPrinter.printStatement(account,
                        account.getLog().recent(MINI_STATEMENT_LINES), "Mini statement");
                case "6" -> StatementPrinter.printStatement(account,
                        account.getLog().getEntries(), "Full statement");
                case "7" -> changePin(account.getOwner());
                case "8" -> System.out.println("Month end: " + account.monthEnd());
                case "0" -> {
                    System.out.println("Card returned. Balance " + Money.format(account.getBalance()) + ".");
                    return;
                }
                case "" -> { }
                default -> System.out.println("Unknown option: " + choice);
            }
        }
    }

    private void printSessionMenu(Account account) {
        System.out.println();
        System.out.printf("%s / %s, available %s%n", account.getNumber(), account.kind(),
                Money.format(account.spendableBalance()));
        System.out.println("1) Balance");
        System.out.println("2) Deposit");
        System.out.println("3) Withdraw");
        System.out.println("4) Transfer to another account");
        System.out.println("5) Mini statement");
        System.out.println("6) Full statement");
        System.out.println("7) Change PIN");
        System.out.println("8) Run month end on this account");
        System.out.println("0) Eject card");
    }

    private void deposit(Account account) {
        BigDecimal amount = readAmount("Amount to deposit: ");
        if (amount == null) {
            return;
        }
        try {
            account.deposit(amount, "cash deposit at the machine");
            System.out.printf("Deposited %s. Balance is now %s.%n",
                    Money.format(amount), Money.format(account.getBalance()));
        } catch (InvalidAmountException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void withdraw(Account account) {
        BigDecimal amount = readAmount("Amount to withdraw: ");
        if (amount == null) {
            return;
        }
        try {
            account.withdraw(amount, "cash withdrawal at the machine");
            System.out.printf("Dispensed %s. Balance is now %s.%n",
                    Money.format(amount), Money.format(account.getBalance()));
        } catch (InsufficientFundsException ex) {
            System.out.println("Rejected: " + ex.getMessage());
            System.out.println("Try " + Money.format(ex.getAvailable()) + " or less.");
        } catch (InvalidAmountException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void transfer(Account from) {
        String destination = readLine("Destination account number: ");
        if (destination == null || destination.isBlank()) {
            return;
        }
        BigDecimal amount = readAmount("Amount to transfer: ");
        if (amount == null) {
            return;
        }
        try {
            Account to = bank.account(destination);
            bank.transfer(from, to, amount);
            System.out.printf("Sent %s to %s. Balance is now %s.%n",
                    Money.format(amount), to.getNumber(), Money.format(from.getBalance()));
        } catch (AccountNotFoundException | InvalidAmountException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        } catch (InsufficientFundsException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    private void changePin(Customer customer) {
        String current = readLine("Current PIN: ");
        String replacement = readLine("New PIN: ");
        if (current == null || replacement == null) {
            return;
        }
        try {
            customer.changePin(current.trim(), replacement.trim());
            System.out.println("PIN changed.");
        } catch (InvalidPINException | CardLockedException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }

    /** Demo books, so the machine has something to run against. */
    private static Bank demoBank() {
        Bank bank = new Bank("Datheon Community Bank");
        Customer aarav = bank.addCustomer(new Customer("C-001", "Aarav Sharma", "2468"));
        Customer diya = bank.addCustomer(new Customer("C-002", "Diya Patel", "1357"));

        bank.open(new SavingsAccount("SV-1001", aarav, Money.of("12500.00")));
        bank.open(new CheckingAccount("CH-2001", aarav, Money.of("1800.00")));
        bank.open(new SavingsAccount("SV-1002", diya, Money.of("7400.00")));
        bank.open(new CheckingAccount("CH-2002", diya, Money.of("250.00"), Money.of("5000.00")));
        return bank;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return input.nextLine();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /** Returns null when input ends, which the caller treats as cancel. */
    private BigDecimal readAmount(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (line == null || line.isBlank()) {
                return null;
            }
            try {
                return Money.parse(line);
            } catch (InvalidAmountException ex) {
                System.out.println("Rejected: " + ex.getMessage());
            }
        }
    }
}
