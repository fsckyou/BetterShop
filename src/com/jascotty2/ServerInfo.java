/**
 * Programmer: Jacob Scott
 * Program Name: ServerInfo
 * Description:
 * Date: Apr 18, 2011
 */
package com.jascotty2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * @author jacob
 */
public class ServerInfo {

    public static String suid = null, sip = null;

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
                suid = SUIDmd5Str(ips);
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

    public static String SUIDmd5Str(String txt) throws NoSuchAlgorithmException {
        byte hash[] = MessageDigest.getInstance("MD5").digest(txt.getBytes());
        String ret = "";
        char chars[] = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's',
            'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
            '~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            ',', '.', //'-', '+', ':', ';', '/', '?', '!', '@', '#', '$', '%', '^', '&', '*',
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

    /** reads the server log for Bukkit Version
     * @param includeStart if true, will append a newline a & the last start timestamp
     * @return
     */
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
                Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
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
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error parsing log start date", ex);
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

    public static String[] installedPlugins(Server sv){
        ArrayList<String> enabled = new ArrayList<String>();
        for(Plugin p : sv.getPluginManager().getPlugins()){
            if(p.isEnabled()){
               enabled.add(p.getDescription().getName() + " v" + p.getDescription().getVersion()); 
            }
        }
        return enabled.toArray(new String[0]);
    }
} // end class ServerInfo

