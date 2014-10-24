package com.brandwatch.twitter.hashtags;


import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusListener;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;

public class Main {
    private Twitter4jStatusClient statusClient;
    private ExecutorService service;
    private Client hosebirdClient;
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String secret;
    private Multiset<String> hashtags;

    public static void main(String[] args) {
        Main main = new Main();
        main.process();
    }

    private void process() {
        loadProperties();
        checkNotNull(consumerKey);
        checkNotNull(consumerSecret);
        checkNotNull(token);
        checkNotNull(secret);
        hashtags = HashMultiset.create();
        setup(consumerKey, consumerSecret, token, secret);
        delay(5);
        for (Multiset.Entry<String> hashtag : Multisets.copyHighestCountFirst(hashtags).entrySet()) {
            System.out.println(hashtag.getElement() + " " + hashtag.getCount());
        }
        shutdown();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        InputStream stream;
        try {
            stream = ClassLoader.getSystemResourceAsStream("config.properties");
            properties.load(stream);
            stream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        consumerKey = properties.getProperty("consumerKey");
        consumerSecret = properties.getProperty("consumerSecret");
        token = properties.getProperty("token");
        secret = properties.getProperty("secret");
    }

    private void setup(String consumerKey, String consumerSecret, String token, String secret) {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);
        ClientBuilder builder = new ClientBuilder()
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(new StatusesSampleEndpoint())
                .processor(new StringDelimitedProcessor(msgQueue));
        StatusListener listener = new StatusAdapter() {
            @Override
            public void onStatus(Status status) {
                HashtagEntity[] entities = status.getHashtagEntities();
                for (HashtagEntity entity : entities) {
                    hashtags.add(entity.getText());
                }
            }
        };
        List<StatusListener> listeners = ImmutableList.of(listener);
        hosebirdClient = builder.build();
        service = Executors.newFixedThreadPool(1);
        statusClient = new Twitter4jStatusClient(hosebirdClient, msgQueue, listeners, service);
        statusClient.connect();
        statusClient.process();
    }

    private void delay(int minutes) {
        for (int x = 0; x < 1; x++) {
            try {
                Thread.sleep(1000 * 60 * minutes);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void shutdown() {
        statusClient.stop(2000);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdownNow();
        hosebirdClient.stop();
    }
}
