package com.example.redditwallpaper;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * Modified from:
 *
 * RedditImageGrabber.java
 *
 * An image downloader for the popular website Reddit
 * Supply it with settings in settings.txt
 *
 * @author  Adam Leslie
 * @version 1.3
 * @since   2/1/2015
 *
 */

// Saves n images from a subreddit's rss feed
// It is recommended to pipe this programs output to a text file if used regularly
public class RedditImageGrabber {

    private static final String LINK_PATTERN = "<span><a href=\\\"https?:\\/\\/\\S+\\\">\\[link\\]";
    private static final String NAME_REPLACE_PATTERN = "[~#%*{}\\:<>?/+|\". ]";
    private static final int CHUNK_SIZE = 2048;
    private static final String USER_AGENT = "Reddit Wallpaper App";

    // Loaded settings
    private static int NUM_TO_DL = 1; // Max of 25 (Reddit page limit)
    private static boolean ENABLE_TITLES = false;
    private static String[] VALID_TYPES = {".jpg", ".png", ".gif"};

    private static String SUBREDDIT;
    private static String DL_PATH;
    private static boolean FORCE_PORTRAIT;
    private static boolean FORCE_HD;
    private static int SCREEN_HEIGHT;
    private static int SCREEN_WIDTH;
    public static LinkedList<String> prevURLs = new LinkedList<String>();

    public RedditImageGrabber(String subreddit, String dl_path,
                              boolean portrait, boolean hd,
                              int height, int width,
                              LinkedList<String> URLs) {
        SUBREDDIT = subreddit;
        DL_PATH = dl_path;
        FORCE_PORTRAIT = portrait;
        FORCE_HD = hd;
        SCREEN_HEIGHT = height;
        SCREEN_WIDTH = width;
        prevURLs = URLs;
    }


    public static String getImage() throws SAXException, IOException, ParserConfigurationException {

        SUBREDDIT = "/r/" + SUBREDDIT;
        String dlPath = "";

        System.out.println("Getting " + NUM_TO_DL + " images from " + SUBREDDIT);

        // construct the XML document tree to navigate
        Document rss = null;

        // 429 error handling due to high amounts of reddit requests
        boolean error429 = true;
        while(error429) {
            try {
                rss = getRSSDocument("https://reddit.com" + SUBREDDIT + ".rss");
                error429 = false;
            } catch (IOException e) {
                String error = "Accessing Reddit's RSS failed: \n\t" + e.getLocalizedMessage();
                if(!error.contains("429")){
                    error429=false;
                }

            }
        }


        // empty or create dl path
        File dlFile = new File(DL_PATH);
        if(dlFile.exists())
            FileUtils.cleanDirectory(dlFile);
        else
            dlFile.mkdirs();
        NodeList items = rss.getElementsByTagName("content");
        NodeList titles = rss.getElementsByTagName("title");
        System.out.println(titles.item(0).getTextContent());    // print out title of subreddit
        System.out.println();

        String link = null;
        String title = null;
        // iterate through the list of submissions, until limit reached or no more to dl
        for(int i = 0, saved = 0; saved < NUM_TO_DL && i < items.getLength(); i++) {
            System.out.println(i + 1);
            link = getLink(items.item(i).getTextContent());

            // check if file already downloaded
            if(prevURLs.contains(link)) {
                System.out.println("FILE ALREADY DOWNLOADED");
            } else {
                // Fix irregular filenames
                if(ENABLE_TITLES)
                    title = titles.item(i + 1).getTextContent().replaceAll(NAME_REPLACE_PATTERN, "_");

                // Test if file is of a recognized filetype
                String fileType = null;
                for(int j = 0; fileType == null && j < VALID_TYPES.length; j++) {
                    if(link.endsWith(VALID_TYPES[j])) {
                        fileType = VALID_TYPES[j];
                    }
                }
                if(fileType == null) {
                    // the link is one we can not download
                    System.out.println("UNSUPPORTED FILETYPE");
                } else {
                    // the link is one we can download
                    dlPath = DL_PATH + "bg" + (saved + 1) + (ENABLE_TITLES ? ("_" + title) : "" ) + fileType;
                    boolean saveValid = saveImage(link, dlPath);

                    // if we failed to save with title enabled, try with it disabled
                    if(!saveValid && ENABLE_TITLES) {
                        System.out.println("DOWNLOAD FAILED: Trying short file name");
                        dlPath = DL_PATH + (saved + 1) + fileType;
                        saveValid = saveImage(link, dlPath);
                    }
                    if(saveValid) {
                        prevURLs.addLast(link);
                        saved++;
                        System.out.println("Saved to: " + dlPath);
                    }
                }
            }

            System.out.println();
        }

        return dlPath;
    }

    // saves an image from a url chunk by chunk to a destination file. Returns true if successfully saved image
    private static boolean saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url ;
        InputStream in;
        FileOutputStream out;
        try {
            url = new URL(imageUrl);
            // use a URLConnection with a spoofed user-agent to bypass HTTP 403 response code
            URLConnection con = url.openConnection();
            con.addRequestProperty("User-Agent", USER_AGENT);
            in = con.getInputStream();
            out = new FileOutputStream(destinationFile);
        } catch (IOException e) {
            System.out.println("Caught IOException: " + e.getMessage());
            return false;
        }

        byte[] b = new byte[CHUNK_SIZE];
        int length;

        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }

        in.close();
        out.close();

        Bitmap bitmap;
        File Image = new File(destinationFile);

        bitmap = BitmapFactory.decodeFile(Image.getAbsolutePath());
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();


        if (FORCE_PORTRAIT && (w>h)) {
            File f = new File(destinationFile);
            f.delete();
            return false;
        }

        if (FORCE_HD && (h < SCREEN_HEIGHT || w < SCREEN_HEIGHT)) {
            File f = new File(destinationFile);
            f.delete();
            return false;
        }

        return true;
    }

    // returns the link to an image given a link pattern specified for the reddit RSS feed
    private static String getLink(String description) {
        Pattern pattern = Pattern.compile(LINK_PATTERN);
        Matcher matcher = pattern.matcher(description);
        matcher.find();
        String link = matcher.group();
        if(link == null) {
            System.out.println("DESCRIPTION: " + description);
            return "LINK NOT FOUND";
        }
        link = link.substring(15, link.length() - 8); // Reduces string to valid link text
        System.out.println("Link: " + link);
        return link;
    }

    // gets the XML/RSS document tree for the given url
    public static Document getRSSDocument(String url) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = null;
        doc = dBuilder.parse(url);

        return doc;
    }

}
