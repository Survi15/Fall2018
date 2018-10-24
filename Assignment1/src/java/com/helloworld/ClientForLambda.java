package com.helloworld;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.StringReader;
import java.util.Arrays;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.JsonReader;


/**
 *
 * @author survi
 */
public class ClientForLambda {

    static int requestCount = 0;
    static int responseCount = 0;
    static int counter = 0;
    static final String target = "https://9l5m8tz969.execute-api.us-west-2.amazonaws.com";
    static final String path = "/Prod/sayHello";
        
    public static synchronized void incrementRequestCount() {
        requestCount++;
    }
    
    public static synchronized void incrementResponseCount() {
        responseCount++;
    }
    
    public static synchronized void incrementCounter() {
        counter++;
    }

    public static void main(String args[]) throws InterruptedException {
        int threadCount = new Integer(args[0]);
        int iterationCount = new Integer(args[1]);
        int sizeLatencyArray =  (int)(threadCount*0.1)+(int)(threadCount*0.5)+
                threadCount+(int)(threadCount*0.25);
        double latency[] = new double[sizeLatencyArray*iterationCount*2];
        
        Client client = ClientBuilder.newClient();
        final WebTarget webTarget = client.target(target).path(path);
        double[] phaseFactors = new double[] {0.1, 0.5, 1.0, 0.25};
        String[] phaseTypes = new String[] {"Warmup", "Load", "Peak", "Cooldown"};
        // client start time set
        System.out.println("Client: Thread Count: "+threadCount+", Iteration Count: "+iterationCount);
        long clientStart = System.currentTimeMillis();
        System.out.println("Client starting time: "+clientStart+" milliseconds");
        for(int i=0;i<phaseFactors.length;i++) {
        	createPhase(threadCount, phaseTypes[i], phaseFactors[i], webTarget, iterationCount, latency);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Client end time: "+endTime+" milliseconds");
        System.out.println("=========================================");
        System.out.println("Total requests sent: " + requestCount);
        System.out.println("Total response received: " + responseCount);
        
        double wall = (endTime - clientStart)/ 1000f;
        System.out.printf("Total wall time: %.3f seconds\n",wall);
        System.out.println("Total number of successful requests: "+counter);
        
        double sum=0;
        for(int i=0;i<counter;i++){
             sum += latency[i];
        }
        
        double throughput  = requestCount/wall;
        System.out.println("Overall throughput across all phases: "+
                throughput+" seconds");
        System.out.println("Mean of all latencies: " + (sum/requestCount)+" seconds");
        // First we sort the array
        Arrays.sort(latency);
        // check for even case
        if (counter % 2 != 0)
            System.out.println("Median of all latencies: "+
                (double)latency[(requestCount / 2)]+" seconds");
        else
            System.out.println("Median of all latencies: "+
                    (double)((latency[ ((requestCount - 1) / 2)] + 
                            latency[ (requestCount / 2)]) / 2.0)+" seconds");
        System.out.println("latency of 95th Percentile: "+ 
                latency[(int)(latency.length * 0.95)]+" seconds");
        System.out.println("latency of 99th Percentile: "+ 
                latency[(int)(latency.length * 0.99)]+" seconds");
        client.close();
        
    }
    
    public static void createPhase(int threadCount, String phaseType, double phaseFactor, 
    		WebTarget webTarget, int iterationCount, double[] latency) throws InterruptedException {
    	long startTime = 0, endTime = 0;
    	int totalThreads = (int) (threadCount*phaseFactor);
    	CountDownLatch latch = new CountDownLatch(totalThreads);
    	startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        for (int i = 0; i < totalThreads; i++) {
            executor.submit(new StopLatchedThread(latch, webTarget, iterationCount, latency));
        }
        System.out.println(phaseType + " phase:All threads running...");
        latch.await();
        executor.shutdown();
//        if(executor.awaitTermination(10, TimeUnit.MILLISECONDS)) {
//        	executor.shutdownNow();
//        }
        endTime = System.currentTimeMillis();
        System.out.printf(phaseType + " phase complete time: %.3f seconds\n",
                ((endTime - startTime)/1000f));
    }

    public static JsonObject callGet(WebTarget target) {
        incrementRequestCount();
        Response response = target.request(MediaType.TEXT_PLAIN).get();
        StringReader stringReader = new StringReader(response.readEntity(String.class));
        JsonReader jsonReader = Json.createReader(stringReader);
        response.close();
        return jsonReader.readObject();
    }

    public static JsonObject callPost(JsonObject input, WebTarget target) {
        incrementRequestCount();
        Response response = target.request(MediaType.TEXT_PLAIN).post(Entity.json(input));
        StringReader stringReader = new StringReader(response.readEntity(String.class));
        JsonReader jsonReader = Json.createReader(stringReader);
        response.close();
        return jsonReader.readObject();
    }
    
    static class StopLatchedThread implements Runnable {
        
        private CountDownLatch stopLatch;
        private WebTarget webTarget;
        private int iterationCount;
        private double[] latency;
        
        public StopLatchedThread(CountDownLatch stopLatch, WebTarget webTarget, int iterationCount, double[] latency) {
			super();
			this.stopLatch = stopLatch;
			this.webTarget = webTarget;
			this.iterationCount = iterationCount;
			this.latency = latency;
		}
        
        public void run() {
            for (int i = 0; i < iterationCount; i++) {
                long requestSent = 0,responseReceived = 0;
                requestSent = System.currentTimeMillis();
                String getResponse = callGet(webTarget).getString("response");
                responseReceived = System.currentTimeMillis();
                if (getResponse.equals("Hello")) {
                    incrementResponseCount();
                    latency[counter]=(responseReceived - requestSent)/1000f; 
                    incrementCounter();
                }   
                requestSent = System.currentTimeMillis();
                String defaultStr = "a";
                JsonObject inputJSON = Json.createObjectBuilder()
                        .add("input", defaultStr).build();
                int postResponse = Integer.valueOf(callPost(inputJSON,webTarget).getString("response"));
                responseReceived = System.currentTimeMillis();
                if (postResponse==defaultStr.length()) {
                    incrementResponseCount();
                    latency[counter]=(responseReceived - requestSent)/1000f; 
                    incrementCounter();
                }       
            }
            stopLatch.countDown();    
        }
    }

}
