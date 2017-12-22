package assignment1starterCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

public class MaxFeeTxHandler {

  private final UTXOPool utxoPool;

  /**
   * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
   * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
   * constructor.
   */
  public MaxFeeTxHandler(UTXOPool utxoPool) {
    this.utxoPool = new UTXOPool(utxoPool);
  }

  /**
   * @return true if:
   * (1) all outputs claimed by {@code tx} (its inputs) are in the current UTXO pool,
   * (2) the signatures on each input of {@code tx} are valid,
   * (3) no UTXO is claimed multiple times by {@code tx},
   * (4) all of {@code tx}s output values are non-negative, and
   * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
   * values; and false otherwise.
   */
  public boolean isValidTx(Transaction tx) {
    double accumulatedSum = 0;
    HashSet<UTXO> claimedUtxos = new HashSet();
    for (int index = 0; index < tx.numInputs(); ++index) {
      Transaction.Input input = tx.getInput(index);
      UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
      Transaction.Output originOutput = utxoPool.getTxOutput(utxo);
      if (originOutput == null) // Check if transaction exists in current UTXO pool.
        return false;

      if (!Crypto.verifySignature(originOutput.address, tx.getRawDataToSign(index), input.signature))
        return false;

      if (claimedUtxos.contains(utxo)) // Check if this input has already been consumed within this transaction.
        return false;

      claimedUtxos.add(utxo);
      accumulatedSum += originOutput.value;
    }

    for (Transaction.Output output : tx.getOutputs()) {
      if (output.value < 0)
        return false;
      accumulatedSum -= output.value;
    }
    return accumulatedSum >= 0;
  }

  /** Calculates a transaction fee based */
  private double calculateTransactionFees(Transaction tx) {
    double accumulatedSum = 0;
    for (Transaction.Input in : tx.getInputs()) {
      UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
      if (!utxoPool.contains(utxo) || !isValidTx(tx)) {
        continue;
      }
      Transaction.Output originOutput = utxoPool.getTxOutput(utxo);
      accumulatedSum += originOutput.value;
    }
    for (Transaction.Output out : tx.getOutputs()) {
      accumulatedSum -= out.value;
    }
    return accumulatedSum;
  }

  /**
   * Handles each epoch by receiving an unordered array of proposed transactions, checking each
   * transaction for correctness, returning a mutually valid array of accepted transactions, and
   * updating the current UTXO pool as appropriate.
   */
  public Transaction[] handleTxs(Transaction[] possibleTxs) {
    TreeSet<Transaction> txsSortedByFees = new TreeSet<>((tx1, tx2) -> {
      double tx1Fees = calculateTransactionFees(tx1);
      double tx2Fees = calculateTransactionFees(tx2);
//      return tx1Fees < tx2Fees ? 1 : -1;
      return Double.valueOf(tx2Fees).compareTo(tx1Fees);
    });
    Collections.addAll(txsSortedByFees, possibleTxs);

    HashSet<Transaction> validTxs = new HashSet();
    for (Transaction tx : txsSortedByFees) {
      if (isValidTx(tx)) {
        validTxs.add(tx);
        // Remove consumed transaction inputs.
        for (Transaction.Input input : tx.getInputs()) {
          UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
          utxoPool.removeUTXO(utxo);
        }

        // Add newly generated output transactions.
        for (int index = 0; index < tx.numOutputs(); ++index) {
          Transaction.Output output = tx.getOutput(index);
          UTXO utxo = new UTXO(tx.getHash(), index);
          utxoPool.addUTXO(utxo, output);
        }
      }
    }

    return validTxs.toArray(new Transaction[validTxs.size()]);
  }
}
