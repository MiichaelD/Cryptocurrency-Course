package assignment1starterCode;

import com.sun.istack.internal.NotNull;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {

  /** Hash of the transaction, its unique id. */
  private byte[] hash;
  private ArrayList<Input> inputs;
  private ArrayList<Output> outputs;

  public Transaction() {
    inputs = new ArrayList();
    outputs = new ArrayList();
  }
  public Transaction(@NotNull Transaction tx) {
    hash = tx.hash.clone();
    inputs = new ArrayList(tx.inputs);
    outputs = new ArrayList(tx.outputs);
  }

  public void addInput(byte[] prevTxHash, int outputIndex) {
    Input in = new Input(prevTxHash, outputIndex);
    inputs.add(in);
  }

  public void addOutput(double value, PublicKey address) {
    Output op = new Output(value, address);
    outputs.add(op);
  }

  public void removeInput(int index) {
    inputs.remove(index);
  }

  public void removeInput(UTXO ut) {
    for (int i = 0; i < inputs.size(); i++) {
      Input in = inputs.get(i);
      UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
      if (u.equals(ut)) {
        inputs.remove(i);
        return;
      }
    }
  }

  public void addSignature(byte[] signature, int index) {
    inputs.get(index).addSignature(signature);
  }

  /**
   * Returns a byte array containing Input's previous transaction' s hash, output index, all output' s transaction's
   * value and address.
   */
  public byte[] getRawDataToSign(int index) {
    // ith input and all outputs
    ArrayList<Byte> sigData = new ArrayList<Byte>();
    if (index >= inputs.size())
      return null;
    Input in = inputs.get(index);
    byte[] prevTxHash = in.prevTxHash;
    ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
    b.putInt(in.outputIndex);
    byte[] outputIndex = b.array();
    if (prevTxHash != null)
      for (int i = 0; i < prevTxHash.length; i++)
        sigData.add(prevTxHash[i]);
    for (int i = 0; i < outputIndex.length; i++)
      sigData.add(outputIndex[i]);
    addOutputsBytesToByteList(sigData);
    byte[] sigD = new byte[sigData.size()];
    int i = 0;
    for (Byte sb : sigData)
      sigD[i++] = sb;
    return sigD;
  }

  public byte[] getRawTx() {
    ArrayList<Byte> rawTx = new ArrayList<Byte>();
    addInputsBytesToByteList(rawTx);
    addOutputsBytesToByteList(rawTx);
    byte[] tx = new byte[rawTx.size()];
    int i = 0;
    for (Byte b : rawTx)
      tx[i++] = b;
    return tx;
  }

  public void finalize() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(getRawTx());
      hash = md.digest();
    } catch (NoSuchAlgorithmException x) {
      x.printStackTrace(System.err);
    }
  }

  private void addInputsBytesToByteList(ArrayList<Byte> list) {
    for (Input in : inputs) {
      byte[] prevTxHash = in.prevTxHash;
      ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
      b.putInt(in.outputIndex);
      byte[] outputIndex = b.array();
      byte[] signature = in.signature;
      if (prevTxHash != null) {
        for (int i = 0; i < prevTxHash.length; i++)
          list.add(prevTxHash[i]);
      }

      for (int i = 0; i < outputIndex.length; i++)
        list.add(outputIndex[i]);

      if (signature != null) {
        for (int i = 0; i < signature.length; i++)
          list.add(signature[i]);
      }
    }
  }

  private void addOutputsBytesToByteList(ArrayList<Byte> list) {
    for (Output op : outputs) {
      ByteBuffer b = ByteBuffer.allocate(Double.BYTES);
      b.putDouble(op.value);
      byte[] value = b.array();
      byte[] addressBytes = op.address.getEncoded();
      for (int i = 0; i < value.length; i++) {
        list.add(value[i]);
      }
      for (int i = 0; i < addressBytes.length; i++) {
        list.add(addressBytes[i]);
      }
    }
  }

  public byte[] getHash() {
    return hash;
  }

  public void setHash(byte[] h) {
    hash = h;
  }

  public ArrayList<Input> getInputs() {
    return inputs;
  }

  public ArrayList<Output> getOutputs() {
    return outputs;
  }

  public Input getInput(int index) {
    if (index < inputs.size()) {
      return inputs.get(index);
    }
    return null;
  }

  public Output getOutput(int index) {
    if (index < outputs.size()) {
      return outputs.get(index);
    }
    return null;
  }

  public int numInputs() {
    return inputs.size();
  }

  public int numOutputs() {
    return outputs.size();
  }

  public static class Input {

    /** Hash of the Transaction whose output is being used. */
    public byte[] prevTxHash;

    /** Used output's index in the previous transaction. */
    public int outputIndex;

    /** The signature produced to check validity. */
    public byte[] signature;

    public Input(byte[] prevHash, int index) {
      if (prevHash != null)
        prevTxHash = Arrays.copyOf(prevHash, prevHash.length);
      outputIndex = index;
    }

    public void addSignature(byte[] sig) {
      if (sig != null)
        signature = Arrays.copyOf(sig, sig.length);
    }
  }

  public static class Output {

    /** Value in BitCoins of the output. */
    public double value;

    /** The address or public key of the recipient. */
    public PublicKey address;

    public Output(double v, PublicKey addr) {
      value = v;
      address = addr;
    }
  }
}
