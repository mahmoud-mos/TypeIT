    package server;

    import java.net.URI;
    import java.net.http.*;
    import java.net.http.HttpRequest.BodyPublishers;
    import java.net.http.HttpResponse.BodyHandlers;

    // Handles all communication with the Groq AI API
    public class AIHandler {

        // API data
        private static final String API_KEY = System.getenv("GROQ_API_KEY");
        private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
        private static final String MODEL = "llama-3.3-70b-versatile"; // free and fast model on Groq

        private final HttpClient httpClient;

        public AIHandler() {
            this.httpClient = HttpClient.newHttpClient();
        }

        // Sends user's question to Groq and returns the AI's response as a String
        public String ask(String userMessage) {
            try {
                // Build the JSON body manually
                String requestBody = """
                    {
                        "model": "%s",
                        "messages": [
                            {
                                "role": "system",
                                "content": "You are a helpful assistant inside a Java chat application. Keep responses concise and clear."
                            },
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ],
                        "max_tokens": 300
                    }
                    """.formatted(MODEL, escapeJson(userMessage)); //so the json isn't broken with " "

                // Build the HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(BodyPublishers.ofString(requestBody))
                        .build();

                // Send the request and get the response
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

                System.out.println("[AIHandler] Raw response: " + response.body());

                //200 is the code for a successful API call
                if (response.statusCode() == 200) {
                    return parseResponse(response.body());
                } else {
                    System.out.println("[AIHandler] API error: " + response.statusCode());
                    return "AI is unavailable right now (Status: " + response.statusCode() + ")";
                }

            } catch (Exception e) {
                System.out.println("[AIHandler] Error contacting AI: " + e.getMessage());
                return "AI is unavailable right now (Exception: " + e.getMessage() + ")";
            }
        }

        // Extracts the actual text from the JSON response
        // as Groq returns a big JSON object only the content is needed
        private String parseResponse(String json) {
            try {
                String marker = "\"content\":";
                int start = json.lastIndexOf(marker); // Get the last occurrence (mostly the actual message)
                if (start == -1) return "Could not parse AI response.";

                start += marker.length();
                // Move to the first double quote after "content":
                start = json.indexOf("\"", start) + 1;

                // Find the end quote, but skip escaped quotes \"
                int end = start;
                while (end < json.length()) {
                    end = json.indexOf("\"", end);
                    if (json.charAt(end - 1) != '\\') {
                        break;
                    }
                    end++;
                }

                return json.substring(start, end)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
            } catch (Exception e) {
                return "Parsing error: " + e.getMessage();
            }
        }

        // Escapes special characters in the user's message so it doesn't break the JSON
        private String escapeJson(String text) {
            return text.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }