package de.iris.ds.rest.api.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

/**
 * ___   ___   __  __     _                      _     _       
 *|_ _| | _ \ |  \/  |   /_\      ___   _ _     /_\   (_)  _ _ 
 * | |  |   / | |\/| |  / _ \    / _ \ | ' \   / _ \  | | | '_|
 *|___| |_|_\ |_|  |_| /_/ \_\   \___/ |_||_| /_/ \_\ |_| |_|  
 * 
 * IRMA onAir REST-API sample client using Apache HTTP Client Framework.
 * {@link https://hc.apache.org/}
 * 
 * This example prints automatic passenger counting data as csv to the console. 
 * 
 * You will need to replace at least the values for credentials, operator ID and 
 * vehicle ID according to the definition of your project. Please contact your iris 
 * project manager if those values are unknown to you or contact irmaonair@irisgmbh.de. 
 * 
 * Copyright (c) 2018 by iris-GmbH, Berlin
 * 
 * All rights reserved.
 */
public class RestApiNativeClient {

	public static void main(String[] args) {

		/*
		 * API access data
		 */
		String user = "demoUser";
		String pw = "demoPassword";
		String host = "api.irmaonair.com";

		/*
		 * API parameter data
		 */
		String deviceLabel = "demoVehicle";
		String operator = "demoOperator";
		String opdate = "2018-06-18";

		/*
		 * API data format parameter
		 */
		String compression = "gzip,deflate"; // or none

		/*
		 * Accepted mime type values: - text/csv - application/json - application/xml
		 */
		String mimeType = "text/csv";

		/*
		 * API Method
		 */
		String method = "stops";
		String version = "v1/r6";

		// set up credentials
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pw.toCharArray());
			}
		});

		try {
			// set up URL
			deviceLabel = URLEncoder.encode(deviceLabel, "UTF-8");
			URL url = new URL("https://" + host + "/services/REST/apc/" + version + "/" + method + "/" + operator
					+ "/?vehicleId=" + deviceLabel + "&opdate=" + opdate + "");

			// Prepare HTTP connection
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept-Encoding", compression);
			conn.setRequestProperty("Accept", mimeType);
			BufferedInputStream reader = null;

			// read response, handle error if necessary
			if (conn.getResponseCode() != 200) {
				System.out.println("Failed : HTTP error code : " + conn.getResponseCode());

				reader = new BufferedInputStream(conn.getErrorStream());
				while (true) {
					int ch = reader.read();
					if (ch == -1) {
						break;
					}
					// error stream from server includes error code
					System.out.print((char) ch);
				}

				System.exit(-1);
			}

			// read response
			if ("gzip".equals(conn.getContentEncoding())) {
				reader = new BufferedInputStream(new GZIPInputStream(conn.getInputStream()));
			} else {
				reader = new BufferedInputStream(conn.getInputStream());
			}

			String filename = opdate + "_" + deviceLabel + ".json";
			try {
				// if contains filename in header
				String tmp = conn.getHeaderField("Content-Disposition").split("filename=")[1];
				filename = tmp.substring(1, tmp.length() - 1);
				System.out.println("Filename generated from API: " + filename);
			} catch (Exception e) {
				System.out.println("No filename in header");
			}

			// write APC data to file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));

			System.out.println("Write output from server to " + filename);
			while (true) {
				int ch = reader.read();
				if (ch == -1) {
					break;
				}
				out.write(ch);
			}
			out.flush();
			out.close();
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}