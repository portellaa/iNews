package inews.server;

import inews.dataStructures.*;
import inews.interfaces.INewsWindow;
import inews.interfaces.NewsWindowClient;
import inews.interfaces.UDPConnection;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that listen the network and sends message
 *
 * @author Luis Portela
 */
public class TransmissionHandler extends Thread implements UDPConnection {

	private static TransmissionHandler mySelf = null;
	private boolean running = true;
	private DatagramSocket socket = null;
	private INewsWindow newsWindow = null;
	private NewsWindowClient clientNews = null;
	private UserStruct me = null;
	private NavigableMap<Integer, AckMessage> waitingList = null;

	/**
	 * Initializes the class with the provided information
	 *
	 * @param myID user struct of the local user
	 * @param newsWindow pointer to the news window functions
	 * @param clientNews pointer to the news window client
	 * @see UserStruct
	 * @see INewsWindow
	 * @see NewsWindowClient
	 */
	private TransmissionHandler(UserStruct myID, INewsWindow newsWindow, NewsWindowClient clientNews) {
		try {
			this.socket = new DatagramSocket(myID.getIp().getPort());
			this.me = myID;
			this.newsWindow = (INewsWindow) newsWindow;
			this.clientNews = (NewsWindowClient) clientNews;
			this.waitingList = new TreeMap<Integer, AckMessage>();

		} catch (SocketException ex) {
			Logger.getLogger(TransmissionHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Returns the unique instance of this class and if it not exists create it and returns
	 *
	 * @param myID user struct of the local user
	 * @param newsWindow pointer to the news window functions
	 * @param clientNews pointer to the news window client
	 * @return TransmissionHandler existing instance of this class
	 * @see UserStruct
	 * @see INewsWindow
	 * @see NewsWindowClient
	 */
	public static TransmissionHandler getInstance(UserStruct myID, INewsWindow newsWindow, NewsWindowClient clientNews) {
		if (mySelf == null) {
			mySelf = new TransmissionHandler(myID, (INewsWindow) newsWindow, (NewsWindowClient) clientNews);
		}

		return mySelf;
	}

	/**
	 * List the network for messages and create a new thread to process it If is a BYE message from the local user it exits
	 *
	 * @see HandleReceivedMessages
	 */
	@Override
	public void run() {
		try {
			try {

				System.out.println("[UDPServerNews]:[run()] -> Iniciei Server");

				while (running) {

					byte buf[] = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

					System.out.println("[UDPServerNews]:[run()] -> Em espera de pacotes");

					// A espera da rede
					this.socket.receive(receivePacket);
					System.out.println("[UDPServerNews]:[run()] ->  -> Recebido pacote de: " + receivePacket.getAddress().getHostAddress());
					if ((!receivePacket.getAddress().getHostAddress().equalsIgnoreCase(this.me.getIp().getIp()))
							&& (!receivePacket.getAddress().getHostAddress().equalsIgnoreCase("127.0.0.1")) && this.isRunning()) {
						HandleReceivedMessages handleNews = new HandleReceivedMessages(this.me, this, receivePacket, (INewsWindow) this.newsWindow,
								(NewsWindowClient) this.clientNews);
						handleNews.start();
					} else {
						Packet tmp = new Packet(receivePacket.getData(), receivePacket.getLength(), new IPStruct(receivePacket.getAddress().getHostAddress(), receivePacket.getPort()));
						if (tmp.getType().equals(MessageType.BYE)) {
							this.setRunning(false);
						}
					}

					// Permite que a Thread que vai tratar da mensagem recebida ganhe processador
					Thread.sleep(10);
				}
			} catch (IOException ex) {
				Logger.getLogger(TransmissionHandler.class.getName()).log(Level.SEVERE, null, ex);
			}
			System.out.println("[UDPServerNews] -> Sai Server");
			//this.serverSocket.close();
			this.finalize();
		} catch (Throwable ex) {
			Logger.getLogger(TransmissionHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Sends packets to the network
	 *
	 * @param sendPacket packet to send
	 * @see Packet
	 */
	@Override
	public void send(Packet sendPacket) {

		try {
			socket.send(sendPacket.buildDatagramPacket());


		} catch (IOException ex) {
			Logger.getLogger(TransmissionHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Add to the acknowledge message list the received message and creates a thread to process it
	 *
	 * @param sendPacket message to be sent and wait for acknowledge or to be retransmited
	 * @param retries number of times that the message should be send if not ack is received
	 * @param timeout time between retries
	 * @see Packet
	 */
	@Override
	public synchronized void sendWithAck(Packet sendPacket, int retries, int timeout) {

		System.out.println("[UDPServerNews]:[sendWithAck] -> Numero de tentativas: " + retries + " timeout: " + timeout);

		AckMessage ackMsg = new AckMessage(sendPacket.getSequence(), retries, timeout, sendPacket);
		this.waitingList.put(sendPacket.getSequence(), ackMsg);
		AckHandler ackMsgHandle = new AckHandler(ackMsg);
		ackMsgHandle.start();
	}

	/**
	 * Read the message waiting for ack and process it
	 *
	 * @param ackMessage message to be processed
	 * @see AckMessage
	 */
	private void readAckPacket(AckMessage ackMessage) {

		switch (ackMessage.getSendPacket().getType()) {

			case SENSE: {

				if (!ackMessage.isAckReceived()) {
					this.newsWindow.successfulLogin();
				} else {
					this.setRunning(false);
					this.newsWindow.failedLogin();
				}
				this.removeAck(ackMessage.getSequenceNumber());
				break;
			}

			case PING: {

				if (!ackMessage.isAckReceived()) {
					this.newsWindow.removeFromListByIP(ackMessage.getSendPacket().getPacketIP());
					this.newsWindow.refreshUsersList();
				}

				this.removeAck(ackMessage.getSequenceNumber());

				break;
			}

			case NEWS: {
				if (!ackMessage.isAckReceived()) {
					this.newsWindow.appendCommandResultToOutputArea("news", "Nao foi obtida resposta!");
				}

				this.removeAck(ackMessage.getSequenceNumber());

				break;
			}

			case RANK: {
				if (!ackMessage.isAckReceived()) {
					this.clientNews.processRankForUser(ackMessage.getSendPacket().getDataList().getFirst().getValue(), 0, false);
				}
				
				this.removeAck(ackMessage.getSequenceNumber());
			}
		}
	}

	/**
	 * Check if the program is still running
	 *
	 * @return true if is alive; false otherwise
	 */
	@Override
	public boolean isRunning() {
		return running;
	}

	/**
	 * Allows to change the running status
	 *
	 * @param running new thread status
	 */
	@Override
	public void setRunning(boolean running) {
		this.running = running;
	}

	// Metodos para operar a lista de mensagens com acknowledge
	/**
	 * Returns the message in the list with the provided sequence number
	 *
	 * @param sequence sequence number to search
	 * @return message if it exists
	 * @see AckMessage
	 */
	@Override
	synchronized public AckMessage getAck(int sequence) {
		return this.waitingList.get(sequence);
	}

	/**
	 * Sets the message on the messages list with the provided sequence number as received
	 *
	 * @param sequence sequence number of the message to mark as received
	 */
	@Override
	synchronized public void receivedAck(int sequence) {

		AckMessage tmpAckMessage;

		if ((tmpAckMessage = this.getAck(sequence)) != null) {
			tmpAckMessage.ackReceived();
		}
	}

	/**
	 * Remove a mensagem da lista de mensagens com acknowledge com o sequence number
	 *
	 * @param sequence sequence number da mensagem a retirar da lista
	 */
	@Override
	synchronized public void removeAck(int sequence) {
		this.waitingList.remove(sequence);


	}

	// Thread responsavel por tratar das mensagens de acknowledge
	/**
	 * Class to process the message it acknowledge
	 *
	 * @see AckMessage
	 * @see Thread
	 */
	private class AckHandler extends Thread {

		/**
		 * Mensagem a ser tratada
		 */
		AckMessage ackInfo;

		/**
		 * Initializes the thread to send the packets with acknowledge and retransmission
		 *
		 * @param ackInfo message with acknowledge and/or needs to be retransmited
		 */
		public AckHandler(AckMessage ackInfo) {
			this.ackInfo = ackInfo;
		}

		@Override
		public void run() {
			// Envia a mensagem caso esta ainda não tenha sido recebida ou o numero de tentativas ainda não tenha chegado ao limite
			while ((!ackInfo.isAckReceived()) && (ackInfo.getRetries() != 0) && isRunning()) {
				try {
					// Chama o metodo que envia a mensagem
					send(ackInfo.getSendPacket());
					// Adormece a thread pelo tempo especificado na mensagem
					Thread.sleep(ackInfo.getTimeout());
				} catch (InterruptedException ex) {
					Logger.getLogger(TransmissionHandler.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			readAckPacket(ackInfo);
		}
	}
}