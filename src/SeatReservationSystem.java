import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SeatReservationSystem {

    private static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 9;
    private static final int MAX_SEATS_PER_USER = 3;

    private final Set<String> reservedSeats;
    private final Set<String> confirmedSeats;

    private final Set<String> reservedByCurrentUser;

    private String currentUser;
    private final ArrayList<User> login;
    private final Scanner scanner;

    public SeatReservationSystem() {
        reservedSeats = new HashSet<>();
        confirmedSeats = new HashSet<>();
        reservedByCurrentUser = new HashSet<>();

        currentUser = null;

        login = new ArrayList<>();
        scanner = new Scanner(System.in);

        // Read the reserved seats from the 'Seats.txt' file
        readReservedSeatsFromFile();
    }


    public void login() {
        // Check if the user is already logged in
        if (currentUser != null) {
            System.out.println("You are already logged in as " + currentUser);
            return;
        }

        // Login the user
        System.out.print("Enter username: ");
        String userId = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        try (Scanner out = new Scanner(new File("Users.txt"))) {
            HashMap<String,String> map = new HashMap<>();
            while(out.hasNextLine()) {
                String [] split = out.nextLine().split(" ");
                map.put(split[0], split[1]);
            }

            if(map.containsKey(userId)) {
                if(map.get(userId).equals(password)) {
                    currentUser = userId;
                    reservedByCurrentUser.clear();
                    login.add(new User(userId, password));
                    System.out.println("Logged in as " + currentUser);
                    loginMenu();
                }
                else System.out.println("Incorrect password.");
            }
            else {
                System.out.println("User does not exist.");
            }
        } catch (Exception e) {
            System.out.println("Invalid user");
            mainMenu();
        }
    }

    public void logout() {
        if (currentUser == null) {
            System.out.println("You are not logged in");
            return;
        }
        currentUser = null;
        System.out.println("Logged out");
    }

    public void displayAvailability() {
        System.out.println("\n\033[1m          SEATS        \033[0m");
        System.out.println("  ╔═══════════════════╗");
        System.out.println("\033[1m  ║ A B C D E F G H I ║\033[0m");
        System.out.println("\033[1m  ╠═══════════════════╣\033[0m");
        for (int i = 0; i < NUM_ROWS; i++) {
            System.out.print((i + 1) + " ║ ");
            for (int j = 0; j < NUM_COLS; j++) {
                String seatId = getSeatId(i, j);
                if (reservedSeats.contains(seatId)) {
                    System.out.print("\033[31mX\033[0m ");
                } else {
                	System.out.print("\033[32m\u25EF\033[0m ");
                }
            }
            System.out.print("║");
            System.out.println();
        }
        System.out.println("  ╚═══════════════════╝");
    }



    private void readReservedSeatsFromFile() {
        try (Scanner fileScanner = new Scanner(new File("Seats.txt"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    String[] seats = parts[1].split(" ");
                    reservedSeats.addAll(Arrays.asList(seats));
                }
            }
        } catch (FileNotFoundException e) {
            // File not found, ignore
        }
    }


    private void writeLinesToFile(List<String> lines, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Seats.txt", append))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private void writeReservedSeatsToFile(String currentUser, Set<String> reservedByCurrentUser) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String seat : reservedByCurrentUser) {
            lines.add(currentUser + " " + seat);
        }
        writeLinesToFile(lines, true); // append to the file
    }


    public void reserveSeats() throws IOException {
        if (reservedByCurrentUser.size() >= MAX_SEATS_PER_USER) {
            System.out.printf("You have already reserved %d seats%n", MAX_SEATS_PER_USER);
            return;
        }

        if (isUserMaxSeatsReached(currentUser)) {
            System.out.printf("Sorry, you have already reserved the maximum of %d seats allowed.%n", MAX_SEATS_PER_USER);
            return;
        }

        System.out.print("Enter row number (1-4): ");
        int row = scanner.nextInt() - 1;
        System.out.print("Enter column letter (A-I): ");
        String column = scanner.next().toUpperCase();

        if (isValidSeat(row, column)) {
            System.out.println("Invalid seat");
            return;
        }

        String seatId = getSeatId(row, getColumnIndex(column));

        if (reservedSeats.contains(seatId)) {
            System.out.println("Seat already reserved");
        } else {
            // Confirm the reservation with the user
            System.out.print("Confirm reservation (Y/N): ");
            String confirm = scanner.next().toUpperCase();

            if (confirm.equals("Y")) {
                // Reserve the seat and add it to the reserved seats by the current user
                reservedSeats.add(seatId);
                reservedByCurrentUser.add(seatId);
                confirmedSeats.add(seatId);
                writeReservedSeatsToFile(currentUser, reservedByCurrentUser);
                System.out.println("Seat reserved!");
            } else if (confirm.equals("N")) {
                System.out.println("Reservation canceled.");
                reservedSeats.remove(seatId);
                reservedByCurrentUser.remove(seatId);
                removeUnreservedSeat(currentUser, seatId);
            } else {
                System.out.println("Invalid input. Reservation canceled.");
            }
        }
    }


    private boolean isUserMaxSeatsReached(String currentUser) {
        try (Scanner fileScanner = new Scanner(new File("Seats.txt"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(" ", 2);
                if (parts.length == 2 && parts[0].equals(currentUser)) {
                    String[] seats = parts[1].split(" ");
                    return seats.length >= MAX_SEATS_PER_USER;
                }
            }
        } catch (FileNotFoundException e) {
            // File not found, ignore
        }
        return false;
    }


    public void unreserveSeats() throws IOException {
        System.out.print("Enter row number (1-4): ");
        int row = scanner.nextInt() - 1;
        System.out.print("Enter column letter (A-I): ");
        String column = scanner.next().toUpperCase();
        if (isValidSeat(row, column)) {
            System.out.println("Invalid seat");
            return;
        }

        String seatId = getSeatId(row, getColumnIndex(column));

        if (!reservedByCurrentUser.contains(seatId)) {
            System.out.println("Seat not reserved by you");
            return;
        }

        reservedSeats.remove(seatId);
        reservedByCurrentUser.remove(seatId);
        removeUnreservedSeat(currentUser, seatId);
    }

    // Remove the unreserved seat from Seats.txt
    public void removeUnreservedSeat(String currentUser, String seatId) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("Seats.txt"))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] parts = currentLine.split(" ", 2);
                if (parts.length != 2) {
                    continue;
                }

                String userId = parts[0];
                String[] seats = parts[1].split(",");
                List<String> seatList = new ArrayList<>(Arrays.asList(seats));
                if (userId.equals(currentUser)) {
                    seatList.remove(seatId);
                    if (!seatList.isEmpty()) {
                        lines.add(userId + " " + String.join(",", seatList));
                    }
                } else {
                    lines.add(currentLine);
                }
            }
        }

        writeLinesToFile(lines, false);

        System.out.printf("Seat %s unreserved%n", seatId);
    }

    public void viewReservedSeats() {
        if (currentUser == null) {
            System.out.println("You need to login first");
            return;
        }

        try (Scanner fileScanner = new Scanner(new File("Seats.txt"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    String user = parts[0];
                    String[] seats = parts[1].split(" ");
                    if (user.equals(currentUser)) {
                        reservedByCurrentUser.addAll(Arrays.asList(seats));
                    }
                }
            }

            if (reservedByCurrentUser.isEmpty()) {
                System.out.println("You have not reserved any seats yet");
            } else {
                System.out.printf("You have reserved %d seats: %s%n", reservedByCurrentUser.size(), reservedByCurrentUser);
            }
        } catch (FileNotFoundException e) {
            // File not found, ignore
        }
    }


    public void loginAdmin() {
        JAppAdmin admin = new JAppAdmin();

        if (currentUser != null && currentUser.startsWith("admin_")) {
            System.out.println("An admin is already logged in");
            return;
        }

        System.out.print("Enter username (hint - admin): ");
        String adminUsername = scanner.next();
        System.out.print("Enter password (hint - admin): ");
        String adminPassword = scanner.next();

        if (adminUsername.equals("admin") && adminPassword.equals("admin")) {
            System.out.println("Logged in as admin");
            currentUser = adminUsername;

            boolean exit = false;
            while (!exit) {
                System.out.println("\n\033[1m╔════════════════════════════════╗");
                System.out.println("\033[1m║        ADMIN MENU              ║");
                System.out.println("╠════════════════════════════════╣");
                System.out.println("║\033[32m 1. Display user list\033[0m           ║");
                System.out.println("║\033[33m 2. View user reservations\033[0m      ║");
                System.out.println("║\033[31m 3. Log out and go to Main Menu\033[0m ║");
                System.out.println("\033[1m╚════════════════════════════════╝");
                System.out.print("\nEnter your choice: ");


                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> admin.displayUserList();
                    case 2 -> admin.displayReservations();
                    case 3 -> {
                        logout();
                        exit = true;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } else {
            System.out.println("Incorrect admin username or password");
        }
    }

    private boolean isValidSeat(int row, String column) {
        return row < 0 || row >= NUM_ROWS || getColumnIndex(column) < 0;
    }

    private int getColumnIndex(String column) {
        return column.charAt(0) - 'A';
    }

    private String getSeatId(int row, int col) {
        return (row + 1) + "" + (char) ('A' + col);
    }

    private void mainMenu() {
        System.out.println("\nWelcome to JApp!");
        System.out.println("1. Login");
        System.out.println("2. Create an account");
        System.out.println("3. Login as Admin");
        System.out.println("4. Exit");
        System.out.print("\nEnter your choice: ");
    }

    public void loginMenu() throws IOException {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n╔═══════════════════════════════╗");
            System.out.println("║\033[1m MENU                          \033[0m║");
            System.out.println("╠═══════════════════════════════╣");
            System.out.println("║\033[32m 1. Display seat availability  \033[0m║");
            System.out.println("║\033[33m 2. Reserve seats              \033[0m║");
            System.out.println("║\033[34m 3. Unreserve seats            \033[0m║");
            System.out.println("║\033[35m 4. View your reservations     \033[0m║");
            System.out.println("║\033[31m 5. Logout and go to main Menu \033[0m║");
            System.out.println("╚═══════════════════════════════╝");
            System.out.print("\nEnter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> displayAvailability();
                case 2 -> reserveSeats();
                case 3 -> unreserveSeats();
                case 4 -> viewReservedSeats();
                case 5 -> {
                    logout();
                    exit = true;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}