package de.holube.tilman;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class APIRequest {

    private static HttpURLConnection con;
    private static JSONObject data;
    private static int maxRequestSize = 10;

    public static void addWordsToJSON(List<String> wordsToLookUp)throws IOException {
        data = new JSONObject(new String(Files.readAllBytes(Paths.get("database.json"))));
        for (int i = 0; i < wordsToLookUp.size(); i = i + maxRequestSize) {
            addMaxWordsToJSON(wordsToLookUp, i);
        }
        FileWriter writer = new FileWriter("database.json");
        writer.write(data.toString());
        writer.close();
    }

    private static void addMaxWordsToJSON(List<String> wordsToLookUp, int index)throws IOException {
        // generate string to send
        String inputString = "";
        int end = index + maxRequestSize;
        if (end > wordsToLookUp.size()) end = wordsToLookUp.size();
        for (int i = index; i < end; i++) {
            inputString += "&list_eq=" + (String) wordsToLookUp.get(i);
        }

        // open connection
        URL url = new URL ("http://www.dlexdb.de/sr/dlexdb/kern/typ/list/?select=typ_cit%2Ctyp_freq_abs");
        con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        // send string
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = inputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // read string
        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONObject responseObject = new JSONObject(response.toString());
            JSONArray responseData = responseObject.getJSONArray("data");

            for (int j = 0; j < responseData.length(); j++) {
                JSONArray wordResponse = responseData.getJSONArray(j);
                try {
                    String word = wordResponse.getString(0);
                    int freq = wordResponse.getInt(1);
                    data.put(wordsToLookUp.get(index + j), freq);
                    if (!word.equals(wordsToLookUp.get(index + j))) {
                        System.out.println(word + "!=" + wordsToLookUp.get(index + j));
                    }
                } catch (JSONException e) {
                    data.put(wordsToLookUp.get(index + j), -1);
                }
            }
        }
    }
}
