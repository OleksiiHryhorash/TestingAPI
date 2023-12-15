import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;

import static org.testng.Assert.assertTrue;

public class ApiRequestTests {
    private static final Logger logger = Logger.getLogger(ApiRequestTests.class.getName());
    private static final String BASE_URL = TestConfig.getAppUrl();

    static {
        try {
            Handler fileHandler = new FileHandler("logs/testLog");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize file logger", e);
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    @Test
    public void testCreatePlayerWithValidData() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "admin";

        try {
            URL url = new URL(BASE_URL + "/player/create/" + editor);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String postData = "{\"age\": 25, " +
                    "\"gender\": \"male\", " +
                    "\"login\": \"test\", " +
                    "\"password\": \"testpass\", " +
                    "\"role\": \"user\", " +
                    "\"screenName\": \"TestUser\"}";

            logger.info("Creating a user...");
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                logger.info("User created successfully");
            } else {
                logger.warning("Failed to create user. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testCreatePlayerWithInvalidData() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "admin";

        try {
            URL url = new URL(BASE_URL + "/player/create/" + editor);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String postData = "{\"age\": 200, " +
                    "\"gender\": \"male\", " +
                    "\"login\": \"test\", " +
                    "\"password\": \"testpass\", " +
                    "\"role\": \"user\", " +
                    "\"screenName\": \"TestUser\"}";

            logger.info("Creating a user with invalid data...");
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_CREATED) {
                logger.warning("Unexpected success. Response code: " + responseCode);
            } else {
                logger.warning("User created successfully with invalid data. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testGetAllPlayers() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        try {
            URL url = new URL(BASE_URL + "/player/get/all");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                String response = readResponse(connection);
                logger.info("Received player data: " + response);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);

                assertTrue(jsonResponse.has("players"));
                assertTrue(jsonResponse.get("players").isArray());
                assertTrue(jsonResponse.get("players").size() > 0);

                JsonNode firstPlayer = jsonResponse.get("players").get(0);
                assertTrue(firstPlayer.has("age"));
                assertTrue(firstPlayer.has("gender"));
                assertTrue(firstPlayer.has("id"));
                assertTrue(firstPlayer.has("role"));
                assertTrue(firstPlayer.has("screenName"));

            } else {
                logger.warning("Failed to get players. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testGetAllPlayersWithInvalidMethod() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        try {
            URL url = new URL(BASE_URL + "/player/get/all");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_BAD_METHOD) {
                logger.info("Expected Method Not Allowed error received");
            } else {
                logger.warning("Unexpected response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testGetPlayerByPlayerId() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        try {

            int playerId = 1;

            URL url = new URL(BASE_URL + "/player/get?playerId=" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                String response = readResponse(connection);
                logger.info("Received player data: " + response);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);

                assertTrue(jsonResponse.has("age"));
                assertTrue(jsonResponse.has("gender"));
                assertTrue(jsonResponse.has("id"));
                assertTrue(jsonResponse.has("role"));
                assertTrue(jsonResponse.has("screenName"));

            } else {
                logger.warning("Failed to get player. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testGetPlayerWithInvalidId() {

        logger.info("Thread" + TestConfig.getThreadCount() + "start");

        try {
            int invalidPlayerId = -1;
            URL url = new URL(BASE_URL + "/player/get/" + invalidPlayerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                logger.info("Expected 404 Not Found for invalid player id");
            } else {
                logger.warning("Unexpected response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread" + TestConfig.getThreadCount() + "end");
    }

    @Test
    public void testDeletePlayer() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "admin";

        try {
            int playerId = 0;

            URL url = new URL(BASE_URL + "/player/delete/" + editor + "/" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Player deleted successfully");
            } else {
                logger.warning("Failed to delete player. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testDeletePlayerWithInvalidId() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "admin";

        try {

            int playerId = 999;

            URL url = new URL(BASE_URL + "/player/delete/" + editor + "/" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.info("Expected failure. Response code: " + responseCode);
            } else {
                logger.warning("Unexpected success. Player deleted with invalid ID. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testUpdatePlayer() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "supervisor";

        try {

            int playerId = 1301229981;

            String updatedData = "{\"age\": 20, " +
                    "\"gender\": \"male\", " +
                    "\"login\": \"test\", " +
                    "\"password\": \"testpass20\", " +
                    "\"role\": \"user\", " +
                    "\"screenName\": \"TestUser20\"}";

            URL url = new URL(BASE_URL + "/player/update/" + editor + "/" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = updatedData.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Player updated successfully");
            } else {
                logger.warning("Failed to update player. Response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }

    @Test
    public void testUpdatePlayerWithInvalidId() {

        logger.info("Thread " + TestConfig.getThreadCount() + " start");

        String editor = "supervisor";

        try {

            int invalidPlayerId = -1;

            String updatedData = "{\"age\": 20, " +
                    "\"gender\": \"male\", " +
                    "\"login\": \"test\", " +
                    "\"password\": \"testpass20\", " +
                    "\"role\": \"user\", " +
                    "\"screenName\": \"TestUser20\"}";

            URL url = new URL(BASE_URL + "/player/update/" + editor + "/" + invalidPlayerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = updatedData.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                logger.info("Expected error for updating player with invalid ID");
            } else {
                logger.warning("Unexpected response code: " + responseCode);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during API request", e);
        }

        logger.info("Thread " + TestConfig.getThreadCount() + " end");
    }
}
