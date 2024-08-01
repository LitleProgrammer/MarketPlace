package de.littleprogrammer.marketplace.utils;

import de.littleprogrammer.marketplace.database.DatabaseTransaction;
import de.littleprogrammer.marketplace.files.ConfigFile;
import org.bson.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhookUtils {

    public static void sendTransaction(DatabaseTransaction transaction) {
        try {
            URL url = new URL(new ConfigFile().getString("discord.webhookURL"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            connection.setDoOutput(true);
            String body = "";

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
