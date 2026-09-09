public class Main {
    public static void main(String[] args) {

        // 1. Set Default Values
        int amountOfDays = 7;
        PlaylistPartitionStrategy partitionStrategy = new BasicPartition();

        // 2. Parse Program Arguments (if provided)
        if (args.length > 0) {
            try {
                amountOfDays = Integer.parseInt(args[0]); // First argument: days
            } catch (NumberFormatException e) {
                System.out.println("Invalid number of days provided. Using default: " + amountOfDays);
            }
        }

        if (args.length > 1) {
            String strategyName = args[1].toLowerCase(); // Second argument: strategy name
            if (strategyName.equals("basic")) {
                partitionStrategy = new BasicPartition();
            }
            // If you add more strategies later, you just add an 'else if' here:
            // else if (strategyName.equals("advanced")) { partitionStrategy = new AdvancedPartition(); }
            else {
                System.out.println("Unknown strategy '" + strategyName + "'. Defaulting to BasicPartition.");
            }
        }

        // 3. Instantiate your core components
        SpotifyAuthenticator auth = new SpotifyAuthenticator();
        SpotifyApiClient client = new SpotifyApiClient();

        // 4. Inject the components into the Orchestrator
        PlaylistOrchestrator orchestrator = new PlaylistOrchestrator(auth, client, partitionStrategy);

        // 5. Execute the master flow
        System.out.println("Starting Spotify Playlist Generator for " + amountOfDays + " days...");
        orchestrator.execute(amountOfDays);
    }
}