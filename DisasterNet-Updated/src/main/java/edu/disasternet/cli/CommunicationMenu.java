package edu.disasternet.cli;

import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.models.Communication;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;

import java.util.*;

public class CommunicationMenu {
    private final List<Communication> communications = DataStore.COMMUNICATIONS;

    public CommunicationMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 COMMUNICATION RECORDS", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Communication");
            System.out.println("2. Search Message");
            System.out.println("3. Search Communication ID (Rabin-Karp)");
            System.out.println("4. Search Multiple Keywords");
            System.out.println("5. Detect Repeated Messages");
            System.out.println("6. View Communications");
            System.out.println("7. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> addCommunication(scanner);
                case "2" -> searchMessage(scanner);
                case "3" -> searchId(scanner);
                case "4" -> multiKeywordSearch(scanner);
                case "5" -> repeatedMessages();
                case "6" -> viewCommunications();
                case "7" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addCommunication(Scanner scanner) {
        System.out.print("Communication ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || communications.stream().anyMatch(c -> c.getCommunicationId().equalsIgnoreCase(id))) { System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW)); return; }
        System.out.print("Sender: ");
        String sender = scanner.nextLine();
        System.out.print("Receiver: ");
        String receiver = scanner.nextLine();
        System.out.print("Message: ");
        String message = scanner.nextLine();
        System.out.print("Priority: ");
        String priority = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        if (sender.isBlank() || receiver.isBlank() || message.isBlank() || !Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(priority)) {
            System.out.println(ConsoleColors.color("[WARNING] Sender, receiver and message are required; priority must be LOW, MEDIUM, HIGH or CRITICAL.", ConsoleColors.YELLOW));
            return;
        }
        communications.add(new Communication(id, sender, receiver, message, priority));
        System.out.println(ConsoleColors.color("[GREEN] Communication record added.", ConsoleColors.GREEN));
    }

    private void searchMessage(Scanner scanner) {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] Search term cannot be empty.", ConsoleColors.YELLOW)); return; }
        System.out.println(ConsoleColors.color("\n=== MESSAGE SEARCH ===", ConsoleColors.CYAN));
        int count = 0;
        for (Communication c : communications) if (!StringAlgorithms.kmp(c.getMessage().toLowerCase(Locale.ROOT), keyword.toLowerCase(Locale.ROOT)).matches().isEmpty()) {
            System.out.println(c.getCommunicationId() + " | " + c.getPriority() + " | " + c.getMessage()); count++;
        }
        System.out.println("Matching communications: " + count + " | KMP O(n+m) time, O(m) space per message.");
    }

    private void searchId(Scanner scanner) {
        System.out.print("ID fragment: ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] ID fragment cannot be empty.", ConsoleColors.YELLOW)); return; }
        System.out.println(ConsoleColors.color("\n=== RABIN-KARP ID SEARCH ===", ConsoleColors.CYAN));
        int count = 0;
        for (Communication c : communications) if (!StringAlgorithms.rabinKarp(c.getCommunicationId().toLowerCase(Locale.ROOT), query.toLowerCase(Locale.ROOT)).matches().isEmpty()) {
            System.out.println(c.getCommunicationId() + " | " + c.getSender() + " -> " + c.getReceiver() + " | " + c.getPriority()); count++;
        }
        System.out.println("Matches: " + count + " | expected O(n+m), worst O(nm); O(1) auxiliary space.");
    }

    private void multiKeywordSearch(Scanner scanner) {
        System.out.print("Enter keywords: ");
        String input = scanner.nextLine();
        if (input.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Enter at least one keyword.", ConsoleColors.YELLOW)); return; }
        List<String> words = Arrays.asList(input.split("\\s+"));
        StringAlgorithms.AhoCorasick automaton = new StringAlgorithms.AhoCorasick(words);
        System.out.println(ConsoleColors.color("\n=== AHO-CORASICK COMMUNICATION RESULTS ===", ConsoleColors.CYAN));
        for (Communication communication : communications) {
            Map<String, List<Integer>> results = automaton.search(communication.getMessage());
            if (!results.isEmpty()) System.out.println(communication.getCommunicationId() + " -> " + results.keySet());
        }
        System.out.println("Automaton states: " + automaton.states() + " | O(text + matches + dictionary) time.");
    }

    private void repeatedMessages() {
        System.out.println(ConsoleColors.color("\n=== REPEATED MESSAGE DETECTION ===", ConsoleColors.CYAN));
        for (int i = 0; i < communications.size(); i++) {
            for (int j = i + 1; j < communications.size(); j++) {
                String a = communications.get(i).getMessage().toLowerCase(Locale.ROOT);
                String b = communications.get(j).getMessage().toLowerCase(Locale.ROOT);
                int distance = edu.disasternet.algorithms.dp.DynamicProgramming.levenshtein(a, b);
                double similarity = 1.0 - (double) distance / Math.max(1, Math.max(a.length(), b.length()));
                if (similarity >= 0.75) {
                    System.out.printf("Possible duplicate: %s and %s (%.0f%% similar)%n", communications.get(i).getCommunicationId(), communications.get(j).getCommunicationId(), similarity * 100);
                }
            }
        }
        System.out.println("Similarity uses Levenshtein distance for every message pair; O(c^2·L^2) worst-case work.");
    }

    private void viewCommunications() {
        System.out.println(ConsoleColors.color("\n=== COMMUNICATIONS ===", ConsoleColors.CYAN));
        for (Communication c : communications) {
            System.out.println(c.getCommunicationId() + " | " + c.getSender() + " -> " + c.getReceiver() + " | " + c.getPriority() + " | " + c.getMessage());
        }
    }
}
