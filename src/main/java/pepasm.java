import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

public class pepasm {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java pepasm <filename.pep>");
            // runs as .pep file
            return;
        }
        ArrayList<String> sourceLines = new ArrayList<>();
        StringBuilder finalObjectCode = new StringBuilder();
        HashMap<String, Integer> labelTable = new HashMap<>();
        int currentAddress = 0;
        try {
            Scanner fileScanner = new Scanner(new File(args[0]));
            // this pass 1 basicall find labels and calculates the memory addresses
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().split(";")[0].trim();
                if (line.isEmpty() || line.startsWith(".END")) continue;
                if (line.contains(":")) {// if the line has a label
                    String[] parts = line.split(":");
                    labelTable.put(parts[0].trim(), currentAddress);
                    line = (parts.length > 1) ? parts[1].trim() : "";
                }
                if (line.isEmpty()) continue;
                sourceLines.add(line);
                String mnemonic = line.split("\\s+")[0].toUpperCase();

                if (mnemonic.equals("STOP") || mnemonic.equals("ASLA") || mnemonic.equals("ASRA")) {
                    currentAddress += 1;
                } else {
                    currentAddress += 3;
                }
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (int line = 0; line < sourceLines.size(); line++) {
            //put in the mnemonic
            finalObjectCode.append(mnemonicToHex(sourceLines.get(line).substring(0,3), getAssigningMode(sourceLines.get(line))));
            finalObjectCode.append(" ");
            //put in the hex value if it is relevant
            if (sourceLines.get(line).length() > 5) {
                finalObjectCode.append(formatHexForObjectCode(sourceLines.get(line)));
                finalObjectCode.append(" ");
            }

        }
    }

    public static String mnemonicToHex(String mnemonic, String addressingMode) {
        switch (mnemonic) {
            case "STBA":
                return "F1";
            case "LDBA":
                if (addressingMode.equals("i")) {
                    return "D0";
                } else {
                    return "D1";
                }
            case "STWA":
                return "E1";
            case "LDWA":
                if (addressingMode.equals("i")) {
                    return "C0";
                } else {
                    return "C1";
                }
            case "ANDA":
                if (addressingMode.equals("i")) {
                    return "80";
                } else {
                    return "81";
                }
            case "ASLA":
                return "0A";
            case "ASRA":
                return "0C";
            case "STOP":
                return "00";
            case "CPBA":
                if (addressingMode.equals("i")) {
                    return "B0";
                } else {
                    return "B1";
                }
            case "BRNE":
                return "1A";
            default: return "00";
        }
    }

    public static String getAssigningMode(String line) {
        if (line.charAt(line.length() - 1) == 'i') {
            return "i";
        } else if (line.charAt(line.length() - 1) == 'd') {
            return "d";
        } else {
            return null;
        }
    }

    public static String formatHexForObjectCode(String line) {
        return null;
    }
}