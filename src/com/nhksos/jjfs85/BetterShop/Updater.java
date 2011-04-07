/**
 * Programmer: Jacob Scott
 * Program Name: Updater
 * Description: checks for bettershop updates from my github download page
 * Date: Mar 18, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jacob
 */
public class Updater {

    public static String downloadPage = "https://github.com/BetterShop/BetterShop/downloads";
    public static String downloadLink = "/downloads/BetterShop/BetterShop/BetterShop.jar";
    // for stats purposes, open the normal page that counts the downloads
    public static String downloadLinkCounter = "https://github.com/downloads/BetterShop/BetterShop/BetterShop.jar";
    public static String downloadLinkUrl = "http://cloud.github.com/downloads/BetterShop/BetterShop/BetterShop.jar";
    
    public static String altDownloadPage = "https://github.com/jascotty2/BetterShop/downloads";
    public static String altDownloadLink = "/downloads/jascotty2/BetterShop/BetterShop.jar";
    public static String altDownloadLinkCounter = "https://github.com/downloads/jascotty2/BetterShop/BetterShop.jar";
    public static String altDownloadLinkUrl = "http://cloud.github.com/downloads/jascotty2/BetterShop/BetterShop.jar";
    public static String suid = null, sip = null;

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

    public static boolean isUpToDate(boolean log) {
        return isUpToDate(downloadPage, downloadLink, log);
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
                            DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm Z", Locale.US);
                            Date pluginDate = (Date) formatter.parse(BetterShop.lastUpdatedStr);

                            // date just obtained
                            formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
                            Date uploadDate = (Date) formatter.parse(dateUploaded);

                            // now check the two dates & comment
                            if ((new Date(pluginDate.getTime() + BetterShop.lastUpdated_gracetime * 60000)).before(uploadDate)) {// && (uploadDate.getTime()-pluginDate.getTime())/1000 > BetterShop.lastUpdated_gracetime * 60) {

                                if (uploadComment.length() > 0) {
                                    if (uploadComment.trim().equalsIgnoreCase(BetterShop.pdfFile.getVersion())) {
                                        if (log) {
                                            BetterShop.Log("BetterShop is (likely) up-to-date (version matches comment)");
                                        }
                                        return true;
                                    } else {
                                        // double-check against this file
                                        File jar = getJarFile();
                                        //System.out.println(jar);
                                        if (jar != null && jar.exists()) { // double-check got the file
                                            if (!(new Date(jar.lastModified() + BetterShop.lastUpdated_gracetime * 60000)).before(uploadDate)) {
                                                if (log) {
                                                    //System.out.println("file is newer than on git");
                                                    BetterShop.Log("File is newer than on git");
                                                }
                                                return true;
                                            }
                                        }

                                        if (log) {
                                            BetterShop.Log("Newer BetterShop version found on git: " + uploadComment + " (" + uploadDate + ")");
                                        }
                                    }
                                } else if (log) {
                                    BetterShop.Log("Newer BetterShop version found on git (" + uploadDate + ")");
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
                BetterShop.Log(Level.WARNING, "unexpected invalid url");//, ex);
            }
        } catch (IOException ex) {
            if (log) {
                BetterShop.Log(Level.WARNING, "failed to check for updates");//, ex);
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

    public static File getJarFile() {
        return new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().getPath().
                replace("%20", " ").replace("%25", "%"));
    }

    public static boolean downloadUpdate() {
        File jar = getJarFile();
        if (jar.exists()) {
            try {
                downloadUpdate(downloadLinkUrl, jar.getAbsolutePath(), downloadLinkCounter);
                return true;
            }/* catch (MalformedURLException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }*/ catch (Exception ex) {
                BetterShop.Log(Level.SEVERE, "Failed to download update", ex, false);
            }
        }
        return false;
    }

    protected static synchronized void downloadUpdate(String location, String filename, String counterLink) throws MalformedURLException, IOException {
        URLConnection connection = new URL(location).openConnection();
        if (counterLink != null && counterLink.length() > 0) {
            try {
                // for stats purposes, open the normal page that counts the downloads
                URLConnection cconn = (new URL(counterLink)).openConnection();
                cconn.getContent();
            } catch (Exception e) {
            }
        }

        connection.setUseCaches(false);

        InputStream in = connection.getInputStream();
        OutputStream out = new FileOutputStream(filename); //  + "_new"

        byte[] buffer = new byte[65536];
        //int currentCount = 0;
        for (int count; (count = in.read(buffer)) >= 0;) {
            out.write(buffer, 0, count);
            //currentCount += count;
        }

        in.close();
        out.close();
    }

    /*
    public boolean loadNew() {
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
    return false;
    }
    //*/
    public static String serverUID() {
        return serverUID(true, -1);
    }

    public static String serverUID(boolean useMask) {
        return serverUID(useMask, -1);
    }

    public static String serverUID(boolean useMask, int maxlen) {
        if (suid == null) {

            String ips = "";
            try {
                // Obtain the InetAddress of the computer on which this program is running
                InetAddress localaddr = InetAddress.getLocalHost();
                ips = localaddr.getHostName();
                try {
                    URL autoIP = new URL("http://www.whatismyip.com/automation/n09230945.asp");
                    BufferedReader in = new BufferedReader(new InputStreamReader(autoIP.openStream()));
                    ips += ":" + (in.readLine()).trim();

                } catch (Exception e) {
                    ips += ":ukpip";
                }
                for (InetAddress i : InetAddress.getAllByName(localaddr.getHostName())) {
                    if (!i.isLoopbackAddress()) {
                        ips += ":" + i.getHostAddress();
                    }
                }
            } catch (Exception ex) {
                ips += ":ukh";
            }
            sip = ips;
            try {
                suid = md5Str(ips);
            } catch (Exception ex) {
                //Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage(), ex);
                suid = ips;
            }
            if (maxlen > 0 && sip.length() > maxlen) {
                sip = sip.substring(0, maxlen);
            }
        }
        return useMask ? suid : sip;
    }

    public static String md5Str(String txt) throws NoSuchAlgorithmException {
        byte hash[] = MessageDigest.getInstance("MD5").digest(txt.getBytes());
        String ret = "";
        char chars[] = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's',
            'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
            '~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '-', '+', ':', ';', ',', '.', '/', '?', '!', '@', '#', '$', '%', '^', '&', '*',
            'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S',
            'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};
        for (byte b : hash) {
            ret += chars[((int) b + 255) % chars.length];
        }
        return ret;
    }

    public static String getBukkitVersion() {
        return getBukkitVersion(false);
    }

    // reads the server log for this info
    public static String getBukkitVersion(boolean includeStart) {
        File slog = new File("server.log");
        if (slog.exists() && slog.canRead()) {
            FileReader fstream = null;
            try {
                String ver = "";
                fstream = new FileReader(slog.getAbsolutePath());
                BufferedReader in = new BufferedReader(fstream);

                String line = "";
                while ((line = in.readLine()) != null) {
                    if (line.contains("This server is running Craftbukkit version git-Bukkit-")) {
                        ver = line;
                    }
                }
                if (ver.length() > 0) {
                    return !includeStart ? ver.substring(ver.indexOf("git-Bukkit-"))
                            : ver.substring(ver.indexOf("git-Bukkit-")) + "\nStartTime: " + ver.substring(0, 19)
                            + "  (" + serverRunTimeSpan(ver.substring(0, 19)) + ")";
                } else {
                    return "?";
                }
            } catch (Exception ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                }
            }

        }
        return "?";
    }

    public static String serverRunTimeSpan(String startTime) {
        Date uploadDate = null;
        try {
            // 2011-04-01 21:35:22
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            uploadDate = formatter.parse(startTime.trim());
        } catch (ParseException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, "Error parsing log start date", ex);
            return ex.getMessage();
        }
        long sec = ((new Date()).getTime() - uploadDate.getTime()) / 1000;
        int mon = (int) (sec / 2592000);
        sec -= mon * 2592000;

        int day = (int) (sec / 86400);
        sec -= day * 86400;

        int hr = (int) (sec / 3600);
        sec -= hr * 3600;

        int min = (int) (sec / 60);
        sec = sec % 60;

        String timeSpan = "";
        if (mon > 0) {
            timeSpan += mon + " Months, ";
        }
        if (day > 0) {
            timeSpan += day + " Days, ";
        }
        if (hr > 0) {
            timeSpan += hr + " Hours, ";
        }
        if (min > 0) {
            timeSpan += min + " Minutes, ";
        }
        return timeSpan + sec + " Sec";
    }
} // end class Updater

