package inews.client;

import inews.dataStructures.IPStruct;
import inews.interfaces.INewsWindow;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class with functinons to connect to the TCP Server and send the information
 * When its created it reads the asked files and send it to the connected server
 * @author Luis Portela
 */
public class TCPClientNews {

	private final Socket clientSocket;
	private final IPStruct destServer;
	private final INewsWindow newsWindow;
	private PrintWriter out = null;
	private BufferedReader in = null;

	/**
	 * Initializes the TCP Client with the provided information
	 * @param destServer destination server
	 * @param newsWindow instance to the news window
	 * @see IPStruct
	 * @see INewsWindow
	 * @throws IOException 
	 */
	public TCPClientNews(IPStruct destServer, INewsWindow newsWindow) throws IOException {

		this.newsWindow = newsWindow;
		this.destServer = destServer;

		// ligar ao servidor
		this.clientSocket = new Socket(this.destServer.getIp(), this.destServer.getPort());
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	/**
	 * Send the files to the connected server
	 * @param listOfFiles list of files on the news directory
	 * @param numOfFiles num of last files to read
	 * @see File
	 */
	public void sendFilesInfo(File[] listOfFiles, int numOfFiles) {
		
		boolean running = true;
		int indexToRead = numOfFiles == 0 ? 0 : (listOfFiles.length - numOfFiles < 0 ? 0 : listOfFiles.length - numOfFiles);

		String readedFiles = this.readFiles(listOfFiles, indexToRead);
		
		out.println(readedFiles.split("\n").length);
		
//		out.println("TRANSMISSION_STARTED");
		while (running == true) {
			try {
				out.println(readedFiles);
				this.logMessage("sendFilesInfo", "Enviada string para servidor");
				out.println("TRANSMISSION_OVER");
				String txt = in.readLine();
				while (!txt.equalsIgnoreCase("TRANSMISSION_OVER")) {
					txt = in.readLine();
				}
				if (txt.equals("TRANSMISSION_OVER"))
					running = false;
			} catch (IOException ex) {
				this.logMessage("sendFilesInfo", "A ligacao com o servidor foi terminada!");
			}
		}
		this.endClient();
	}

	/**
	 * Ends the client connection
	 * Closes the socket and finalize this class
	 */
	public void endClient() {
		try {
			this.out.close();
			this.in.close();
			clientSocket.close();
			this.logMessage("endClient", "Comunicacao com o servidor terminada!");
			this.finalize();
		} catch (IOException ex) {
			Logger.getLogger(TCPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Throwable ex) {
			Logger.getLogger(TCPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Reads the files information
	 * @param listOfFiles list of files on the news directory
	 * @param startIndex index to start read
	 * @return String with the content of the files without titles separated by \0
	 */
	private String readFiles(File[] listOfFiles, int startIndex) {

		BufferedReader fileReader;
		String line = null;
		StringBuilder finalString = new StringBuilder();
		
		if (listOfFiles.length > 0) {
			for (; startIndex < listOfFiles.length; startIndex++) {
				try {
					fileReader = new BufferedReader(new FileReader(listOfFiles[startIndex]));
					try {
						while ((line = fileReader.readLine()) != null) {
							finalString.append(line + "\n");
						}
					} finally {
						fileReader.close();
					}
					if (startIndex < (listOfFiles.length - 1)) {
						finalString.append('\0');
					}

				} catch (IOException ex) {
					this.logMessage("readTitlesFile", "Nao foi possivel abrir o ficheiro para leitura");

					Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		
		return finalString.toString();
	}

	/**
	 * Prints the message to the system out and the log area of news window
	 * @param func function sending the message
	 * @param message message to print
	 */
	private void logMessage(String func, String message) {
		this.newsWindow.appendTextToLogArea("[TCPClientNews]:[" + func + "] -> " + message);
		System.out.println("[TCPClientNews]:[" + func + "] -> " + message);
	}
}