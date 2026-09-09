import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.json.JSONArray;

public class SpotifyAuthenticator {
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private HttpClient client;
    private HttpServer server;
    private String accessToken;

    public SpotifyAuthenticator(){
        client = HttpClient.newHttpClient();
    }


    /*Returns the constructed accounts.spotify.com/authorize URL.*/
    public String generateAuthUrl(){
        return "https://accounts.spotify.com/authorize/?client_id="+CLIENT_ID+"&response_type=code&redirect_uri=http://127.0.0.1:8000/callback&scope=user-library-read%20playlist-modify-private";
    }

    /*Exposes the token so the API client can use it.*/
    public String getAccessToken(){
        return accessToken;
    }

    /*Starts the local HTTP server on port 8000, blocks until the browser redirects, captures the code, shuts down the server, and returns the code.*/
    public String listenForAuthCode(){
        CompletableFuture<String> container = startLocalServer();
        String code = container.join();
        server.stop(0);
        return code;
    }


    /*Makes the POST request to exchange the code for an access token and stores it internally.*/
    public void fetchAndSetTokens(){
        String code = listenForAuthCode();
        String clientId = CLIENT_ID; //EDIT THE CONSTANTS SO THIS WORKS
        String clientSecret = CLIENT_SECRET; //EDIT THE CONSTANTS SO THIS WORKS
        String encodedAuthorization = clientId + ":" + clientSecret;
        String payload = "grant_type=authorization_code&code=" + code + "&redirect_uri=http://127.0.0.1:8000/callback";
        String encodedText= Base64.getEncoder().encodeToString(encodedAuthorization.getBytes());

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://accounts.spotify.com/api/token")).headers
                ("Authorization","Basic "+encodedText,"Content-Type","application/x-www-form-urlencoded").POST
                (HttpRequest.BodyPublishers.ofString(payload)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            accessToken = jsonObject.getString("access_token");
        }
        catch (Exception e){
            System.out.println("Caught exception while attempting to fetch tokens");
        }
    }

    private CompletableFuture<String> startLocalServer(){
        try{
            server = HttpServer.create(new InetSocketAddress(8000),0);
            CompletableFuture<String> future = new CompletableFuture<>();
            server.createContext("/callback",new HttpHandler(){
                @Override
                public void handle(HttpExchange t) {
                    URI uri = t.getRequestURI();
                    String query = uri.getQuery();
                    String foundParam;
                    String code;
                    if (query != null) {
                        String[] separatedQuery = query.split("&");
                        for (String parameter : separatedQuery) {
                            if (parameter.contains("code=")) {
                                foundParam = parameter;
                                code = foundParam.split("=")[1];
                                try {
                                    t.sendResponseHeaders(200, code.length());
                                    t.getResponseBody().write(code.getBytes());
                                    t.close();
                                } catch (Exception e) {
                                    System.out.println("Caught exception while sending response headers");
                                }
                                future.complete(code);
                                break;
                            }
                        }
                    }
                }
            });
            server.setExecutor(null);
            server.start();
            return future;
        } catch (IOException e) {
            System.out.println("Caught exception while trying to start local server");
            return null;
        }
    }
}
