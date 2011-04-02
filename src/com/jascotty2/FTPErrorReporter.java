/**
 * Programmer: Jacob Scott
 * Program Name: FTPErrorReporter
 * Description:
 * Date: Mar 29, 2011
 */
package com.jascotty2;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * @author jacob
 */
public class FTPErrorReporter {

    private static String user = "bettershopftp",
            pass = "5ZVm9406h8u5";
    protected static String ftpHost = "nas.boldlygoingnowhere.org";
    protected static FTPupload uploader = null;
    protected static int filesizeLimit = 2000; // yes, i know it's actually 2048

    public static String SendNewText(String txt) {
        // http://www.kodejava.org/examples/356.html
        //FTPClient client = new FTPClient(); // import org.apache.commons.net.ftp.FTPClient;
        if (uploader == null) {
            uploader = new FTPupload(ftpHost, user, pass);
        }
        if (txt.length() > filesizeLimit) {
            txt = txt.substring(0, filesizeLimit);
        }
        String fn = String.valueOf((new Date()).getTime()).substring(3) + Rand.randFname(7, 15);
        //String fn = Rand.randFname(15, 25);
        if (uploader.uploadText(txt, fn)) {
            return fn;
        }else{
            return null;
        }
    }

    // http://www.programmers-corner.com/sourcecode/142
    // FTPupload.java by Rowland http://www.home.comcast.net/~rowland3/
    // Upload a file via FTP, using the JDK.
    public static class FTPclientConn {

        public final String host;
        public final String user;
        protected final String password;
        protected URLConnection urlc;

        public FTPclientConn(String _host, String _user, String _password) {
            host = _host;
            user = _user;
            password = _password;
            urlc = null;
        }

        protected URL makeURL(String targetfile) throws MalformedURLException {
            if (user == null) {
                return new URL("ftp://" + host + "/" + targetfile + ";type=i");
            } else {
                return new URL("ftp://" + user + ":" + password + "@" + host + "/" + targetfile + ";type=i");
            }
        }

        protected InputStream openDownloadStream(String targetfile) throws Exception {
            URL url = makeURL(targetfile);
            urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            return is;
        }

        protected OutputStream openUploadStream(String targetfile) throws Exception {
            URL url = makeURL(targetfile);
            urlc = url.openConnection();
            OutputStream os = urlc.getOutputStream();
            return os;
        }

        protected void close() {
            urlc = null;
        }
    }

    public static class FTPupload {

        protected FTPclientConn cconn = null;

        /**
         * create an ftp uploader, logging in & starting a connection
         * @author jacob
         * @param _host
         * @param _user
         * @param _password
         */
        public FTPupload(String _host, String _user, String _password) {
            cconn = new FTPclientConn(_host, _user, _password);
        }

        public boolean uploadText(String text, String filename) {
            OutputStream os = null;
            InputStream is = null;
            try {
                os = cconn.openUploadStream(filename);
                is = new ByteArrayInputStream(text.getBytes("UTF-8"));
                byte[] buf = new byte[16384];
                int c;
                while (true) {
                    //System.out.print(".");
                    c = is.read(buf);
                    if (c <= 0) {
                        break;
                    }
                    //System.out.print("[");
                    os.write(buf, 0, c);
                    //System.out.print("]");
                }
                return true;
            } catch (Exception ex) {
                //Logger.getLogger(FTPErrorReporter.class.getName()).log(Level.SEVERE, "Error uploading text file", ex);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    cconn.close(); // section 3.2.5 of RFC1738
                } catch (IOException ex) {
                }
            }
            return false;
        }

        public void uploadFile(String localfile, String targetfile) {
            try {
                OutputStream os = cconn.openUploadStream(targetfile);
                FileInputStream is = new FileInputStream(localfile);
                byte[] buf = new byte[16384];
                int c;
                while (true) {
                    //System.out.print(".");
                    c = is.read(buf);
                    if (c <= 0) {
                        break;
                    }
                    //System.out.print("[");
                    os.write(buf, 0, c);
                    //System.out.print("]");
                }
                os.close();
                is.close();
                cconn.close(); // section 3.2.5 of RFC1738
            } catch (Exception E) {
                System.err.println(E.getMessage());
                E.printStackTrace();
            }
        }
    }
} // end class FTPErrorReporter

