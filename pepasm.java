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
        HashMap<String, Integer> labelTable = new HashMap<>();
        int currentAddress = 0;
        try {
            Scanner fileScanner = new Scanner(new File(args[0]));
            // this pass 1 basicall find labels and calculates the memory addresses
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().split(";")[0].trim();
                if (line.isEmpty() || line.startsWith(".END")) continue;
                if (line.contains(":")) {
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
        } // pass 2 (use string builder for ASLA AND ASRA)
    }
}
