import java.util.*;
import java.io.*;

public class pepasm {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java pepasm <filename.pep>");
            return;
        }

        ArrayList<String> lines = new ArrayList<>();
        HashMap<String, Integer> labels = new HashMap<>();
        int currentAddr = 0;

        try {
            Scanner sc = new Scanner(new File(args[0]));
            while (sc.hasNextLine()) {
                String raw = sc.nextLine();
                String line = raw.split(";")[0].trim(); 
                
                if (line.isEmpty() || line.equalsIgnoreCase(".END")) continue;

                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    labels.put(parts[0].trim(), currentAddr);
                    line = (parts.length > 1) ? parts[1].trim() : "";
                }

                if (line.isEmpty()) continue;
                lines.add(line);
                String mnemonic = line.split("\\s+")[0].toUpperCase();
                if (mnemonic.equals("STOP") || mnemonic.equals("ASLA") || mnemonic.equals("ASRA")) {
                    currentAddr += 1;
                } else {
                    currentAddr += 3;
                }
            }
            sc.close();
        } catch (Exception e) {
            System.out.println("Error reading the file.");
            return;
        }

        String result = "";
        for (String line : lines) {
            String[] parts = line.split("\\s+", 2);
            String op = parts[0].toUpperCase();
            
            String mode = "i"; 
            if (line.toLowerCase().endsWith(", d")) {
                mode = "d";
            }
            result += getOpcode(op, mode) + " ";
            if (parts.length > 1) {
                String hexVal = processOperand(parts[1], labels);
                if (hexVal != null) {
                    result += hexVal + " ";
                }
            }
        }
        System.out.println(result.trim());
    }
    public static String getOpcode(String op, String mode) {
        switch (op) {
            case "STOP": return "00";
            case "ASLA": return "0A";
            case "ASRA": return "0C";
            case "STBA": return "F1";
            case "STWA": return "E1";
            case "BRNE": return "1A";
            case "LDBA": return mode.equals("i") ? "D0" : "D1";
            case "LDWA": return mode.equals("i") ? "C0" : "C1";
            case "ANDA": return mode.equals("i") ? "80" : "81";
            case "CPBA": return mode.equals("i") ? "B0" : "B1";
            case "ADDA": return mode.equals("i") ? "70" : "71"; 
            default:     return "00";
        }
    }

    public static String processOperand(String opPart, HashMap<String, Integer> labels) {
        String clean = opPart.split(",")[0].trim();
        String hex = "";

        if (clean.startsWith("0x")) {
            hex = clean.substring(2).toUpperCase();
        } else if (labels.containsKey(clean)) {
            hex = Integer.toHexString(labels.get(clean)).toUpperCase();
        } else {
            return null;
        }
        while (hex.length() < 4) {
            hex = "0" + hex;
        }
      
        return hex.substring(0, 2) + " " + hex.substring(2, 4);
    }
}
