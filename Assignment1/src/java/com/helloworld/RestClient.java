/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.helloworld;
import java.util.Arrays;
import java.util.Random;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author survi
 */
public class RestClient {

    static int requestCount = 0;
    static int responseCount = 0;
    static int counter = 0;
        
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
        String ip = args[2];
        String port = args[3];
        long startTime = 0, endTime = 0;
        int sizeLatencyArray =  (int)(threadCount*0.1)+(int)(threadCount*0.5)+
                threadCount+(int)(threadCount*0.25);
        double latency[] = new double[sizeLatencyArray*iterationCount*2];
        //int counter = 0;
        class StopLatchedThread implements Runnable {
            
            private CountDownLatch stopLatch;
            public StopLatchedThread(CountDownLatch stopLatch) {
                this.stopLatch = stopLatch;
                
            }
            public void run() {
                for (int i = 0; i < iterationCount; i++) {
                    long requestSent = 0,responseReceied = 0;
                    requestSent = System.currentTimeMillis();
                    String getResponse = callGet(ip,port);
                    responseReceied = System.currentTimeMillis();
                    if (getResponse.equals("Hello")) {
                        incrementResponseCount();
                        latency[counter]=(responseReceied - requestSent)/1000f; 
                        incrementCounter();
                    }
                    

                    
                    String defaultStr = "abcdefghijklmnopqrstuvwxyz";
                    String input = defaultStr.substring(new Random().nextInt(26)+1);
                    requestSent = System.currentTimeMillis();
                    int postResponse = callPost(input,ip,port);
                    responseReceied = System.currentTimeMillis();
                    if (postResponse==input.length()) {
                        incrementResponseCount();
                        latency[counter]=(responseReceied - requestSent)/1000f; 
                        incrementCounter();
                    }
                }
                
                stopLatch.countDown();
            }
        }
        CountDownLatch latch1 = new CountDownLatch((int)(threadCount*0.1));
        CountDownLatch latch2 = new CountDownLatch((int)(threadCount*0.5));
        CountDownLatch latch3 = new CountDownLatch(threadCount);
        CountDownLatch latch4 = new CountDownLatch((int)(threadCount*0.25));
        System.out.println("client: "+threadCount+" "+iterationCount+" "
                +ip+" "+port);
        long clientStart = System.currentTimeMillis();
        System.out.println("Client start time: "+clientStart
                +" millis");
        startTime = System.currentTimeMillis();
        ExecutorService executor1 = Executors.newFixedThreadPool((int)(threadCount*0.1));
        for (int i = 0; i < (int)(threadCount*0.1); i++) {
            executor1.submit(new StopLatchedThread(latch1));
        }
        System.out.println("Warmup phase:All threads running...");
        latch1.await();
        executor1.shutdown();
        endTime = System.currentTimeMillis();
        System.out.printf("Warmup phase complete time: %.3f seconds\n",
                ((endTime - startTime)/1000f));
        startTime = System.currentTimeMillis();
        ExecutorService executor2 = Executors.newFixedThreadPool((int)(threadCount*0.5));
        for (int i = 0; i < (int)(threadCount*0.5); i++) {
            executor2.submit(new StopLatchedThread(latch2));
        }
        System.out.println("Load phase:All threads running...");
        latch2.await();
        executor2.shutdown();
        endTime = System.currentTimeMillis();
        System.out.printf("Load phase complete time: %.3f seconds\n",
                ((endTime - startTime)/1000f));
        
        startTime = System.currentTimeMillis();
        ExecutorService executor3 = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor3.submit(new StopLatchedThread(latch3));
        }
        System.out.println("Peak phase:All threads running...");
        latch3.await();
        executor3.shutdown();
        endTime = System.currentTimeMillis();
        System.out.printf("Peak phase complete time: %.3f seconds\n",
                ((endTime - startTime)/1000f));
        startTime = System.currentTimeMillis();
        ExecutorService executor4 = Executors.newFixedThreadPool((int)(threadCount*0.25));
        for (int i = 0; i < (int)(threadCount*0.25); i++) {
            executor4.submit(new StopLatchedThread(latch4));
        }
        System.out.println("Cooldown phase:All threads running...");
        latch4.await();
        executor4.shutdown();
        endTime = System.currentTimeMillis();
        System.out.printf("Cooldown phase complete time: %.3f seconds\n",
                ((endTime - startTime)/1000f));
        System.out.println("Client end time: "+endTime+" milliseconds");
        
        System.out.println("=========================================");
        System.out.println("Total requests sent: " + requestCount);
        System.out.println("Total response received: " + responseCount);
        double wall=(endTime - clientStart)/ 1000f;
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
                (double)latency[requestCount / 2]+" seconds");
        else
            System.out.println("Median of all latencies: "+
                    (double)((latency[(requestCount - 1) / 2] + 
                            latency[requestCount / 2]) / 2.0)+" seconds");
        System.out.println("latency of 95th Percentile: "+ 
                latency[(int)(latency.length * 0.95)]+" seconds");
        System.out.println("latency of 99th Percentile: "+ 
                latency[(int)(latency.length * 0.99)]+" seconds");

    }

    public static String callGet(String ip, String port) {
        incrementRequestCount();
        //Create client instance
        Client client = ClientBuilder.newClient();
        //send GET request using the URI
        String target = new String("http://" + ip + ":" + port + "/HelloWorldApplication/webresources/helloworld/sayHello");
        return client.target(target).
                request(javax.ws.rs.core.MediaType.TEXT_PLAIN).get(String.class);
    }

    public static int callPost(String input, String ip, String port) {
        incrementRequestCount();
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target("http://" + ip + ":" + port + "/HelloWorldApplication/webresources/helloworld");
        Response response = target.request(MediaType.TEXT_PLAIN).
                put(Entity.entity(input, MediaType.TEXT_PLAIN), Response.class);
        int result = response.readEntity(Integer.class);
        return result;

    }

}
