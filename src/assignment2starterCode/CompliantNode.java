package assignment2starterCode;

import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_tXDistribution;
    private int numRounds;
    private boolean[] followees;
    private boolean[] blackListed;
    private Set<Transaction> pendingTransactions;

    public CompliantNode(
        double p_graph,
        double p_malicious,
        double p_txDistribution,
        int numRounds) {
      this.p_graph = p_graph;
      this.p_malicious = p_malicious;
      this.p_tXDistribution = p_txDistribution;
      this.numRounds = numRounds;
      pendingTransactions = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
      this.followees = followees;
      this.blackListed = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
      this.pendingTransactions.addAll(pendingTransactions);
    }

    /** Returns a set of pending transactions to be sent to this node's followers. */
    public Set<Transaction> sendToFollowers() {
        Set<Transaction> toSend = pendingTransactions;
        pendingTransactions = new HashSet<>();
        return toSend;
    }

    /**
     * Checks if received set of candidate transactions come from known followees and adds them to
     * the pending transaction set.
     */
    public void receiveFromFollowees(Set<Candidate> candidates) {
        Set<Integer> senders = new HashSet<>();
        for (Candidate candidate : candidates) {
            senders.add(candidate.sender);
        }

        for (int i = 0; i < followees.length; ++i) {
            if (followees[i] && !senders.contains(i))
                blackListed[i] = true;
        }
        for (Candidate c : candidates) {
            if (!blackListed[c.sender]) {
                pendingTransactions.add(c.tx);
            }
        }
    }
}
