import java.util.TreeMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Antenna {
    public enum Types { SQUARE, ROUND }
    public Types type;
    private Scanner input;
    // Number of decimal places that 'convert' will return
    private int DECIMAL_PLACES = 3;
    private SquareAntenna square;
    private RoundAntenna round;

    Antenna(Types type) {
        this.type = type;

        input = new Scanner(System.in);

        System.out.println("All units are in millimeters");

        switch (type) {
            case ROUND:
                round = new RoundAntenna();
                break;
            case SQUARE:
                square = new SquareAntenna();
                break;
            default:
                throw new IllegalArgumentException();
        }

        System.out.println();
        System.out.printf("The inductance of the coil is: %s\n", getInductance());
        System.out.println();
    }

    // Returns a double from the user
    private double getValidDouble(String msg) {
        double value;
        System.out.println(msg);

        try {
            value = input.nextDouble();
        } catch (InputMismatchException ignored) {
            System.out.println("Invalid entry.");
            value = getValidDouble(msg);
        }

        return value;
    }

    // Converts the raw inductance to the appropriate unit
    private String convert(double val) {
        // Unit prefixes
        String[] PREFIX_ARRAY = new String[]{"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

        if (val == 0.0) {
            return String.format("%." + DECIMAL_PLACES + "fH", 0.0);
        } else {
            double posVal = val < 0.0 ? -val : val;
            double log10 = Math.log10(posVal);
            int count = (int) Math.floor(log10 / 3.0);
            int index = count + 5;
            val /= Math.pow(10.0, count * 3);
            return index >= 0 && index < PREFIX_ARRAY.length ? String.format("%." + DECIMAL_PLACES + "f%sH", val, PREFIX_ARRAY[index]) : String.format("%." + DECIMAL_PLACES + "fe%dH", val, count * 3);
        }
    }

    public TreeMap<String, String> getDetails() {
        if (type == Types.ROUND) {
            return round.getDetails();
        } else {
            return square.getDetails();
        }
    }

    // Prints key/value maps of the constructed antenna to the console
    public void printDetails() {
        TreeMap<String, String> details;

        if (type == Types.ROUND) {
            details = round.getDetails();
        } else {
            details = square.getDetails();
        }

        details.forEach((key,  value) -> System.out.printf("%s: %s\n", key, value));

        System.out.println();
    }

    // Returns the antenna's inductance
    public String getInductance() {
        if (type == Types.ROUND) {
            return round.getInductance();
        } else {
            return square.getInductance();
        }
    }

    interface AntennaMethods {
        void getVariables();
        String getInductance();
        TreeMap<String, String> getDetails();
    }

    class RoundAntenna implements AntennaMethods {
        private double averageDiameter;
        private double width;
        private double numberOfTurns;

        RoundAntenna() {
            this.averageDiameter = 0.0;
            this.width = 0.0;
            this.numberOfTurns = 0.0;

            System.out.println();
            System.out.println("You selected round.");
            System.out.println();

            getVariables();
        }

        @Override
        public void getVariables() {
            averageDiameter = getValidDouble("Enter the average diameter[D] of the NFC antenna: ");

            width = getValidDouble("Enter the width[s] of the NFC antenna: ");

            numberOfTurns = getValidDouble("Enter the number of turns[Na] of the NFC antenna: ");
        }

        @Override
        public String getInductance() {
            return convert(24.6 * Math.pow(numberOfTurns, 2.0) * (averageDiameter / 10.0) / (1.0 + 2.75 * (width / 10.0 / (averageDiameter / 10.0))) * 1.0E-9);
        }

        @Override
        public TreeMap<String, String> getDetails() {
            TreeMap<String, String> details = new TreeMap<>();

            details.put("type", "round");
            details.put("Dr", String.valueOf(averageDiameter));
            details.put("s", String.valueOf(width));
            details.put("Na", String.valueOf(numberOfTurns));
            details.put("La", getInductance());

            return details;
        }
    }

    class SquareAntenna implements AntennaMethods {
        private double width;
        private double height;
        private double trackThickness;
        private double trackWidth;
        private double trackSpacing;
        private double numberOfTurns;
        private double aAvg;
        private double bAvg;
        private double dS;

        SquareAntenna() {
            this.width = 0.0;
            this.height = 0.0;
            this.trackThickness = 0.0;
            this.trackWidth = 0.0;
            this.trackSpacing = 0.0;
            this.numberOfTurns = 0.0;
            this.dS = 0.0;

            getVariables();
        }

        @Override
        public void getVariables() {
            width = getValidDouble("Enter the width[a0] of the NFC antenna: ");

            height = getValidDouble("Enter the height[b0] of the NFC antenna: ");

            trackThickness = getValidDouble("Enter the track thickness[t] of the NFC antenna:\n(0.03556mm = 1oz/ft^2)");

            trackWidth = getValidDouble("Enter the track width[w] of the NFC antenna:\n(0.254mm = 10mils)");

            trackSpacing = getValidDouble("Enter the track spacing[g] of the NFC antenna:\n(0.254mm = 10mils)");

            numberOfTurns = getValidDouble("Enter the number of turns[Na] of the NFC antenna: ");

            calcAAvg();
            calcBAvg();
            calcDS();
        }

        @Override
        public String getInductance() {
            double u0 = 1.256637062E-9;
            return convert(u0 / Math.PI * (calcx1() + calcx2() - calcx3() + calcx4()) * Math.pow(numberOfTurns, 1.8));
        }

        @Override
        public TreeMap<String, String> getDetails() {
            TreeMap<String, String> details = new TreeMap<>();

            details.put("type", "square");
            details.put("a0", String.valueOf(width));
            details.put("b0", String.valueOf(height));
            details.put("w", String.valueOf(trackWidth));
            details.put("t", String.valueOf(trackThickness));
            details.put("g", String.valueOf(trackSpacing));
            details.put("Na", String.valueOf(numberOfTurns));
            details.put("La", getInductance());

            return details;
        }

        private void calcAAvg() {
            aAvg = width - numberOfTurns * (trackSpacing + trackWidth);
        }

        private void calcBAvg() {
            bAvg = height - numberOfTurns * (trackSpacing + trackWidth);
        }

        private void calcDS() {
            dS = 2.0 * (trackThickness + trackWidth) / Math.PI;
        }

        private double calcx1() {
            return aAvg * Math.log(2.0 * aAvg * bAvg / (dS * (aAvg + Math.sqrt(Math.pow(aAvg, 2.0) + Math.pow(bAvg, 2.0)))));
        }

        private double calcx2() {
            return bAvg * Math.log(2.0 * aAvg * bAvg / (dS * (bAvg + Math.sqrt(Math.pow(aAvg, 2.0) + Math.pow(bAvg, 2.0)))));
        }

        private double calcx3() {
            return 2.0 * (aAvg + bAvg - Math.sqrt(Math.pow(aAvg, 2.0) + Math.pow(bAvg, 2.0)));
        }

        private double calcx4() {
            return  (aAvg + bAvg) / 4.0;
        }
    }
}
