import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import org.json.JSONObject;
import org.json.JSONArray;


public class SpotifyApiClient {

    private HttpClient client;
    public SpotifyApiClient() {
        client = HttpClient.newHttpClient();
    }

    /*Fetches and returns your user ID, which is required to create a playlist.*/
    public String getCurrentUserId(String accessToken){

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.spotify.com/v1/me")).header("Authorization", "Bearer"+accessToken).GET().build();
        try{
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            String JsonResponse = response.body();
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + JsonResponse);
            String[] words = JsonResponse.split("\\s+");
            boolean found = false;
            for(String word : words){
                if(found){ //this lets us get the next string after id
                    return word; //this is the id
                }
                if(word.equals("id")){
                    found=true;
                }
            }
        }
        catch(Exception e){
            System.out.println("caught exception while sending authorization request");
        }

        return null;
    }

    /*Handles the GET request. It must loop through the pagination internally, accumulating and returning one massive list of Track objects.*/
    public List<Track> fetchAllLikedSongs(String accessToken){
        List<Track> allTracks = new ArrayList<>();
        String next = "https://api.spotify.com/v1/me/tracks?limit=50"; //first 50 songs
        while(next!=null){
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(next)).header("Authorization", "Bearer "+accessToken).GET().build();
            try{
                HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
                System.out.println("Liked Songs Status: " + response.statusCode());
                System.out.println("Liked Songs Body: " + response.body());
                String JsonResponse = response.body(); //JSON body of the response from server
                JSONObject jsonObject = new JSONObject(JsonResponse);
                JSONArray items = jsonObject.getJSONArray("items");
                for(int i = 0; i < items.length(); i++){
                    Track track = createTrack(items,i);
                    allTracks.add(track); //add the song to the list
                }
                //check if we have more pages
                if(jsonObject.isNull("next")){ //avoid errors
                    next=null;
                }
                else{
                    next=jsonObject.getString("next");
                }
            }
            catch(Exception e){
                System.out.println("caught exception while trying to get liked songs");
                e.printStackTrace();
                break;
            }
        }
        return allTracks;
    }

    /* Makes the POST request to create a playlist and returns its new Spotify ID*/
    public String createEmptyPlaylist(String accessToken, String playlistName){
        String jsonPayload = "{\"name\": \""+playlistName+"\"," +
                " \"description\": \"...\"," +
                " \"public\": false}";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.spotify.com/v1/me/playlists")).headers
                ("Authorization", "Bearer "+accessToken, "Content-Type","application/json").POST(BodyPublishers.ofString(jsonPayload)).build();
        try{
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            System.out.println("Liked Songs Status: " + response.statusCode());
            System.out.println("Liked Songs Body: " + response.body());
            String jsonResposne = response.body();
            JSONObject jsonObject = new JSONObject(jsonResposne);
            return jsonObject.getString("id");
        }
        catch(Exception e){
            System.out.println("caught exception while trying to create playlist");
        }
        return null;
    }

    /*Makes the POST request to populate the playlist with uris.*/
    public void addTracksToPlaylist(String accessToken, String playlistId, List<String> trackUris){
        String fullUrl = "https://api.spotify.com/v1/playlists/"+playlistId+"/items";
        for(int i=0;i<(int)Math.ceil(trackUris.size()/100.0); i++){
            List<String> currentUris = new ArrayList<>();
            for(int j=0; j<100 && (j + 100 * i) < trackUris.size(); j++) {
                currentUris.add(trackUris.get(j + 100 * i));
            }
            String jsonPayload = concatenateUris(currentUris);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fullUrl)).
                    headers("Authorization", "Bearer "+accessToken, "Content-Type","application/json")
                    .POST(BodyPublishers.ofString(jsonPayload)).build();
            try{
                HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            }
            catch(Exception e){
                System.out.println("caught exception while trying to add an item to a playlist");
            }

        }
    }

    /* concatenate Uris in the required format */
    private String concatenateUris(List<String> uris){
        String jsonPayload = "{\n\"uris\":[\n";
        for (int k = 0; k < uris.size(); k++) {
            String uri = uris.get(k);
            if (!uri.startsWith("spotify:track:")) {
                uri = "spotify:track:" + uri;
            }
            jsonPayload =jsonPayload+"    \"" + uri + "\"";
            // add "," if needed
            if (k < uris.size() - 1) {
                jsonPayload += ",\n";
            } else {
                jsonPayload += "\n";
            }
        }
        jsonPayload = jsonPayload+"  ]\n}";
        return jsonPayload;
    }
    /* create track from JSON fields*/
    private Track createTrack(JSONArray items, int i){
        JSONObject currentObject = items.getJSONObject(i);
        String albumId = currentObject.getJSONObject("track").getJSONObject("album").getString(("id"));
        String albumName = currentObject.getJSONObject("track").getJSONObject("album").getString("name");
        Album album = new Album(albumId, albumName);
        String uri = currentObject.getJSONObject("track").getString("uri");
        String songName =  currentObject.getJSONObject("track").getString("name");
        int trackNumber = currentObject.getJSONObject("track").getInt("track_number");
        Track track = new Track(uri,songName,album,trackNumber);
        return track;
    }
}
