package datahandler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileReader {

    // Reads a CSV file and returns the data as a List of rows
    public List<CSVRecord> readWholeDataCSV(String csvFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(csvFile));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            return new ArrayList<>(csvParser.getRecords());
        }
    }

    // Reads a JSON file and returns its content as a JSONObject
    public JSONObject readJSON(String jsonFile) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonFile)));
        return new JSONObject(content);
    }

    // Method to read links from a CSV file
    public static List<String> readLinksFromCSV(String filePath) {
        List<String> links = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                links.add(line);  // Add each link to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }

    // Reads a CSV file and returns rows as an array of strings
    public static List<String[]> readCommentorCSV(String filePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
