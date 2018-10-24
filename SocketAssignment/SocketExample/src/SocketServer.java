
/**
 *
 * @author Ian Gortan
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer {
	public static void main(String[] args) throws Exception {
		ServerSocket m_ServerSocket = new ServerSocket(12031);

		int id = 0;
		System.out.println("Server started .....");

		while (true) {
			ActiveCount threadCount = new ActiveCount();
			ExecutorService threadPool = Executors.newFixedThreadPool(30);
			Socket clientSocket = m_ServerSocket.accept();
			threadPool.submit(new SocketHandlerThread(clientSocket, threadCount));
			threadPool.awaitTermination(10, TimeUnit.MILLISECONDS);
			threadPool.shutdown();
		}
	}
}
