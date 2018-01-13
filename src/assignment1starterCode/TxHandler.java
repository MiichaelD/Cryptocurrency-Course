package assignment1starterCode;

import java.util.HashSet;
import java.util.LinkedList;

public class TxHandler {

  private final UTXOPool utxoPool;

  /**
   * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
   * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
   * constructor.
   */
  public TxHandler(UTXOPool utxoPool) {
    this.utxoPool = new UTXOPool(utxoPool);
  }

  public boolean isValidTx(Transaction tx) {
    return isValidTx(tx, utxoPool);
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
  public static boolean isValidTx(Transaction tx, UTXOPool utxoPool) {
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

  /**
   * Handles each epoch by receiving an unordered array of proposed transactions, checking each
   * transaction for correctness, returning a mutually valid array of accepted transactions, and
   * updating the current UTXO pool as appropriate.
   */
  public Transaction[] handleTxs(Transaction[] possibleTxs) {
    LinkedList<Transaction> validTxs = new LinkedList();
    for (Transaction tx : possibleTxs) {
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
