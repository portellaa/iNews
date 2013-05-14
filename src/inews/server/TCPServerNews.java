package inews.server;

import inews.interfaces.INewsWindow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that creates a TCP Server News
 *
 * @author Luis Portela
 */
public class TCPServerNews {

	private final ServerSocket serverSocket;
	private Socket clientSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private int port;
	private INewsWindow newsWindow = null;

	/**
	 * Initializes the TCP Server with the provided information
	 *
	 * @param newsWindow instance of the news windows
	 * @param port port to create the TCP server
	 * @throws IOException
	 * @see INewsWindow
	 */
	public TCPServerNews(INewsWindow newsWindow, int port) throws IOException {

		this.port = port;
		this.serverSocket = new ServerSocket(port);
		this.port = this.serverSocket.getLocalPort();
		this.newsWindow = newsWindow;

		this.logMessage("Constructor", "Servidor TCP a escuta na porta: " + this.port);
	}

	/**
	 * Waits for the connection of the client and receives is data
	 *
	 * @return received string
	 */
	public String waitForMessage() {

		boolean running = true;
		String text;
		StringBuilder finalText = new StringBuilder();

		// sets loop while message not received and connection not established
		while (running == true) {
			try {
				this.logMessage("waitForMessage", "A espera de mensagem");
				this.clientSocket = serverSocket.accept();

				this.logMessage("waitForMessage", "IP que se ligou: " + this.clientSocket.getInetAddress().getHostAddress());

				this.out = new PrintWriter(clientSocket.getOutputStream(), true);
				this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				//coneccao estabelecida
				while (true) {
					text = in.readLine();
					if (text.equals("TRANSMISSION_OVER")) {
						out.println("TRANSMISSION_OVER");
						this.clientSocket.close();
						System.out.println("Fim da comunicacao");
						running = false;
						break;
					} else if (text == null) {
						out.println("TRANSMISSION_OVER");
						this.clientSocket.close();
						System.out.println("Fim da comunicacao");
						running = false;
						break;
					} else {
						finalText.append(text);
						finalText.append("\n\n");
					}
				}
				this.logMessage("waitForMessage", "Terminou ciclo vida servidor");
			} catch (IOException ex) {
				this.logMessage("waitForMessage", "A ligacao com o cliente foi fechada!");
			}
		}

		return finalText.toString().replaceAll("\n\n", "\n");
	}

	/**
	 * Returns the port used to the TCP Server
	 *
	 * @return port for TCP Server
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Terminates the connection and closes the socket
	 *
	 * @throws IOException
	 */
	public void endServer() throws IOException {
		try {
			this.serverSocket.close();
			this.finalize();
		} catch (Throwable ex) {
			Logger.getLogger(TCPServerNews.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.newsWindow = null;

	}

	/**
	 * Prints the message to the system output and the news window log area
	 *
	 * @param func function sending the message
	 * @param message message to print
	 * @see INewsWindow#appendTextToLogArea(java.lang.String)
	 */
	private void logMessage(String func, String message) {
		this.newsWindow.appendTextToLogArea("[TCPServerNews]:[" + func + "] -> " + message);
		System.out.println("[TCPServerNews]:[" + func + "] -> " + message);
	}
}