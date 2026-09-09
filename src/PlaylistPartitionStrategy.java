import java.util.List;

public interface PlaylistPartitionStrategy {

    /*Takes the raw list of tracks and distributes them into a list of playlists (where each playlist is a List<Track>).*/
    public List<List<Track>> partition(List<Track> tracks,int amountOfDays);
}
