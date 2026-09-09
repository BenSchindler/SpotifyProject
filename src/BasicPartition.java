import java.util.*;

class BasicPartition implements PlaylistPartitionStrategy{
private static final int AVERAGE_ALBUM_SIZE = 14;
    @Override
    public List<List<Track>> partition(List<Track> tracks, int amountOfDays){
        List<List<Track>> listOfPlaylists = new ArrayList<>();
        HashMap<String,List<Track>> trackMap = new HashMap<>();
        //populate the hashmap
        populateHashMap(trackMap,tracks);

        //sort songs in album
        sortTracksInAlbums(trackMap);
        //cast the trackMap keyset to arraylist, so I can shuffle using Collections.shuffle
        Set<String> albumNames = trackMap.keySet();
        ArrayList<String> shuffledList = new ArrayList<>();
        for(String albumName:albumNames){
            shuffledList.add(albumName);
        }
        Collections.shuffle(shuffledList);
        //for each second day we give some extra space, in case we werent able to reach the sweet spot of tracks.size/amountOfDays
        listOfPlaylists = addSongsToPlaylist(amountOfDays,listOfPlaylists,tracks,shuffledList,trackMap);
        return listOfPlaylists;
    }

    private void populateHashMap(HashMap<String,List<Track>> trackMap,List<Track> tracks){
        for(Track track:tracks){
            if(!trackMap.containsKey(track.getAlbum().getTitle())){
                trackMap.put(track.getAlbum().getTitle(),new ArrayList<>());
            }
            trackMap.get(track.getAlbum().getTitle()).add(track);
        }
    }

    private void sortTracksInAlbums(HashMap<String, List<Track>> trackMap) {
        for (List<Track> albumTracks : trackMap.values()) {
            albumTracks.sort(Comparator.comparingInt(Track::getTrackNumber));
        }
    }

    private List<List<Track>> addSongsToPlaylist(int amountOfDays,List<List<Track>> listOfPlaylists,List<Track> tracks,ArrayList<String> shuffledList,HashMap<String,List<Track>> trackMap){
        //for each second day we give some extra space, in case we werent able to reach the sweet spot of tracks.size/amountOfDays
        int sizeOfPlaylist;
        for(int i=0;i<amountOfDays;i++){
            listOfPlaylists.add(new ArrayList<>());
            if(i%2==0){ //for even days
                sizeOfPlaylist = (tracks.size()/amountOfDays)+AVERAGE_ALBUM_SIZE;
            }
            else{ //for odd days
                sizeOfPlaylist = (tracks.size()/amountOfDays);
            }
            //while the size of current playlist, in the i'th day is smaller than what we allow
            while(!shuffledList.isEmpty() && listOfPlaylists.get(i).size()<=sizeOfPlaylist){
                //amount of songs left to add
                int delta = sizeOfPlaylist-listOfPlaylists.get(i).size();
                //check if we can add the album as a whole
                if(trackMap.get(shuffledList.get(0)).size()<=delta) {
                    for (Track track : trackMap.get(shuffledList.get(0))) {
                        listOfPlaylists.get(i).add(track);
                    }
                    shuffledList.remove(0);
                }
                else{
                    break;
                }
            }
        }
        //dump the remainder into the last day's playlist so no tracks are lost
        if (!shuffledList.isEmpty()) {
            List<Track> lastDay = listOfPlaylists.get(amountOfDays - 1);
            for (String leftoverAlbum : shuffledList) {
                lastDay.addAll(trackMap.get(leftoverAlbum));
            }
        }
        return listOfPlaylists;
    }



}

