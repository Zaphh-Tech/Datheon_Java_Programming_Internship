# Task 2: ATM and Account Engine

A banking engine with an ATM front end. Savings and current accounts, PIN
authentication, deposits, withdrawals, transfers, interest, overdrafts and a
full audit trail. The refusals are real exceptions, not return codes.

## Running it

    cd task2-atm-banking
    javac -d out src/banking/*.java
    java -cp out banking.ATM

The machine starts with four demo accounts so there is something to work
against:

| Account | Type | Holder | Opening balance | PIN |
| --- | --- | --- | --- | --- |
| SV-1001 | Savings | Aarav Sharma | 12,500.00 | 2468 |
| CH-2001 | Current | Aarav Sharma | 1,800.00 | 2468 |
| SV-1002 | Savings | Diya Patel | 7,400.00 | 1357 |
| CH-2002 | Current | Diya Patel | 250.00 | 1357 |

Three wrong PINs in a row lock the card, which is a property of the customer,
not of the account, so it locks both of that customer's cards. There is no
unlock on the machine, only `Customer.unlock()` for a branch to call.

## Session menu

    1) Balance
    2) Deposit
    3) Withdraw
    4) Transfer to another account
    5) Mini statement
    6) Full statement
    7) Change PIN
    8) Run month end on this account
    0) Eject card

## Classes

| File | Responsibility |
| --- | --- |
| `Account.java` | Abstract account: number, owner, private balance, transaction log, deposit and withdraw. |
| `SavingsAccount.java` | Earns monthly interest, has to keep a minimum balance on deposit. |
| `CheckingAccount.java` | May be overdrawn up to a limit, charged monthly interest on the overdrawn part. |
| `Customer.java` | Card holder, PIN check and the three strike lock out. |
| `Pin.java` | Salted SHA-256 digest of a PIN, with a constant time comparison. |
| `Bank.java` | Customers, accounts, transfers and the month end run. |
| `Transaction.java` | One immutable audit line: time, type, amount, resulting balance, note. |
| `TransactionLog.java` | Append only history for one account. |
| `Money.java` | Parsing, rounding and formatting of every amount in the system. |
| `StatementPrinter.java` | Balance screens and statements. |
| `ATM.java` | The machine: card, PIN, session menu. |

## Custom exceptions

| Exception | Thrown when |
| --- | --- |
| `InsufficientFundsException` | A withdrawal or transfer exceeds what the account may release. Carries the requested amount, the available amount and the shortfall. |
| `InvalidPINException` | The PIN is wrong. Carries the number of attempts left. |
| `CardLockedException` | The card has been locked by three failed attempts. |
| `InvalidAmountException` | The amount is zero, negative, unparseable, or finer than one paisa. |
| `AccountNotFoundException` | The account number does not exist. |

## Decisions worth noting

Balances are `BigDecimal` at two decimal places, never `double`. A `double`
cannot hold 0.10 exactly, and interest postings on a `double` balance drift
away from the ledger after enough of them.

The balance field is private to `Account` and only moves through one protected
`post` method, which writes the audit line in the same step. No subclass can
change a balance without the statement recording it.

`withdraw` checks against `spendableBalance()` rather than the balance.
A savings account reports its balance minus the minimum balance, a current
account reports its balance plus the overdraft limit. That one method is the
whole difference in withdrawal behaviour between the two account types.

`monthEnd()` is abstract: savings credits interest, current charges overdraft
interest. The ATM calls `account.monthEnd()` without knowing which it is
holding.

The two halves of a transfer, `transferOut` and `transferIn`, are package
private. A transfer has to go through `Bank.transfer`, so nothing outside the
package can post one leg and not the other. The debit goes first because it is
the leg that can be refused: it either posts in full or throws before any
balance has moved.

PINs are stored as a salted SHA-256 digest, so nothing in the program can read
one back out and a printed `Customer` cannot leak one. The comparison uses
`MessageDigest.isEqual` rather than `String.equals`.

The card list on the welcome screen shows only account numbers and types.
Holder names and balances appear after the PIN, not before.

## Sample output

Savings withdrawal refused because of the minimum balance, with the amount that
would have worked:

    Amount to withdraw: 12200
    Rejected: account SV-1001 cannot release INR 12,200.00, only INR 12,000.00 is available (short by INR 200.00)
    Try INR 12,000.00 or less.

A statement after a withdrawal and a transfer:

    WHEN                 TYPE                     AMOUNT          BALANCE  DETAIL
    2026-10-28 19:25:10  Opening balance   INR 12,500.00    INR 12,500.00  account opened
    2026-10-28 19:25:10  Withdrawal        INR -2,000.00    INR 10,500.00  cash withdrawal at the machine
    2026-10-28 19:25:10  Transfer out        INR -500.00    INR 10,000.00  to CH-2001 Aarav Sharma

A current account taken into overdraft, then charged for it at month end:

    Amount to withdraw: 1000
    Dispensed INR 1,000.00. Balance is now INR -750.00.
    Month end: charged INR 9.38 overdraft interest on CH-2002

    WHEN                 TYPE                     AMOUNT          BALANCE  DETAIL
    2026-10-28 19:18:35  Opening balance      INR 250.00       INR 250.00  account opened
    2026-10-28 19:18:35  Withdrawal        INR -1,000.00      INR -750.00  cash withdrawal at the machine
    2026-10-28 19:18:35  Fee                   INR -9.38      INR -759.38  overdraft charge at 1.25% a month

Three wrong PINs:

    PIN: incorrect PIN, 2 attempt(s) left before the card is retained
    PIN: incorrect PIN, 1 attempt(s) left before the card is retained
    PIN: Card retained: the card for Diya Patel is locked, the branch has to reset it

## Rates used

Savings interest 4.50% a year, posted monthly as one twelfth of the annual
rate. Minimum balance 500.00. Overdraft limit 2,000.00 by default and 5,000.00
on CH-2002, charged at 1.25% a month on the overdrawn amount. The division is
carried at six decimal places and only the posted figure is rounded to paisa,
so twelve monthly postings do not round in the bank's favour every time.
