
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketClientMultithreaded {
    
    static CyclicBarrier barrier; 
    
    public static void main(String[] args)  {
        String hostName;
        final int MAX_THREADS = 100 ;
        int port;
        
        if (args.length == 2) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            hostName= null;
            port = 12031;  // default port in SocketServer
        }
        barrier = new CyclicBarrier(MAX_THREADS);
        
        // TO DO create and start client threads
        long start=System.currentTimeMillis();
        for(int i=0;i<MAX_THREADS;i++) {
        	SocketClientThread obj = new SocketClientThread(hostName, port, barrier);
        	obj.start();
        }
        try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Socket Client multi thread exiting");
        long end=System.currentTimeMillis();
        System.out.printf("Time %.3f",(end-start)/1000f);
                
    }

      
}
