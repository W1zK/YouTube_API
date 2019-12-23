import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class Search {
    private static final String PROPERTIES_FILENAME = "/resources/youtube.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Enter NAME");
        String finder = scanner.nextLine();
        connectClient(finder);
    }


    private static void connectClient(String queryTerm) {
        YouTube youtube;
        Properties properties = new Properties();
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-search").build();
        try {
            InputStream in = Search.class.getResourceAsStream(PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
        YouTube.Search.List search = null;
        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apiKey = properties.getProperty("youtube.apikey");
        if (search != null) {
            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/publishedAt)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse searchResponse = null;
            try {
                searchResponse = search.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<SearchResult> searchResultList;
            if (searchResponse != null) {
                searchResultList = searchResponse.getItems();
                prettyPrint(searchResultList.iterator(), queryTerm);
            }
        }
    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();



            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();


                System.out.println(" Video Id: " + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Channel Title: " + singleVideo.getSnippet().getChannelTitle());
                System.out.println(" Published date : " + singleVideo.getSnippet().getPublishedAt());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }


}
