package datahandler;

public class InfoHandling {

    // Method to extract post ID from a link (assuming the ID is the last part of the URL)
    public static String extractPostId(String link) {
        return link.substring(link.lastIndexOf("/") + 1);
    }

    // Method to convert a string number to an integer, handling "K" and "M" suffixes
    public static int parseNumber(String numStr) {
        if (numStr == null || numStr.isEmpty()) {
            return 0;
        }

        // Remove commas
        numStr = numStr.replace(",", "");

        // Check for K and M suffix
        if (numStr.endsWith("K")) {
            return (int) (Double.parseDouble(numStr.replace("K", "")) * 1_000);
        } else if (numStr.endsWith("M")) {
            return (int) (Double.parseDouble(numStr.replace("M", "")) * 1_000_000);
        } else {
            // No suffix, parse directly
            return Integer.parseInt(numStr);
        }
    }
}
