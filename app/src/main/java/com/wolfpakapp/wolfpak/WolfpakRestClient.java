package com.wolfpakapp.wolfpak;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * The WolfpakRestClient can be used to make GET and POST requests to the Wolfpak server.
 * All methods are static, meaning the client does not need to be initialized.
 */
public class WolfpakRestClient {
    private static final String BASE_URL = "http://ec2-52-4-176-1.compute-1.amazonaws.com/";

    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);

    /**
     * Perform a HTTP GET request on the Wolfpak server with parameters.
     * @param url the relative URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @param handler the response handler instance that should handle the response.
     */
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        client.get(getAbsoluteUrl(url), params, handler);
    }

    /**
     * Perform a HTTP POST request on the Wolfpak server with parameters.
     * @param url the relative URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @param handler the response handler instance that should handle the response.
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        client.post(getAbsoluteUrl(url), params, handler);
    }

    /**
     * Perform a HTTP PUT request on the Wolfpak server with parameters.
     * @param url the relative URL to send the request to.
     * @param params additional PUT parameters or files to send with the request.
     * @param handler the response handler instance that should handle the response.
     */
    public static void put(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        client.put(getAbsoluteUrl(url), params, handler);
    }

    /**
     * Return the absolute URL made by appending the relative URL to the base URL of the server.
     * @param relativeUrl the relative URL.
     * @return the absolute URL
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
