package assignment3starterCode;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

  public static final int CUT_OFF_AGE = 10;

  private HashMap<ByteArrayWrapper, BlockNode> blockChain;
  private BlockNode maxHeightNode;
  private TransactionPool transactionPool;

  /**
   * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
   * block
   */
  public BlockChain(Block genesisBlock) {
    blockChain = new HashMap<>();
    UTXOPool utxoPool = new UTXOPool();
    addCoinbaseToUtxoPool(genesisBlock.getCoinbase(), utxoPool);
    BlockNode genesisNode = new BlockNode(genesisBlock, null, utxoPool);
    blockChain.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisNode);
    transactionPool = new TransactionPool();
    maxHeightNode = genesisNode;
  }

  /** Get the maximum height block */
  public Block getMaxHeightBlock() {
    return maxHeightNode.getBlock();
  }

  /** Get the UTXOPool for mining a new block on top of max height block */
  public UTXOPool getMaxHeightUTXOPool() {
    return maxHeightNode.getUtxoPool();
  }

  /** Get the transaction pool to mine a new block */
  public TransactionPool getTransactionPool() {
    // If there are multiple blocks at the same height, return the oldest block in getMaxHeightBlock() function.
    return transactionPool;
  }

  /**
   * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
   * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
   *
   * <p>
   * For example, you can try creating a new block over the genesis block (block height 2) if the
   * block chain height is {@code <=
   * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
   * at height 2.
   *
   * @return true if block is successfully added
   */
  public boolean addBlock(Block block) {
    // No null or genesis (without previous hash) blocks allowed.
    if (block == null || block.getPrevBlockHash() == null) {
      return false;
    }
    BlockNode parentNode = blockChain.get(new ByteArrayWrapper(block.getPrevBlockHash()));
    if (parentNode == null) {
      return false;
    }

    // All transactions should be valid.
    TxHandler handler = new TxHandler(parentNode.getUtxoPool());
    Transaction[] blockTxs = block.getTransactions().toArray(new Transaction[0]);
    if (handler.handleTxs(blockTxs).length != blockTxs.length) {
      return false;
    }

    int newHeight = parentNode.height + 1;
    if (newHeight <= maxHeightNode.height - CUT_OFF_AGE) {
      return false;
    }

    // Add block to the blockchain.
    UTXOPool utxoPool = handler.getUTXOPool();
    addCoinbaseToUtxoPool(block.getCoinbase(), utxoPool);
    BlockNode node = new BlockNode(block, parentNode, utxoPool);
    blockChain.put(new ByteArrayWrapper(block.getHash()), node);

    if (newHeight > maxHeightNode.height) {
      maxHeightNode = node;
    }
    return true;
  }

  /** Add a transaction to the transaction pool */
  public void addTransaction(Transaction tx) {
    transactionPool.addTransaction(tx);
  }

  private void addCoinbaseToUtxoPool(Transaction coinbaseTx, UTXOPool utxoPool) {
    for (int i = 0; i < coinbaseTx.numOutputs(); i++) {
      Transaction.Output out = coinbaseTx.getOutput(i);
      UTXO utxo = new UTXO(coinbaseTx.getHash(), i);
      utxoPool.addUTXO(utxo, out);
    }
  }

  private static class BlockNode {
    private Block block;
    private ArrayList<BlockNode> children;
    private int height;
    private UTXOPool utxoPool;

    BlockNode(Block block, BlockNode parent, UTXOPool utxoPool) {
      this.block = block;
      this.utxoPool = utxoPool;
      this.children = new ArrayList<>();

      if (parent != null) {
        height = parent.height + 1;
        parent.addChild(this);
      } else {
        height = 1;
      }
    }


    /** Returns utxo pool from which to add blocks on top of. */
    UTXOPool getUtxoPool() {
      return new UTXOPool(utxoPool);
    }

    void addChild(BlockNode node) {
      children.add(node);
    }

    Block getBlock() {
      return block;
    }
  }
}