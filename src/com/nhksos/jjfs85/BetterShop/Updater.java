/**
 * Programmer: Jacob Scott
 * Program Name: Updater
 * Description: checks for bettershop updates from my github download page
 * Date: Mar 18, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import com.jascotty2.MySQL.InstallDependency;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author jacob
 */
public class Updater extends InstallDependency {

    public static String downloadPage = "https://github.com/jascotty2/BetterShop/downloads";
    public static String downloadLink = "/downloads/jascotty2/BetterShop/BetterShop.jar";
    public static String altDownloadPage = "https://github.com/BetterShop/BetterShop/downloads";
    public static String altDownloadLink = "/downloads/BetterShop/BetterShop/BetterShop.jar";

    public Updater() {
    } // end default constructor

    public static void check() {
        check(downloadPage, downloadLink);
    }

    public static void checkMain() {
        check(altDownloadPage, altDownloadLink);
    }

    public static void check(String location, String linktofind) {
        isUpToDate(location, linktofind, true);
    }

    public static boolean isUpToDate() {
        return isUpToDate(downloadPage, downloadLink);
    }

    public static boolean isUpToDate(String location, String linktofind) {
        return isUpToDate(downloadPage, downloadLink, false);
    }

    public static boolean isUpToDate(String location, String linktofind, boolean log) {
        InputStream stream = null;
        URLConnection connection;
        try {
            connection = new URL(location).openConnection();
            //connection.setUseCaches(false);
            //long urlLastModified = connection.getLastModified(); // git doesn't report this :(
            int urlFilesize = connection.getContentLength(); // only gets size of header :(
            //System.out.println(urlFilesize);
            // download page
            stream = connection.getURL().openStream();

            if (urlFilesize > 4000) {
                urlFilesize -= 4000;
                // discard head
                stream.read(new byte[4000], 0, 4000);
            }
            // remove tail
            if (urlFilesize > 8000) {
                urlFilesize -= 8000;
            }


            byte dlPage[] = new byte[urlFilesize];
            stream.read(dlPage);
            // slow:
            //for(int bt; (bt=stream.read())!=-1;) file+=(char)bt;
            //for (int i = 0; i < urlFilesize; ++i) file += (char) dlPage[i];
            String file = new String(dlPage);

            // now find the link to search for (as a string.. don't parse for <a href
            int link = file.indexOf(linktofind);
            if (link != -1) {
                //System.out.println(link);
                // found, now look for label with date (will be right after link)
                int title = file.indexOf("title=\"", link);
                if (title > link) { // && file.indexOf("\"", title+8)!=-1){
                    if (file.indexOf(">", title + 8) != -1) {
                        title = file.indexOf(">", title);
                        if (title > link && file.indexOf("<", title + 8) != -1) {
                            // now parse in the date uploaded
                            //2011-03-18 09:47:56"
                            //String dateUploaded = file.substring(title+7, file.indexOf("\"", title+8));
                            //>Fri Mar 18 09:47:56 -0700 2011<
                            String dateUploaded = file.substring(title + 1, file.indexOf("<", title + 1));
                            String uploadComment = "";
                            // check if there is a comment (with version number)
                            int ver = file.indexOf("—", link);
                            if (ver != -1 && ver < title) {
                                // continue until newline or <
                                int nl = file.indexOf("\n", ver);
                                int gt = file.indexOf("<", ver);
                                if (nl > gt && gt > ver) {
                                    nl = gt;
                                }
                                uploadComment = file.substring(ver + 2, nl);
                            }

                            // date this plugin has set
                            DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm Z");
                            Date pluginDate = (Date) formatter.parse(BetterShop.lastUpdatedStr);

                            // date just obtained
                            formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
                            Date uploadDate = (Date) formatter.parse(dateUploaded);

                            // now check the two dates & comment
                            if ((new Date(pluginDate.getTime() + BetterShop.lastUpdated_gracetime * 60000)).before(uploadDate)) {// && (uploadDate.getTime()-pluginDate.getTime())/1000 > BetterShop.lastUpdated_gracetime * 60) {
                                if (log) {
                                    if (uploadComment.length() > 0) {
                                        BetterShop.Log("Newer BetterShop version found on git: " + uploadComment + " (" + uploadDate + ")");
                                    } else {
                                        BetterShop.Log("Newer BetterShop version found on git (" + uploadDate + ")");
                                    }
                                }
                                return false;
                                //BetterShop.Log((new Date(pluginDate.getTime() +BetterShop.lastUpdated_gracetime * 60000)) + " before " + uploadDate);
                            } else if (log) {
                                BetterShop.Log("BetterShop is up-to-date");
                                //BetterShop.Log((new Date(pluginDate.getTime() +BetterShop.lastUpdated_gracetime * 60000)) + " after " + uploadDate);
                            }
                            return true;
                        }
                    }
                }
            }
            if (log) {
                BetterShop.Log("Update failed to find git download to check against");
            }
        } catch (MalformedURLException ex) {
            if (log) {
                BetterShop.Log(Level.WARNING, "unexpected invalid url", ex);
            }
        } catch (IOException ex) {
            if (log) {
                BetterShop.Log(Level.WARNING, "failed to check for updates", ex);
            }
        } catch (Exception ex) {
            if (log) {
                BetterShop.Log(Level.WARNING, "uxexpected error during check for updates", ex);
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    //BetterShop.Log(Level.WARNING, "Failed to close URL InputStream", ex);
                }
            }
        }
        return true;
    }

    public boolean loadNew() {
        /*
        File pluginFile = new File(new File("plugins"), pluginName + ".jar");
        if (pluginFile.isFile()) {
        try {
        Plugin newPlugin = serverPM.loadPlugin(pluginFile);
        if (newPlugin != null) {
        pluginName = newPlugin.getDescription().getName();
        sender.sendMessage("§ePlugin Loaded: §c[" + pluginName + "]");
        serverPM.enablePlugin(newPlugin);
        if (newPlugin.isEnabled()) {
        sender.sendMessage("§ePlugin Enabled: §a[" + pluginName + "]");
        } else {
        sender.sendMessage("§ePlugin §cFAILED§e to Enable:§c[" + pluginName + "]");
        }
        } else {
        sender.sendMessage("§ePlugin §cFAILED§e to Load!");
        }
        } catch (InvalidPluginException ex) {
        sender.sendMessage("§cFile exists but is not a plugin file.");
        } catch (InvalidDescriptionException ex) {
        sender.sendMessage("§cPlugin exists but is invalid.");
        }
        } else {
        sender.sendMessage("§cFile does NOT exist, check name and try again.");
        }
        //*/
        return false;
    }
} // end class Updater

