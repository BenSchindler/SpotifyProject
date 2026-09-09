import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
class PlaylistOrchestrator {

    private SpotifyApiClient client;
    private SpotifyAuthenticator auth;
    private PlaylistPartitionStrategy strategy;

    public PlaylistOrchestrator(SpotifyAuthenticator auth, SpotifyApiClient client, PlaylistPartitionStrategy strategy) {
        this.auth = auth;
        this.client = client;
        this.strategy = strategy;
    }

    /*The master method. It will trigger authentication, fetch the songs via the client, pass them to the strategy, and finally loop through the resulting lists to tell the client to create and populate the playlists.*/
    public void execute(int amountOfDays){
        String authUrl = auth.generateAuthUrl();
        System.out.println("Please visit this link in your browser to authorize: "+authUrl);
        auth.fetchAndSetTokens();
        String accessToken = auth.getAccessToken();
        List<Track> likedSongs = client.fetchAllLikedSongs(accessToken);
        List<List<Track>> partitionedPlaylists = strategy.partition(likedSongs, amountOfDays);
        for(int i=0;i<amountOfDays;i++){
            List<Track> dailyTracks = partitionedPlaylists.get(i);
            // tell the client to create an empty playlist and get its new ID
            String playlistName = "My Playlist - Day " + (i + 1);
            String playlistId = client.createEmptyPlaylist(accessToken, playlistName);

            // extract the URIs from the Track objects
            List<String> trackUris = new ArrayList<>();
            for (Track track : dailyTracks) {
                trackUris.add(track.getUri()); // get the URI required to populate the playlist[
            }

            //  populate the playlist
            client.addTracksToPlaylist(accessToken, playlistId, trackUris);
            System.out.println("added playlist number " +(i+1));
        }
    }
}


