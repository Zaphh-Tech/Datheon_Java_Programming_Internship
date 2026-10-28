package library;

/** Thrown when a member is already holding as many books as the policy allows. */
public class LoanLimitException extends Exception {

    public LoanLimitException(Member member, int limit) {
        super(member.getName() + " already has " + member.activeLoans().size()
                + " book(s) out, the limit is " + limit);
    }
}
