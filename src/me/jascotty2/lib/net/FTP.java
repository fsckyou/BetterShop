/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: gives methods to upload text files to an ftp server
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.lib.net;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

//import org.apache.commons.net.ftp.FTPClient;
/**
 * @author jacob
 */
public class FTP {

	private String user = "", pass = "";
	protected String ftpHost = "";
	// max filesize the ftp host will accept
	protected int filesizeLimit = 2500; // yes, i know it's actually 2560
	private FTPupload uploader = null;

	public FTP(String username, String password, String hostname) {
		this(username, password, hostname, -1);
	}

	public FTP(String username, String password, String hostname, int maxfilesize) {
		user = username;
		pass = password;
		ftpHost = hostname;
		filesizeLimit = maxfilesize;
		uploader = new FTPupload(ftpHost, user, pass);
	}

	public boolean SendNewText(String filename, String txt) throws Exception {
		if (filename == null || txt == null) {
			return false;
		}
		// http://www.kodejava.org/examples/356.html
		//FTPClient client = new FTPClient(); // import org.apache.commons.net.ftp.FTPClient;

		if (txt.length() > filesizeLimit) {
			txt = txt.substring(0, filesizeLimit);
		}

//        while(filename.contains("/")){
//            String d = filename.substring(0, filename.indexOf("/"));
//            filename = filename.substring(filename.indexOf("/") + 1);
//        }

		return uploader.uploadText(txt, filename);
	}
} // end class FTPErrorReporter

// http://www.programmers-corner.com/sourcecode/142
// FTPupload.java by Rowland http://www.home.comcast.net/~rowland3/
// Upload a file via FTP, using the JDK.
class FTPclientConn {

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
			return new URL("ftp://" + user + ":" + 
					password.replace("@", "%40").replace("#", "%23").replace(" ", "%20")
					+ "@" + host + "/" + targetfile + ";type=i");
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

class FTPupload {

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

	public boolean uploadText(String text, String filename) throws Exception {

		OutputStream os = cconn.openUploadStream(filename);
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
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

		return true;
	}

	public void uploadFile(String localfile, String targetfile) throws FileNotFoundException, Exception {
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
	}
}
