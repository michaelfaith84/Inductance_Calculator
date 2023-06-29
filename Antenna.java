import java.util.TreeMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Antenna {
    /**
     * The types of antennae supported.
     */
    public enum Types { SQUARE, ROUND }

    /**
     * The type of antenna this object represents.
     */
    public Types type;
    private Scanner input;
    /**
     * The number of decimal places the inductance will be formatted to.
     */
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
        if (!validateInductance()) {
            System.out.println("\tCoil is out of the 300nH - 3µH inductance range.");
        }
        System.out.println();
    }

    /**
     * @param msg - The message that should be presented to the user.
     * @return double - The double value the user entered.
     */
    private double getValidDouble(String msg) {
        double value;
        System.out.println(msg);

        try {
            value = input.nextDouble();
        } catch (InputMismatchException ignored) {
            input.nextLine();
            System.out.println("Invalid entry.");
            value = getValidDouble(msg);
        }

        return value;
    }

    /**
     *
     * @param val - The raw inductance to be formatted.
     * @return String - The formatted inductance.
     */
    private String convert(double val) {
        // Unit prefixes
        String[] PREFIX_ARRAY = new String[]{"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

        if (val == 0.0) {
            return String.format("%." + DECIMAL_PLACES + "fH", 0.0);
        } else {
            double log10 = Math.log10(Math.abs(val));
            int count = (int) Math.floor(log10 / 3.0);
            int index = count + 5;
            val /= Math.pow(10.0, count * 3);
            return index >= 0 && index < PREFIX_ARRAY.length ? String.format("%." + DECIMAL_PLACES + "f%sH", val, PREFIX_ARRAY[index]) : String.format("%." + DECIMAL_PLACES + "fe%dH", val, count * 3);
        }
    }

    /**
     * Returns an associative array detailing the antenna's specs.
     * @return TreeMap
     */
    public TreeMap<String, String> getDetails() {
        if (type == Types.ROUND) {
            return round.getDetails();
        } else {
            return square.getDetails();
        }
    }

    /**
     * Prints the key/value map detailing the antenna to the console.
     */
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

    /**
     * Returns the converted, stringified inductance.
     * @return String
     */
    public String getInductance() {
        if (type == Types.ROUND) {
            return convert(round.getInductance());
        } else {
            return convert(square.getInductance());
        }
    }

    /**
     * 300nH to 3µH is the target.
     * @return boolean
     */
    public boolean validateInductance() {
        String inductance = getInductance();
        String units = inductance.substring(inductance.length() - 2);
        int number = Integer.parseInt(inductance.substring(0, inductance.length() -2).split("\\.")[0]);

        return (units.equals("nH") && number >= 300) || (units.equals("µH") && number <= 3);
    }

    /**
     * These methods are important for working with the existing application.<br /><br />
     */
    interface AntennaMethods {
        /**
         * This prompts the user for relevant information to their antenna.
         */
        void getVariables();
        /**
         * This should return the raw inductance.
         * @return double
         */
        double getInductance();

        /**
         * This should return an associative array of relevant key/value pairs.
         * @return TreeMap
         */
        TreeMap<String, String> getDetails();
    }

    class RoundAntenna implements AntennaMethods {
        /**
         * Diameter -> d
         */
        private double diameter;
        /**
         * Average Width -> s
         */
        private double averageWidth;
        /**
         * Number of Turns -> Na
         */
        private double numberOfTurns;

        RoundAntenna() {
            this.diameter = 0.0;
            this.averageWidth = 0.0;
            this.numberOfTurns = 0.0;

            System.out.println();
            System.out.println("You selected round.");
            System.out.println();

            getVariables();
        }

        @Override
        public void getVariables() {
            diameter = getValidDouble("Enter the average diameter[d] of the NFC antenna: ");

            averageWidth = getValidDouble("Enter the width[s] of the NFC antenna: ");

            numberOfTurns = getValidDouble("Enter the number of turns[Na] of the NFC antenna: (1-6 is ideal)");
        }

        @Override
        public double getInductance() {
            return 24.6 * Math.pow(numberOfTurns, 2.0) * (diameter / 10.0) / (1.0 + 2.75 * (averageWidth / 10.0 / (diameter / 10.0))) * 1.0E-9;
        }

        @Override
        public TreeMap<String, String> getDetails() {
            TreeMap<String, String> details = new TreeMap<>();

            details.put("type", "round");
            details.put("d", String.valueOf(diameter));
            details.put("s", String.valueOf(averageWidth));
            details.put("Na", String.valueOf(numberOfTurns));
            details.put("La", convert(getInductance()));

            return details;
        }
    }

    class SquareAntenna implements AntennaMethods {
        /**
         * Overall Width -> a0
         */
        private double width;
        /**
         * Overall Height -> b0
         */
        private double height;
        /**
         * Track Thickness -> t
         */
        private double trackThickness;
        /**
         * Track Width -> w
         */
        private double trackWidth;
        /**
         * Track Spacing -> g
         */
        private double trackSpacing;
        /**
         * Number of Turns -> Na
         */
        private double numberOfTurns;
        /**
         * Average Width -> Aavg
         */
        private double averageWidth;
        /**
         * Average Height -> Bavg
         */
        private double averageHeight;
        /**
         * Diameter -> d
         */
        private double diameter;

        SquareAntenna() {
            this.width = 0.0;
            this.height = 0.0;
            this.trackThickness = 0.0;
            this.trackWidth = 0.0;
            this.trackSpacing = 0.0;
            this.numberOfTurns = 0.0;
            this.diameter = 0.0;

            getVariables();
        }

        @Override
        public void getVariables() {
            width = getValidDouble("Enter the width[a0] of the NFC antenna: ");

            height = getValidDouble("Enter the height[b0] of the NFC antenna: ");

            trackThickness = getValidDouble("Enter the track thickness[t] of the NFC antenna:\n(0.03556mm = 1oz/ft^2)");

            trackWidth = getValidDouble("Enter the track width[w] of the NFC antenna:\n(0.254mm = 10mils)");

            trackSpacing = getValidDouble("Enter the track spacing[g] of the NFC antenna:\n(0.254mm = 10mils)");

            numberOfTurns = getValidDouble("Enter the number of turns[Na] of the NFC antenna: (1-6 is ideal)");

            calcAverageWidth();
            calcAverageHeight();
            calcDiameter();
        }

        @Override
        public double getInductance() {
            final double u0 = 1.256637062E-9;
            return u0 / Math.PI * (calcx1() + calcx2() - calcx3() + calcx4()) * Math.pow(numberOfTurns, 1.8);
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
            details.put("La", convert(getInductance()));

            return details;
        }

        private void calcAverageWidth() {
            averageWidth = width - numberOfTurns * (trackSpacing + trackWidth);
        }

        private void calcAverageHeight() {
            averageHeight = height - numberOfTurns * (trackSpacing + trackWidth);
        }

        private void calcDiameter() {
            diameter = 2.0 * (trackThickness + trackWidth) / Math.PI;
        }

        private double calcx1() {
            return averageWidth * Math.log(2.0 * averageWidth * averageHeight / (diameter * (averageWidth + Math.sqrt(Math.pow(averageWidth, 2.0) + Math.pow(averageHeight, 2.0)))));
        }

        private double calcx2() {
            return averageHeight * Math.log(2.0 * averageWidth * averageHeight / (diameter * (averageHeight + Math.sqrt(Math.pow(averageWidth, 2.0) + Math.pow(averageHeight, 2.0)))));
        }

        private double calcx3() {
            return 2.0 * (averageWidth + averageHeight - Math.sqrt(Math.pow(averageWidth, 2.0) + Math.pow(averageHeight, 2.0)));
        }

        private double calcx4() {
            return  (averageWidth + averageHeight) / 4.0;
        }
    }
}
