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

        //start building the final output line by line
        for (String line : sourceLines) {
            //put in the mnemonic
            String mnemonic = line.split("\\s+")[0].toUpperCase(); //in human this basically is getting the first word of the string. found on google.
            finalObjectCode.append(mnemonicToHex(mnemonic, getAddressingMode(line)));
            finalObjectCode.append(" ");

            //Substitute labels for their addresses
            String operandHex = resolveOperand(line, labelTable);
            if (operandHex != null) {
                finalObjectCode.append(operandHex);
                finalObjectCode.append(" ");
            }

            //put in the hex value if it is relevant
            if (formatHexForObjectCode(line) != null) {
                finalObjectCode.append(formatHexForObjectCode(line));
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

    public static String getAddressingMode(String line) {
        if (line.charAt(line.length() - 1) == 'i') {
            return "i";
        } else if (line.charAt(line.length() - 1) == 'd') {
            return "d";
        } else {
            return null;
        }
    }

    public static String formatHexForObjectCode(String line) {
        // Split into mnemonic + operand
        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            return null; // no operand at all
        }
        String operandPart = parts[1].trim();

        // Strip addressing mode if present: "0x0011, i" → "0x0011"
        int commaIndex = operandPart.indexOf(',');
        if (commaIndex != -1) {
            operandPart = operandPart.substring(0, commaIndex).trim();
        }

        // Must start with 0x
        if (!operandPart.startsWith("0x")) {
            return null;
        }

        // Extract hex digits
        String hex = operandPart.substring(2).toUpperCase();
        // Pep9 operands must be exactly 4 hex digits
        if (hex.length() != 4) {
            return null;
        }

        // Format as "AA BB"
        return hex.substring(0, 2) + " " + hex.substring(2, 4);
    }

    public static String resolveOperand(String line, HashMap<String, Integer> labelTable) {
        // Split into mnemonic + operand
        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            return null; // no operand
        }

        String operand = parts[1].trim();

        // Strip addressing mode: "label, d" → "label"
        int commaIndex = operand.indexOf(',');
        if (commaIndex != -1) {
            operand = operand.substring(0, commaIndex).trim();
        }

        // Case 1: literal hex operand
        if (operand.startsWith("0x")) {
            String hex = operand.substring(2).toUpperCase();
            if (hex.length() != 4) return null;
            return hex.substring(0, 2) + " " + hex.substring(2, 4);
        }

        // Case 2: label operand
        if (labelTable.containsKey(operand)) {
            int address = labelTable.get(operand);
            String hex = String.format("%04X", address);
            return hex.substring(0, 2) + " " + hex.substring(2, 4);
        }

        // Unknown operand
        return null;
    }

}