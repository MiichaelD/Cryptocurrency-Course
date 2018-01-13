package com.coursera.cryptocurrencies;

import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Main {

  public static void main(String[] args) {
    try {
      System.out.println("com.coursera.cryptocurrencies.Main:");
      assignment1starterCode.Main1.main(args);
      System.out.println("\n\ncom.coursera.cryptocurrencies.Main:");
      assignment3starterCode.Main3.main(args);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (SignatureException e) {
      e.printStackTrace();
    }
  }
}