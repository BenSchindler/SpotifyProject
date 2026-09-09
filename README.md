# Spotify Playlist Partition Generator

A Java application that interfaces directly with the Spotify Web API using OAuth 2.0 PKCE/Authorization Code flow. It fetches a user's entire library of "Liked Songs" and intelligently partitions them into balanced, multi-day playlists while keeping full albums grouped and ordered.

---

## Architecture & Design Patterns

* **Strategy Pattern (`PlaylistPartitionStrategy`):** Decouples playlist creation logic from API mechanics, allowing different partitioning algorithms (e.g., `BasicPartition`) to be swapped or extended at runtime.
* **Orchestrator Pattern (`PlaylistOrchestrator`):** Coordinates authentication, data retrieval, partitioning calculations, and playlist publication sequentially.
* **Local HTTP Loopback Server (`SpotifyAuthenticator`):** Spawns an internal HTTP server (`com.sun.net.httpserver.HttpServer`) on port `8000` to capture OAuth authorization codes directly from browser redirects via `CompletableFuture`.
* **Batch Request Handling (`SpotifyApiClient`):** Implements pagination across Spotify endpoints (fetching liked tracks in blocks of 50, and uploading tracks to new playlists in batches of 100).

---

## Dependencies & Libraries

* **Java Standard Library (JDK 11+):**
  * `java.net.http.HttpClient` — Modern non-blocking HTTP requests.
  * `com.sun.net.httpserver.HttpServer` — Lightweight built-in local redirect receiver.
  * `java.util.concurrent.CompletableFuture` — Asynchronous code capture.
* **JSON-Java (`org.json`):**
  * Bundled inside `lib/json-20260522.jar` for parsing Spotify API JSON payloads and responses.

---

## Setup & Prerequisites

### 1. Spotify Developer Dashboard Setup
To connect this app to Spotify, you must register a free application:
1. Log in to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Click **Create App**.
3. Fill in the app name and description (e.g., `Playlist Generator`).
4. Set the **Redirect URI** to:
   ```text
   http://127.0.0.1:8000/callback
   ```
5. Save the app, go to **Settings**, and locate your **Client ID** and **Client Secret**.

### 2. Configure Credentials in Code
Open `src/SpotifyAuthenticator.java` and paste your Spotify credentials into the class constants:

```java
private static final String CLIENT_ID = "YOUR_CLIENT_ID_HERE";
private static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET_HERE";
```

*(Note: Never commit your actual keys back to public Git).*

---

## Compilation

Open your terminal in the root directory of the project:

**On Windows (CMD / PowerShell):**
```cmd
javac -cp "lib/*" -d out src/*.java
```

**On macOS / Linux:**
```bash
javac -cp "lib/*" -d out src/*.java
```

---

## Running the Application

Run the application using the following commands:

**On Windows (CMD / PowerShell):**
```cmd
java -cp "out;lib/*" Main [amountOfDays] [strategy]
```

**On macOS / Linux:**
```bash
java -cp "out:lib/*" Main [amountOfDays] [strategy]
```

### Command Line Arguments
* `[amountOfDays]` *(Optional, Default: `7`)*: The number of daily playlists to generate.
* `[strategy]` *(Optional, Default: `basic`)*: The partitioning strategy name (currently supports `basic`).

### Example Execution
```cmd
java -cp "out;lib/*" Main 5 basic
```

---

## User Workflow During Execution

1. **Launch:** The app prints an authorization URL in the terminal:
   ```text
   Please visit this link in your browser to authorize: https://accounts.spotify.com/authorize/?client_id=...
   ```
2. **Authorize:** Open the printed URL in your web browser and click **Agree**.
3. **Capture:** The browser automatically redirects to `http://127.0.0.1:8000/callback`. The internal server captures the authorization token and completes the handshake.
4. **Processing:** The app downloads your liked songs, clusters them into album blocks, and distributes them evenly across the specified number of days.
5. **Output:** Playlists named `My Playlist - Day 1`, `My Playlist - Day 2`, etc., are created and populated directly in your Spotify account.
