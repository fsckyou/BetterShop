/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: checks for a jar's updates from the github download page
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

import me.jascotty2.lib.io.CheckInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GitJarUpdater extends FileDownloader {

	/**
	 * @param attempt the # of the url being tried (starts at 0)
	 * @return the url of the download page (html) <br/>
	 * ex. "https://github.com/BetterShop/BetterShop/downloads"
	 */
	public abstract String getDownloadPage(int attempt);

	/**
	 * @param attempt the # of the url being tried (starts at 0)
	 * @return the link on the page to look for <br/>
	 * ex. "/downloads/BetterShop/BetterShop/BetterShop.jar"
	 */
	public abstract String getDownloadLink(int attempt);

	/**
	 * optionally, can open this page first before downloading the file <br/>
	 * (normally used for the download counter link)
	 * @param attempt the # of the url being tried (starts at 0)
	 * @return <br/>
	 * ex. "https://github.com/downloads/BetterShop/BetterShop/BetterShop.jar"
	 */
	// for stats purposes, open the normal page that counts the downloads
	public abstract String getDownloadLinkCounter(int attempt);

	/**
	 * @param attempt the # of the url being tried (starts at 0)
	 * @return the actual download link <br />
	 * ex. "http://cloud.github.com/downloads/BetterShop/BetterShop/BetterShop.jar"
	 */
	public abstract String getDownloadLinkUrl(int attempt);
	
	/**
	 * @return the jar file that is to be updated
	 */
	public abstract File getJarFile();

	public boolean isUpToDate(String thisVersion, Date thisFile, long maxDiff, Logger log) {
		int i = 0;
		String dl;
		do {
			dl = getDownloadPage(i);
			if (goodLink(dl) && !isUpToDate(dl, getDownloadLink(i), 
					thisVersion, thisFile, maxDiff, log, i + 1)) {
				return false;
			} else {
				++i;
			}
		} while (dl != null);
		return true;
	}

	public boolean isUpToDate(String location, String linktofind, 
			String thisVersion, Date thisFile, long maxDiff, Logger log, int mirrorNum) {

		InputStream stream = null;
		URLConnection connection;
		try {
			connection = new URL(location).openConnection();
			//connection.setUseCaches(false);
			//long urlLastModified = connection.getLastModified(); // git doesn't report this :(
			int urlFilesize = connection.getContentLength(); // only gets size of header (the dl link is actually a redirect)
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
				int title = file.indexOf("datetime=\"", link);
				if (title > link) { // && file.indexOf("\"", title+8)!=-1){
					if (file.indexOf('"', title) != -1) {
						title = file.indexOf('"', title);
						if (title > link && file.indexOf('"', title + 12) != -1) {
							// now parse in the date uploaded
							//2011-03-18 09:47:56"
							//String dateUploaded = file.substring(title+7, file.indexOf("\"", title+8));
							//>Fri Mar 18 09:47:56 -0700 2011<
							String dateUploaded = file.substring(title + 1, file.indexOf('"', title + 1));
							String uploadComment = "";
							// check if there is a comment (with version number)
							int ver = file.indexOf('\u2014', link);
							if (ver != -1 && ver < title) {
								// continue until newline or <
								int nl = file.indexOf('\n', ver);
								int gt = file.indexOf('<', ver);
								if (nl > gt && gt > ver) {
									nl = gt;
								}
								uploadComment = file.substring(ver + 2, nl);
							}
							
							// date just obtained
							// ex: datetime="2011-05-11T19:00:19-07:00"
							//formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
							DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

							if (dateUploaded.length() > 5
									&& dateUploaded.substring(dateUploaded.length() - 5).contains(":")) {
								dateUploaded = dateUploaded.substring(0, dateUploaded.length() - 5)
										+ dateUploaded.substring(dateUploaded.length() - 5).replace(":", "");
							}

							Date uploadDate = formatter.parse(dateUploaded);

							// now check the two dates & comment
							if ((new Date(thisFile.getTime() + maxDiff)).before(uploadDate)) {// && (uploadDate.getTime()-pluginDate.getTime())/1000 > BetterShop.lastUpdated_gracetime * 60) {

								if (uploadComment.length() > 0) {
									if (versionHigher(uploadComment, thisVersion)) {//uploadComment.trim().equalsIgnoreCase(BetterShop.pdfFile.getVersion())) {
										if (log != null) {
											log.info("Program is (likely) up-to-date (version >= comment)");
										}
										return true;
									} else {

										// double-check against this file
										File jar = getJarFile();
										//System.out.println(jar);
										if (jar != null && jar.exists()) { // double-check got the file
											if (!(new Date(jar.lastModified() + maxDiff)).before(uploadDate)) {
												if (log != null) {
													//System.out.println("file is newer than on git");
													log.info("File is newer than on git (" + mirrorNum + ")");
												}
												return true;
											}
										}

										if (log != null) {
											log.info("Newer Program version found on git: " + uploadComment + " (" + uploadDate + ")");
										}
									}
								} else if (log != null) {
									log.info("Newer Program version found on git (" + uploadDate + ")");
								}

								return false;
								//BetterShopLogger.Log((new Date(pluginDate.getTime() +BetterShop.lastUpdated_gracetime * 60000)) + " before " + uploadDate);
							} else if (log != null) {
								log.info("Program is up-to-date (" + mirrorNum + ")");
								//BetterShopLogger.Log((new Date(pluginDate.getTime() +BetterShop.lastUpdated_gracetime * 60000)) + " after " + uploadDate);
							}
							return true;
						}
					}
				}
			}
			if (log != null) {
				log.info("Update failed to find git download to check against (" + mirrorNum + ")");
			}
		} catch (MalformedURLException ex) {
			if (log != null) {
				log.warning("unexpected invalid url (" + mirrorNum + ")");//, ex);
			}
		} catch (IOException ex) {
			if (log != null) {
				log.warning("failed to check for updates (" + mirrorNum + ")");//, ex);
			}
		} catch (Exception ex) {
			if (log != null) {
				log.log(Level.WARNING, "uxexpected error during check for updates (" + mirrorNum + ")", ex);
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ex) {
					//BetterShopLogger.Log(Level.WARNING, "Failed to close URL InputStream", ex);
				}
			}
		}
		return true;
	}

	public void downloadUpdate() throws MalformedURLException, IOException {
		File jar = getJarFile();
		if (jar.exists()) {
			int i = 0;
			String dl;
			do {
				dl = getDownloadLinkUrl(i++);
			} while (dl != null && !goodLink(dl));
			if (dl != null) {
				String counterLink = getDownloadLinkCounter(i - 1);
				if (counterLink != null && counterLink.length() > 0) {
					try {
						// for stats purposes, open the normal page that counts the downloads
						URLConnection cconn = (new URL(counterLink)).openConnection();
						cconn.getContent();
					} catch (Exception e) {
						// ignore errors here..
					}
				}
				download(dl, jar.getAbsolutePath());
			}
		}
	}

	/**
	 * compare two version strings, like "1.4.2" !> "1.4.3" | "1.4.2b"
	 * @param version first version string
	 * @param compare version string to compare to
	 * @return true if compare is > than first version parameter
	 */
	static boolean versionHigher(String version, String compare) {
		if (version == null || compare == null) {
			return false;
		}
		String v1[] = version.trim().toLowerCase().split("\\."),
				v2[] = compare.trim().toLowerCase().split("\\.");
		int n1, n2;
		for (int i = 0; i < v1.length && i < v2.length; ++i) {
			if (CheckInput.IsInt(v1[i]) && CheckInput.IsInt(v2[i])) {
				n1 = CheckInput.GetInt(v1[i], 0);
				n2 = CheckInput.GetInt(v2[i], 0);
				if (n1 != n2) {
					return n2 > n1;
				}
			} else {
				String a1 = "", a2 = "";
				if (CheckInput.IsInt(v1[i])) {
					n1 = CheckInput.GetInt(v1[i], 0);
				} else if (java.util.regex.Pattern.matches("(\\p{Digit}+)[a-z]", v1[i])) {
					n1 = CheckInput.GetInt(v1[i].substring(0, v1[i].length() - 1), 0);
					a1 = v1[i].substring(v1[i].length() - 1);
				} else {
					n1 = -1;
				}
				if (CheckInput.IsInt(v2[i])) {
					n2 = CheckInput.GetInt(v2[i], 0);
				} else if (java.util.regex.Pattern.matches("(\\p{Digit}+)[a-z]", v2[i])) {
					n2 = CheckInput.GetInt(v2[i].substring(0, v2[i].length() - 1), 0);
					a2 = v2[i].substring(v2[i].length() - 1);
				} else {
					n2 = -1;
				}

				if (n1 != n2) {
					return n2 > n1;
				} else if (a1.length() != a2.length()) {
					return a2.length() > a1.length();
				} else if (a1.length() > 0 && a2.charAt(0) != a1.charAt(0)) {
					return a2.charAt(0) > a1.charAt(0);
				}

			}
		}
		return v2.length > v1.length;
	}
