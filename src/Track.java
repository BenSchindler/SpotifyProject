public class Track {
    private String uri;
    private String name;
    private Album album;
    private int trackNumber;

    public Track(String uri, String name, Album album, int trackNumber) {
        this.uri = uri;
        this.name = name;
        this.album = album;
        this.trackNumber = trackNumber;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public Album getAlbum() {
        return album;
    }

    public int getTrackNumber() {
        return trackNumber;
    }
}
