package banking;

import java.util.List;

/** Account and statement tables, kept out of the ATM flow code. */
public final class StatementPrinter {

    private static final String RULE = "-".repeat(84);

    private StatementPrinter() {
    }

    public static void printBalance(Account account) {
        System.out.println(RULE);
        System.out.printf("Account    : %s (%s)%n", account.getNumber(), account.kind());
        System.out.printf("Holder     : %s%n", account.getOwner().getName());
        System.out.printf("Balance    : %s%n", Money.format(account.getBalance()));
        System.out.printf("Available  : %s%n", Money.format(account.spendableBalance()));
        if (account instanceof SavingsAccount savings) {
            System.out.printf("Minimum    : %s must stay on deposit%n", Money.format(savings.getMinimumBalance()));
            System.out.printf("Interest   : %s%% a year, next posting %s%n",
                    savings.getAnnualRatePercent(), Money.format(savings.monthlyInterest()));
        } else if (account instanceof CheckingAccount current) {
            System.out.printf("Overdraft  : %s limit, %s used%n",
                    Money.format(current.getOverdraftLimit()), Money.format(current.overdrawnAmount()));
        }
        System.out.println(RULE);
    }

    public static void printStatement(Account account, List<Transaction> entries, String heading) {
        System.out.println(RULE);
        System.out.printf("%s for %s (%s)%n", heading, account.getNumber(), account.getOwner().getName());
        System.out.println(RULE);
        System.out.printf("%-19s  %-15s %15s  %15s  %s%n", "WHEN", "TYPE", "AMOUNT", "BALANCE", "DETAIL");
        for (Transaction entry : entries) {
            System.out.printf("%-19s  %-15s %15s  %15s  %s%n", entry.timestamp(), entry.type().getLabel(),
                    Money.format(entry.signedAmount()), Money.format(entry.balanceAfter()),
                    entry.note() == null ? "" : entry.note());
        }
        System.out.println(RULE);
        System.out.printf("%d of %d entries shown.%n", entries.size(), account.getLog().size());
    }

    public static void printAccountList(Bank bank) {
        System.out.println(RULE);
        System.out.printf("%s: accounts on file%n", bank.getName());
        System.out.println(RULE);
        System.out.printf("%-10s %s%n", "NUMBER", "TYPE");
        for (Account account : bank.allAccounts()) {
            // Holder names and balances stay off this screen: it is printed
            // before anyone has entered a PIN.
            System.out.printf("%-10s %s%n", account.getNumber(), account.kind());
        }
        System.out.println(RULE);
    }
}
