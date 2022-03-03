package me.duncanruns.fsg.cryptography;

import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class DRandRequester {
    private final HttpClient httpClient;

    public DRandRequester() {
        httpClient = HttpClients.createDefault();
    }

    public DRandInfo get(String round) throws IOException {
        HttpGet request = new HttpGet("https://drand.cloudflare.com/public/" + round);
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request);
        String out = EntityUtils.toString(response.getEntity());
        response.close();
        return new Gson().fromJson(out, DRandInfo.class);
    }
}
