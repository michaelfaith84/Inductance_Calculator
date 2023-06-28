import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ArrayList<Antenna> antennae = new ArrayList<>();
        Scanner input = new Scanner(System.in);
        String selection;
        boolean running = true;

        do {
            System.out.println("What would you like to do?");
            System.out.println("\tCreate [r]ound antenna?");
            System.out.println("\tCreate [s]quare antenna?");
            if (!antennae.isEmpty()) {
                System.out.println("\t[L]ist created antennae?");
                System.out.println("\t[E]xport antennae?");
            }
            System.out.println("\t[Q]uit?");

            selection = input.nextLine().toLowerCase().substring(0, 1);

            switch (selection) {
                case "r":
                    antennae.add(new Antenna(Antenna.Types.ROUND));
                    keepAntenna(input, antennae);
                    break;
                case "s":
                    antennae.add(new Antenna(Antenna.Types.SQUARE));
                    keepAntenna(input, antennae);
                    break;
                case "l":
                    for (Antenna antenna : antennae) {
                        antenna.printDetails();
                    }
                    break;
                case "e":
                    exportAsCSV(input, antennae);
                    break;
                case "q":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection.");
                    System.out.println();
            }
        } while (running);
    }

    public static void keepAntenna(Scanner input, ArrayList<Antenna> antennae) {
        if (!getValidBoolean(input, "Keep antenna?", "y","n")) {
            antennae.remove(antennae.size() - 1);
        }
    }

    public static boolean getValidBoolean(Scanner input, String msg, String truthy, String falsy) {
        String value;

        do {
            System.out.printf("%s [%s|%s]:\n", msg, truthy, falsy);
            value = input.nextLine().toLowerCase();

            if (value.equals(truthy.toLowerCase())) {
                return true;
            } else if (value.equals(falsy.toLowerCase())) {
                return false;
            } else {
                System.out.println("Invalid selection.");
                System.out.println();
            }
        } while (true);
    }

    public static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static boolean isValidFileName(String fileName) {
        return !new File(fileName + "_round.csv").exists() && !new File(fileName + "_square.csv").exists();
    }

    public static String getValidFileName(Scanner input) {
        String fileName = sanitizeFileName(input.nextLine());
        if (!isValidFileName(fileName)) {
            while (!isValidFileName(fileName)) {
                System.out.println("Invalid file name. Try again.");
                fileName = sanitizeFileName(input.nextLine());
            }
        }

        return fileName;
    }

    public static void initializeCSV(PrintWriter writer, Antenna antenna) {
        String line = "";
        for (String key : antenna.getDetails().keySet()) {
            line += key + ",";
        }
        writer.println(line.substring(0, line.length() -1));
    }

    public static void writeData(PrintWriter writer, Antenna antenna) {
        String line = "";
        for (String value : antenna.getDetails().values()) {
            line += value + ",";
        }
        writer.println(line.substring(0, line.length() -1));
    }

    public static void exportAsCSV(Scanner input, ArrayList<Antenna> antennae) {
        String fileName;
        File round;
        PrintWriter roundWriter = null;
        File square;
        PrintWriter squareWriter = null;

        System.out.println("File name to use?");
        fileName = getValidFileName(input);

        round = new File(fileName + "_round.csv");
        square = new File(fileName + "_square.csv");

        try {
            for (Antenna antenna : antennae) {
                if (antenna.type == Antenna.Types.ROUND) {
                    if (!round.exists()) {
                        round.createNewFile();
                        roundWriter = new PrintWriter(round);
                        initializeCSV(roundWriter, antenna);
                    }
                    writeData(roundWriter, antenna);
                } else {
                    if (!square.exists()) {
                        square.createNewFile();
                        squareWriter = new PrintWriter(square);
                        initializeCSV(squareWriter, antenna);
                    }
                    writeData(squareWriter, antenna);
                }
            }
            if (roundWriter != null) {
                roundWriter.close();
            }
            if (squareWriter != null) {
                squareWriter.close();
            }
        } catch (IOException ignored) {
            System.out.println("Unable to create files.");
        }
    }
}