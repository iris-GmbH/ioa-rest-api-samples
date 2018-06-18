package de.iris.ds.rest.api.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

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
public class RestApiApacheClient {

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
		String operator = "demoOperator";
		String vehicleId = "demoVehicle";
		String opdate = "2018-06-18";
		
		// limit result, 0 all data for selected operation date
		String limit = "0";

		/*
		 * API Method
		 */
		String method = "stops";
		String version = "v1/r6";

		/*
		 * API data format parameter
		 */
		String compression = "gzip,deflate"; // or none

		/*
		 * Accepted mime type values: 
		 * 		- text/csv 
		 * 		- application/json 
		 * 		- application/xml
		 */
		String mimeType = "text/csv";

		CloseableHttpClient httpclient = null;

		try {
			// set credential provider
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(host, 443), new UsernamePasswordCredentials(user, pw));

			// used for self-signed certificates
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
			// custom http client with ssl and credential provider
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credsProvider).build();

			URIBuilder uriBuilder = new URIBuilder(
					"https://" + host + "/services/REST/apc/" + version + "/" + method + "/" + operator + "");

			// set up query parameters
			List<NameValuePair> parameter = new ArrayList<NameValuePair>();
			parameter.add(new BasicNameValuePair("opdate", opdate));
			parameter.add(new BasicNameValuePair("limit", limit));
			parameter.add(new BasicNameValuePair("vehicleId", vehicleId));
			uriBuilder.addParameters(parameter);

			// set up request
			HttpPost postRequest = new HttpPost(uriBuilder.build());
			postRequest.setHeader("Accept-Encoding", compression);
			postRequest.setHeader("Accept", mimeType);

			// execute REST call
			System.out.println(postRequest.toString());
			CloseableHttpResponse response = httpclient.execute(postRequest);
			HttpEntity entity = response.getEntity();

			// get http response code
			int stCode = response.getStatusLine().getStatusCode();

			// get response data as string
			String content = EntityUtils.toString(entity);
			if (stCode == 200) {
				// HTTP body entity result
				if (entity != null) {
					System.out.println(content);
				}
			} else {
				System.out.println("Error Http Code: " + stCode);
				System.out.println("Error Entity: " + content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpclient != null)
				try {
					httpclient.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
		}
	}
}