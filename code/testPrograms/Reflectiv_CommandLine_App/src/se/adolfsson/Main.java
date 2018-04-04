package se.adolfsson;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    System.out.println("Reflectiv CommandLine App Starting...");
    System.out.println();

    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter reflective content: ");
    String reflect = scanner.next();

    System.out.println(reflect);

    System.out.println();
    System.out.println("Reflectiv CommandLine App Ending");
  }
}
