package inews.server;

import inews.client.TCPClientNews;
import inews.dataStructures.*;
import inews.interfaces.INewsWindow;
import inews.interfaces.NewsWindowClient;
import inews.interfaces.UDPConnection;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Class to process the received message
 * @see Thread
 * @author Luis Portela
 */
public class HandleReceivedMessages extends Thread {

	private Packet packet;
	private INewsWindow newsWindow;
	private NewsWindowClient newsClient;
	private UDPConnection transmiter;
	private UserStruct me;

	/**
	 * Initializes the Thread with the provided values
	 * @param myID local user
	 * @param transmiter connection handler
	 * @param dp datagram packet
	 * @param newsWindow front-end window
	 * @param newsClient client to send
	 */
	public HandleReceivedMessages(UserStruct myID, UDPConnection transmiter, DatagramPacket dp, INewsWindow newsWindow, NewsWindowClient newsClient) {
		this.me = myID;
		this.transmiter = transmiter;
		this.packet = new Packet(dp.getData(), dp.getLength(), new IPStruct(dp.getAddress().getHostAddress(), dp.getPort()));
		this.newsWindow = (INewsWindow) newsWindow;
		this.newsClient = (NewsWindowClient) newsClient;
	}

	/**
	 * Starts the application and process the received message
	 */
	@Override
	public void run() {

		if (this.transmiter.isRunning()) {

			UserStruct updateUser;

			System.out.println("[HandleReceivedMessage]:[" + this.packet.getType().toString() + "] : Recebido:\n");
			System.out.println("[HandleReceivedMessage]:[" + this.packet.getType().toString() + "] : USER: " + this.packet.getUser());
			System.out.println("[HandleReceivedMessage]:[" + this.packet.getType().toString() + "] : IP: " + this.packet.getPacketIP());
			System.out.println("[HandleReceivedMessage]:[" + this.packet.getType().toString() + "] : SEQUENCE: " + this.packet.getSequence());

			switch (this.packet.getType()) {

				// caso a mensagem recebida seja um SENSE
				// se o utilizador for igual ao utilizador actual envia a mensagem de DUPLICATE
				case SENSE: {

					this.logMessage("senseCASE", "Recebida mensagem SENSE");
					if (this.packet.getUser().getName().equalsIgnoreCase(this.me.getName().toString())) {
						this.logMessage("sensePacket", "Utilizador igual a mim!");
						this.newsClient.duplicatePacket(this.packet.getSequence(), this.packet.getPacketIP());
					}

					break;
				}

				// se a mensagem for um DUPLICATE marca o ack do respectivo SENSE como recebido
				// termina a aplicacao
				case DUPLICATE: {

					this.transmiter.receivedAck(this.packet.getSequence());
					this.transmiter.setRunning(false);
					this.logMessage("senseCASE", "Recebida mensagem DUPLICATE");

					break;

				}

				// se a mensagem for ACTIVE marca o ack correspondente como recebido e processa a informacao
				case ACTIVE: {

					AckMessage tmpAckMessage = this.transmiter.getAck(this.packet.getSequence());

					if (tmpAckMessage != null) {
						if (tmpAckMessage.getSendPacket().getType().equals(MessageType.PING)) {
							this.transmiter.receivedAck(this.packet.getSequence());
						}
					}

					this.logMessage("activeCASE", "Recebida mensagem ACTIVE");
					UserStruct commUser = this.newsWindow.addToList(this.packet.getUser().getName().toString(), new UserStruct(this.packet.getUser().getName(), this.packet.getPacketIP()));

					LinkedList<MessageData> dataList = this.packet.getDataList();
					MessageData receivedInfo;

					while ((receivedInfo = dataList.pollFirst()) != null) {

						switch (receivedInfo.getKey()) {
							case NUMBER: {
								commUser.setTotalNews(Integer.valueOf(receivedInfo.getValue()));
								this.logMessage("activeCASE", "Actualizado total noticias utilizador: " + commUser.toString());
								break;
							}
							case DATE: {
								commUser.setLastNewDate(Long.valueOf(receivedInfo.getValue()));
								this.logMessage("activeCASE", "Actualizada ultima noticia utilizador: " + commUser.toString());
								break;
							}
						}
					}

					this.newsWindow.refreshUsersList();

					break;
				}

				// caso seja um PING envia mensagem ACTIVE para o endereço unicast
				case PING: {

					this.logMessage("pingCASE", "Recebida mensagem PING");
					if (!this.newsWindow.containsKey(this.packet.getUser().getName().toString())) {
						this.newsClient.requestUnknownUserInfo(this.packet.getPacketIP());
					}
					this.newsClient.activePacket(this.packet.getSequence(), this.packet.getPacketIP());

					break;
				}
					
				// caso seja um TITLES envia um PROVIDE_TITLES
				case TITLES: {

					this.logMessage("titlesCASE", "Recebida mensagem TITLES ");
					this.newsClient.provideTitlesPacket(this.packet.getSequence(), this.packet.getPacketIP());

					break;
				}

				// marca o ack respectivo como recebido e processa os titulos apresentado-os e gravando-os na estrutura de dados do utilizador associado
				case PROVIDE_TITLES: {

					this.logMessage("provide_titlesCASE", "Recebida mensagem PROVIDE_TITLES");

					AckMessage tmpAckMessage = this.transmiter.getAck(this.packet.getSequence());

					if (tmpAckMessage != null) {
						if (tmpAckMessage.getSendPacket().getType().equals(MessageType.TITLES)) {
							this.transmiter.receivedAck(tmpAckMessage.getSequenceNumber());

							LinkedList<MessageData> titlesList = this.packet.getDataList();
							NavigableMap<Long, String> userTitlesList = null;
							int totalOfTitles;
							MessageData tmpEntry1, tmpEntry2;
							String commandResult = new String();

							if (titlesList != null) {

								tmpEntry1 = titlesList.pollFirst();
								if (tmpEntry1.getKey().equals(MessageDataType.NUMBER)) {
									totalOfTitles = Integer.parseInt(tmpEntry1.getValue());
									if (totalOfTitles == ((int) titlesList.size() / 2)) {
										userTitlesList = new TreeMap<Long, String>();
										commandResult = "\ntitles " + this.packet.getUser().getName() + " - Total: " + tmpEntry1.getValue();
										commandResult += ("\n"
												+ "\n<title>\t\t\t\t<data da noticia>\n");

										while ((tmpEntry1 = titlesList.pollFirst()) != null) {
											commandResult += ("\n" + tmpEntry1.getValue());
											tmpEntry2 = titlesList.pollFirst();
											commandResult += ("\t\t" + formatDate(Long.parseLong(tmpEntry2.getValue())) + "\n");
											userTitlesList.put(Long.parseLong(tmpEntry2.getValue()), tmpEntry1.getValue());
										}
										this.newsWindow.getUserFromName(this.packet.getUser().getName()).setNewsList(userTitlesList);
									}
									else {
										commandResult = "\nA lista de titulos não está consistente com o campo NUMBER!";
									}
								}
							}
							else {
								commandResult = "\nO execucao do comando falhou!";
							}
							
							this.newsWindow.appendCommandResultToOutputArea("titles " + this.packet.getUser().getName(), commandResult);
						}
					}

					break;
				}

				// envia o ack da mensagem de NEWS para o host que pediu noticias, espera 3 segundos e inicia o client TCP para enviar a informacao
				case NEWS: {

					File[] listOfFiles = null;
					TCPClientNews clientTCP = null;
					MessageData tmpMessage;
					IPStruct serverTCP;
					int numOfFilesToRead = 0;

					this.logMessage("NEWS_CASE", "Recebida mensagem NEWS");
					this.newsClient.newsAckPacket(this.packet.getSequence(), this.packet.getPacketIP());

					this.waitSomeTime(1);

					if ((listOfFiles = this.newsClient.readDirFiles()) != null) {

						serverTCP = this.packet.getPacketIP();

						try {
							while ((tmpMessage = this.packet.getDataList().pollFirst()) != null) {
								switch (tmpMessage.getKey()) {
									case NUMBER: {
										numOfFilesToRead = Integer.parseInt(tmpMessage.getValue());
										break;
									}
									case PORT: {
										serverTCP.setPort(Integer.parseInt(tmpMessage.getValue()));
									}
								}
							}
							clientTCP = new TCPClientNews(serverTCP, (INewsWindow) this.newsWindow);
							clientTCP.sendFilesInfo(listOfFiles, numOfFilesToRead);

						} catch (IOException ex) {
							//Logger.getLogger(HandleReceivedMessages.class.getName()).log(Level.SEVERE, null, ex);
							this.logMessage("newsCASE", "O servidor fechou a ligacao!");
						} finally {
							if (clientTCP != null) {
								clientTCP.endClient();
							}
						}
					}

					break;
				}

				// marca o ack respectivo como recebido e processa as noticias
				case NEWS_ACK: {

					AckMessage tmpAckMessage = this.transmiter.getAck(this.packet.getSequence());

					if (tmpAckMessage != null) {
						if (tmpAckMessage.getSendPacket().getType().equals(MessageType.NEWS)) {
							this.transmiter.receivedAck(this.packet.getSequence());
						}
					}

					this.newsClient.receiveNews(this.packet.getUser().getName());

					break;

				}

				// envia o rank para o utilizador do utilizador pedido
				case RANK: {

					this.logMessage("RANK_CASE", "Recebida mensagem RANK");
					MessageData tmpData = this.packet.getDataList().pollFirst();
					if (tmpData.getKey().equals(MessageDataType.TARGET)) {
						this.newsClient.rankAckPacket(this.packet.getSequence(), this.packet.getPacketIP(), tmpData.getValue());

					}

					break;
				}

				// marca o ack como recebido e processa o valor recebido apresentando a informacao caso seja o ultimo
				case RANK_ACK: {

					AckMessage tmpAckMessage = this.transmiter.getAck(this.packet.getSequence());

					if (tmpAckMessage != null) {
						if (tmpAckMessage.getSendPacket().getType().equals(MessageType.RANK)) {
							this.transmiter.receivedAck(this.packet.getSequence());
						}
					}
					
					LinkedList<MessageData> dataList = packet.getDataList();
					MessageData tmpData;
					String user;
					if (dataList.size() >= 2) {
						tmpData = dataList.pollFirst();
						if (tmpData.getKey().equals(MessageDataType.TARGET)) {
							user = tmpData.getValue();
							tmpData = dataList.pollFirst();
							if (tmpData.getKey().equals(MessageDataType.SCORE)) {
								this.newsClient.processRankForUser(user, Integer.valueOf(tmpData.getValue()), true);
								this.logMessage("RANK_ACK_CASE", "Recebido Rank para " + user);
							}
						}
					}
					else this.logMessage("RANK_ACK_CASE", "Mensagem mal formada");

					break;

				}

				// caso seja um BYE remove o utilizador da lista
				case BYE: {

					this.logMessage("BYE_CASE", "Recebida mensagem BYE");
					updateUser = this.newsWindow.getUserFromName(this.packet.getUser().getName());
					if (updateUser != null) {
						this.newsWindow.removeFromListByName(this.packet.getUser().getName());
					}

					break;
				}
			}

			// actualiza o ultimo acesso do utilizador que enviou o pacote
			if (!this.packet.getType().equals(MessageType.DUPLICATE)) {
				updateUser = this.newsWindow.getUserByIP(packet.getPacketIP());
				if (updateUser != null) {
					this.logMessage("readPacket", "Acesso de utilizador: " + updateUser.getName());
					updateUser.updateLastConn();
				}
			}
		}

	}

	/**
	 * Prints the message to the log area of the news window and system output
	 * @param func function sending the message
	 * @param message message to print
	 */
	private void logMessage(String func, String message) {
		this.newsWindow.appendTextToLogArea("[HandleReceivedMessage]:[" + func + "] -> " + message);
		System.out.println("[HandleReceivedMessage]:[" + func + "] -> " + message);
	}

	/**
	 * Formats the date
	 * @param unixTimeStamp date in unix time stamp
	 * @return String with the date
	 */
	private String formatDate(Long unixTimeStamp) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		return dateFormat.format(new Date(unixTimeStamp * 1000));
	}

	/**
	 * Waits until the time is equal or bigger
	 * @param timeToWait time to wait in seconds
	 */
	private void waitSomeTime(long timeToWait) {

		long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < (timeToWait * 1000));
	}
}
