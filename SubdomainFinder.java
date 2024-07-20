import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubdomainFinder {

    public static void main(String[] args) {
        Args parsedArgs = parseArgs(args);

        if (parsedArgs == null) {
            System.err.println("Invalid arguments. Usage: -d <domain> [-o <output_file>]");
            return;
        }

        String target = clearUrl(parsedArgs.domain);
        String output = parsedArgs.output;

        ArrayList<String> subdomains = new ArrayList<>();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://crt.sh/?q=%25." + target + "&output=json").openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != 200) {
                System.err.println("[X] Information not available!");
                return;
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            String json = response.toString();
            Pattern pattern = Pattern.compile("\"name_value\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                subdomains.add(matcher.group(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("\n[!] ---- TARGET: " + target + " ---- [!] \n");

        HashSet<String> uniqueSubdomains = new HashSet<>(subdomains);
        ArrayList<String> sortedSubdomains = new ArrayList<>(uniqueSubdomains);
        Collections.sort(sortedSubdomains);

        for (String subdomain : sortedSubdomains) {
            System.out.println("[-]  " + subdomain);
            if (output != null) {
                saveSubdomains(subdomain, output);
            }
        }

        System.out.println("\n\n[!]  Definitely use python for these tasks!!");
    }

    private static Args parseArgs(String[] args) {
        String domain = null;
        String output = null;

        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) || "--domain".equals(args[i])) {
                if (i + 1 < args.length) {
                    domain = args[++i];
                } else {
                    return null;
                }
            } else if ("-o".equals(args[i]) || "--output".equals(args[i])) {
                if (i + 1 < args.length) {
                    output = args[++i];
                } else {
                    return null;
                }
            }
        }

        if (domain == null) {
            return null;
        }

        return new Args(domain, output);
    }

    private static String clearUrl(String target) {
        return target.replaceFirst(".*www\\.", "").split("/")[0].trim();
    }

    private static void saveSubdomains(String subdomain, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            writer.write(subdomain);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Args {
        String domain;
        String output;

        Args(String domain, String output) {
            this.domain = domain;
            this.output = output;
        }
    }
}
