import java.io.*;
import java.util.*;

enum RoomCategory {
    STANDARD(100.0),
    DELUXE(180.0),
    SUITE(300.0);

    private final double pricePerNight;

    RoomCategory(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }
}

class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int roomNumber;
    private final RoomCategory category;
    private boolean isBooked;

    public Room(int roomNumber, RoomCategory category) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.isBooked = false;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomCategory getCategory() { return category; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}

class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String reservationId;
    private final String guestName;
    private final int roomNumber;
    private final RoomCategory category;
    private final int nights;
    private final double totalAmount;

    public Reservation(String reservationId, String guestName, int roomNumber, RoomCategory category, int nights) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.category = category;
        this.nights = nights;
        this.totalAmount = category.getPricePerNight() * nights;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public double getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        return String.format("Booking ID: %s | Guest: %s | Room: %d (%s) | Nights: %d | Total: $%.2f",
                reservationId, guestName, roomNumber, category, nights, totalAmount);
    }
}

public class HotelReservationSystem {
    private static final String FILE_NAME = "hotel_data.dat";
    private static List<Room> rooms = new ArrayList<>();
    private static List<Reservation> reservations = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        if (rooms.isEmpty()) {
            initializeRooms();
        }

        while (true) {
            System.out.println("\n=== HOTEL RESERVATION SYSTEM ===");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Make a Reservation");
            System.out.println("3. Cancel a Reservation");
            System.out.println("4. View Booking Details");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": searchRooms(); break;
                case "2": makeReservation(); break;
                case "3": cancelReservation(); break;
                case "4": viewBookingDetails(); break;
                case "5":
                    saveData();
                    System.out.println("Thank you for using the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid selection. Please enter 1-5.");
            }
        }
    }

    private static void initializeRooms() {
        rooms.add(new Room(101, RoomCategory.STANDARD));
        rooms.add(new Room(102, RoomCategory.STANDARD));
        rooms.add(new Room(201, RoomCategory.DELUXE));
        rooms.add(new Room(202, RoomCategory.DELUXE));
        rooms.add(new Room(301, RoomCategory.SUITE));
    }

    private static void searchRooms() {
        System.out.println("\n--- Available Rooms ---");
        boolean found = false;
        for (Room r : rooms) {
            if (!r.isBooked()) {
                System.out.printf("Room %d [%s] - $%.2f/night\n",
                        r.getRoomNumber(), r.getCategory(), r.getCategory().getPricePerNight());
                found = true;
            }
        }
        if (!found) System.out.println("No rooms currently available.");
    }

    private static void makeReservation() {
        searchRooms();
        System.out.print("\nEnter Room Number to book: ");
        int roomNum = parseInteger();

        Room selectedRoom = null;
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNum && !r.isBooked()) {
                selectedRoom = r;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("Room unavailable or invalid room number.");
            return;
        }

        System.out.print("Enter Guest Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Number of Nights: ");
        int nights = parseInteger();
        if (nights <= 0) {
            System.out.println("Invalid duration.");
            return;
        }

        double total = selectedRoom.getCategory().getPricePerNight() * nights;
        System.out.printf("Total Cost: $%.2f\n", total);
        
        // Payment Simulation
        System.out.print("Enter Card Number to Simulate Payment: ");
        String card = scanner.nextLine().trim();
        if (card.length() < 12) {
            System.out.println("Payment failed. Invalid card details.");
            return;
        }

        selectedRoom.setBooked(true);
        String bookingId = "RES" + (1000 + new Random().nextInt(9000));
        Reservation res = new Reservation(bookingId, name, selectedRoom.getRoomNumber(), selectedRoom.getCategory(), nights);
        reservations.add(res);
        saveData();

        System.out.println("Payment Successful!");
        System.out.println("Reservation Confirmed! " + res);
    }

    private static void cancelReservation() {
        System.out.print("\nEnter Booking ID to cancel: ");
        String resId = scanner.nextLine().trim();

        Reservation target = null;
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(resId)) {
                target = r;
                break;
            }
        }

        if (target == null) {
            System.out.println("Reservation not found.");
            return;
        }

        for (Room room : rooms) {
            if (room.getRoomNumber() == target.getRoomNumber()) {
                room.setBooked(false);
                break;
            }
        }

        reservations.remove(target);
        saveData();
        System.out.println("Reservation " + resId + " cancelled successfully.");
    }

    private static void viewBookingDetails() {
        System.out.print("\nEnter Booking ID or Guest Name: ");
        String query = scanner.nextLine().trim().toLowerCase();

        boolean found = false;
        for (Reservation r : reservations) {
            if (r.getReservationId().toLowerCase().equals(query) || r.getGuestName().toLowerCase().contains(query)) {
                System.out.println(r);
                found = true;
            }
        }
        if (!found) System.out.println("No matching reservation record found.");
    }

    private static int parseInteger() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(rooms);
            oos.writeObject(reservations);
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            rooms = (List<Room>) ois.readObject();
            reservations = (List<Reservation>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Starting with fresh system data.");
        }
    }
}
