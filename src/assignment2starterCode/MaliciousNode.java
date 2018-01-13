package assignment2starterCode;

import java.util.Set;
import java.util.HashSet;

public class MaliciousNode implements Node {

    public MaliciousNode(
        double p_graph,
        double p_malicious,
        double p_txDistribution,
        int numRounds) { }

    public void setFollowees(boolean[] followees) { }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) { }

    public Set<Transaction> sendToFollowers() {
        return new HashSet<>();
    }

    public void receiveFromFollowees(Set<Candidate> candidates) { }
}