//    static boolean fileVersionOlder(String compare) {
//        return versionHigher(BetterShop.pdfFile.getVersion(), readVersionFile());
//    }
//
//    static String readVersionFile() {
//        File versionFile = new File(BSConfig.pluginFolder, "VERSION");
//        String line = null;
//        if (versionFile.exists() && versionFile.canRead()) {
//            FileReader fstream = null;
//            BufferedReader in = null;
//            try {
//                fstream = new FileReader(versionFile.getAbsolutePath());
//                in = new BufferedReader(fstream);
//
//                line = in.readLine();
//
//            } catch (Exception ex) {
//                BetterShopLogger.Log(Level.SEVERE, "Error reading version save file", ex);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                        fstream.close();
//                    } catch (IOException ex) {
//                    }
//                }
//            }
//        }
//        return line;
//    }
//    public static void main(String[] args) {
//
//        com.jascotty2.ConsoleInput c = new com.jascotty2.ConsoleInput();
//        String in = "";
//        System.out.println("input: version, compare");
//        while ((in = c.GetString("\n> ")).length() > 0) {
//            if (!in.contains(",")) {
//                System.out.println("missing compare version");
//            } else {
//                System.out.println(versionHigher(in.substring(0, in.indexOf(",")),
//                        in.substring(in.indexOf(",") + 1)));
//            }
//        }
//    }
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
//    public static void main(String[] args) {
//        String in = "";
//        com.jascotty2.io.ConsoleInput c = new com.jascotty2.io.ConsoleInput();
//        while ((in = c.GetString("\n> ")).length() > 0) {
//            // ex: datetime="2011-05-11T19:00:19-07:00"
//            //formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
//            ///DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
//            DateFormat formatter = new SimpleDateFormat(in, Locale.US);
//            try {
//                System.out.println(formatter.format(new Date()));
//                String d = c.GetString("> ");
//                if(d.length() > 5 && d.substring(d.length()-5).contains(":")){
//                    d = d.substring(0, d.length()-5) + d.substring(d.length()-5).replace(":", "");
//                    System.out.println(d);
//                }
//                Date uploadDate = (Date) formatter.parse(d);
//                System.out.println(uploadDate);
//            } catch (Exception ex) {
//                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
//
//    }
} // end class Updater

