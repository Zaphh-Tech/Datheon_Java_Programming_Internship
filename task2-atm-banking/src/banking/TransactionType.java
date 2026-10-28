package banking;

public enum TransactionType {
    OPENING("Opening balance"),
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_IN("Transfer in"),
    TRANSFER_OUT("Transfer out"),
    INTEREST("Interest"),
    FEE("Fee");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
