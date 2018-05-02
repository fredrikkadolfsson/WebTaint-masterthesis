package se.adolfsson;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		System.out.println("Reflective CommandLine App Starting...");
		System.out.println();

		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter reflective content: ");
		//String reflect = scanner.next();

		//System.out.println(reflect.replaceAll("[^\\d.]", ""));
		//System.out.println(reflect); // Will crash here!!!

		System.out.println();
		System.out.println("Reflective CommandLine App Ending");
	}
}
