package inews.client;

import inews.dataStructures.*;
import inews.interfaces.INewsWindow;
import inews.interfaces.NewsWindowClient;
import inews.interfaces.UDPConnection;
import inews.server.TCPServerNews;
import inews.server.TransmissionHandler;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to create the packages and send them to be processed by the TransmissionHandler class
 * This class have the functions to the NewsWindow send messages to the network
 * @see TransmissionHandler
 * @see NewsWindowClient
 * @see NewsWindow
 * @author Luis Portela
 */
public final class UDPClientNews implements NewsWindowClient {
	
	public static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss";
	
	private static UDPClientNews mySelf = null;
	private INewsWindow newsWindow = null;
	
	private UDPConnection udpConn;
	private UserStruct user = null;
	private IPStruct broadcast = null;
	private int sequenceNumber;
	
	private TCPServerNews serverTCP;
	private NavigableMap<String, Rank> usersRank = null;
	private NavigableMap<String, Integer> usersCount = null;
	private Integer usersListSize = null;

	/**
	 * Private constructor to create an instance of this class
	 * @param conn instance of the transmission handler
	 * @param user instance of the local user
	 * @param broadcast the broadcast ip and the port to comunicate
	 * @param newsWindow pointer to the newsWindow
	 * @see TransmissionHandler
	 * @see UserStruct
	 * @see IPStruct
	 * @see INewsWindow
	 */
	private UDPClientNews(UDPConnection conn, UserStruct user, IPStruct broadcast, INewsWindow newsWindow) {
		
		this.udpConn = conn;
		this.user = user;
		this.broadcast = broadcast;
		this.sequenceNumber = 0;
		this.newsWindow = (INewsWindow) newsWindow;
	}
	
	/**
	 * Returns the instance of this class or creates a new one and returns if it doesn't exists
	 * @param conn instance of the transmission handler
	 * @param user instance of the local user
	 * @param broadcast the broadcast ip and the port to comunicate
	 * @param newsWindow pointer to the newsWindow
	 * @return the existing instance of this class or the new one created
	 * @see TransmissionHandler
	 * @see UserStruct
	 * @see IPStruct
	 * @see INewsWindow
	 */
	public static UDPClientNews getInstance(UDPConnection conn, UserStruct user, IPStruct broadcast, INewsWindow newsWindow){
		if (mySelf == null)
			mySelf = new UDPClientNews(conn, user, broadcast, (INewsWindow)newsWindow);
		
		return mySelf;
	}
	
	/**
	 * Sends the initial SENSE packets
	 * Used by the NewsWindows when it starts
	 */
	@Override
	public void applicationStarted() {
		this.sensePacket(this.getSequence());
	}

	/**
	 * Receives the input text of NewsWindow input area, parse it and send the messages
	 * @param arg text on the input area
	 */
	@Override
	public void textAvailable(String arg) {
		
		while (arg.endsWith(" ")) {
			arg = arg.substring(0, arg.lastIndexOf(" "));
		}
		arg = arg.replaceAll("(\\n|\\r|\\r\\n)", "");
		String[] tokens = arg.split(" ");
		String commandResult = new String();
		
		// Caso o utilizador tenha pedido a lista de utilizadores
		if (tokens.length > 0) {
			
			if (tokens[0].equalsIgnoreCase("LIST")) {
				
				UserStruct tmpUser;
				Iterator<UserStruct> usersList = this.newsWindow.getUserListIterator();
				String tmp;
				int usersCount = 0;
		
				tmp = ("\n"+"<nick>\t\t<IP address>\t\t<No of Titles>\t\t<Data ultimo>\n\n");
				
				while (usersList.hasNext()) {
					tmpUser = usersList.next();
					tmp += tmpUser.toString("\t") + "\n\n";
					usersCount++;
				}

				commandResult = "list " + " - Total Utilizadores: " + usersCount + "\n";
				commandResult += tmp;
				
				this.newsWindow.appendCommandResultToOutputArea(tokens[0], commandResult);
			}
			else if (tokens[0].equalsIgnoreCase("RANK")) {
				
				commandResult = tokens[0];
				
				if (tokens.length >= 1) {
					
					switch (tokens.length) {
						// caso o utilizador tenha pedido o rank para todos os utilizadores ligados
						case 1: {
							
							Iterator<UserStruct> usersList = this.newsWindow.getUserListIterator();
							UserStruct tmpUser;
							
							this.usersCount = new TreeMap<String, Integer>();
							this.usersRank = new TreeMap<String, Rank>();
							this.usersListSize = this.newsWindow.getUsersCount();
							
							this.getRankForUser(this.user.getName());
							
							while (usersList.hasNext()) {
								tmpUser = usersList.next();
								this.getRankForUser(tmpUser.getName().toString());
							}
							
							break;
						}
						// caso o utilizador tenha pedido o rank para só um utilizador
						case 2: {
							
							this.usersCount = new TreeMap<String, Integer>();
							this.usersRank = new TreeMap<String, Rank>();
							this.usersListSize = this.newsWindow.getUsersCount();
							
							this.getRankForUser(tokens[1]);
							
							break;
						}
						// caso o utilizador queira fornecer rank a um utilizador ligado
						case 3: {
							commandResult += (" " + tokens[1] + " " + tokens[2]);
							UserStruct userToRank = this.newsWindow.getUserFromName(tokens[1].toString());
							
							if ((userToRank != null) && (!this.user.getName().equalsIgnoreCase(tokens[1]))) {
//								userToRank.setRank(Double.valueOf(tokens[2].replaceAll(",", ".")));
								if (tokens[2].matches("[1-5]")) {
									userToRank.setRank(Integer.valueOf(tokens[2]));
									commandResult += "\n\nRank: " + tokens[2] + " atribuido a utilizador: " + tokens[1];
									this.logMessage("textAvailable", "RANK: Utilizador: "+tokens[1]+" com rank: "+tokens[2]);
								}
								else commandResult += "\n\nPor favor introduza um valor entre 1 e 5";
							}
							else {
								commandResult += "\n\nO utilizador " + tokens[1] + " não existe!";
								this.logMessage("textAvailable", "RANK -> O utilizador "+tokens[1]+" não existe!");
							}
							
							this.newsWindow.appendCommandResultToOutputArea(tokens[0] + " " + tokens[1] + " " + tokens[2], commandResult);
							
							break;
						}
					}
				}
				
			}
			// caso o utilizador tenha pedido para ver a lista de titulos de um utilizador
			else if (tokens[0].equalsIgnoreCase("TITLES")) {
				
				if (tokens.length > 1) {			
					UserStruct origDestination = this.newsWindow.getUserFromName(tokens[1].toString());
					
					if (origDestination != null) {
						this.titlesPacket(sequenceNumber, origDestination.getIp());
					}
					else this.newsWindow.appendCommandResultToOutputArea(tokens[0] + " " + tokens[1], "O Utilizador nao existe!");
				}
				else this.newsWindow.appendCommandResultToOutputArea(tokens[0], "  Usage: titles <user>");
				
			}
			// caso o utilizador tenha pedido para ver as noticias de um utilizador
			else if (tokens[0].equalsIgnoreCase("NEWS")) {
				
				UserStruct origDestination;
				
				int newsCount = 0;
				
				if (tokens.length >= 2) {
					
					if (tokens.length == 3) newsCount = Integer.parseInt(tokens[2]);
					
					if ((origDestination = this.newsWindow.getUserFromName(tokens[1])) != null) {
						// code before send the news from the TCP connection
//						if (origDestination.getNewsList() == null) // caso o utilizador ainda não tenha a lista de noticias de um utilizador envia um titles para obte-la
//							this.titlesPacket(this.getSequence(), origDestination.getIp());
						this.newsPacket(this.getSequence(), origDestination.getIp(), newsCount);
					}
					else this.newsWindow.appendCommandResultToOutputArea("", "O utilizador " + tokens[1] + " nao existe!");
				}
				else this.newsWindow.appendCommandResultToOutputArea(tokens[0], "Usage: news <user>  or  news <user> <num of last news>");
				
			}
			// caso o utilizador tenha introduzido na input area que deseja fechar o programa
			else if (tokens[0].equalsIgnoreCase("QUIT")) {
				
				this.newsWindow.appendCommandResultToOutputArea(arg, "");
				
				this.closeApplication();
				this.newsWindow.closeApplication("A fechar aplicacao", 0);
			}
			else if (tokens[0].equalsIgnoreCase("CLEAR")) {
				this.newsWindow.clearOutputArea();
				
			}
			// imprime todos os comandos possiveis
			else {
				commandResult = "\nclear\n\tLimpa a Output Area"+
						"\nlist\n\tLista todos os utilizadores activos"+
						"\n\nrank\n\tlista o rank de todos os utilizadores activos"+
						"\nrank <nick>\n\tLista o rank do utilizador <nick>"+
						"\nrank <nick> <valor>\n\tAtribui rank ao utilizador com o nick <nick>"+
						"\n\ntitles <nick>\n\tPede e Lista os titulos das noticias do utilizador <nick>"+
						"\n\nnews <nick>\n\tPede e Lista todas as noticias do utilizador <nick>"+
						"\nnews <nick> <num ultimas noticias>\n\tLista as <num ultimas noticias> do utilizador <nick>"+
						"\n\nquit\n\tTermina aplicacao";
				
				this.newsWindow.appendCommandResultToOutputArea(tokens[0], commandResult);
			}
		}
	}

	/**
	 * Sends the BYE packets to the network when the application is closed
	 * Called by the NewsWindow when the user closes the window or by this class when the user writes quit in the input area
	 */
	@Override
	public void windowClosed() {
		
		System.out.println("[UDPClientNews]:[windowClosed] -> A Fechar aplicacao");
		this.closeApplication();
	}

	/**
	 * Sends the initial ACTIVE packets when the application opens
	 * Its called after the application start
	 */
	@Override
	public void windowOpened() {
		
		this.logMessage("windowOpened", "Janela Aberta");
		
		this.activePacket(this.getSequence(), this.broadcast);
		this.logMessage("windowOpened", "Enviadas mensagens ACTIVE");
		
		this.waitSomeTime(2);
			
		this.activePacket(this.getSequence(), this.broadcast);
		this.logMessage("windowOpened", "Enviada segunda mensagem ACTIVE");
		
		this.pingPacket(this.getSequence(), this.broadcast, 2, 2000);
		this.logMessage("windowOpened", "Enviada mensagem PING");
		
		this.newsWindow.enableApplication();
	}
	
	/**
	 * Sends the BYE messages and finalize the this class
	 */
	private void closeApplication() {
		
		this.logMessage("closeApplication", "Fechando a aplicacao");
		
		this.udpConn.setRunning(false);
		
		this.byePacket(this.getSequence(), this.broadcast);
		logMessage("closeApplication", "Enviada primeira mensagem BYE");
		
		this.waitSomeTime(2);
			
		this.byePacket(this.getSequence(), this.broadcast);
		logMessage("closeApplication", "Enviada segunda mensagem BYE");
		
		this.broadcast = null;
		this.udpConn = null;
		this.user = null;
		
		try {
			this.finalize();
		} catch (Throwable ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Request the information of an unknown user
	 * @param destination IP of the user
	 * @see IPStruct
	 */
	@Override
	public void requestUnknownUserInfo(IPStruct destination) {
		this.pingPacket(this.getSequence(), destination, 1, 3000);
	}
	
	/**
	 * Sends the local user info to the network
	 */
	@Override
	public void sendMyInfo() {
		this.activePacket(this.getSequence(), this.broadcast);
	}

	/**
	 * Sends a ping packet to the user with the IP destination
	 * @param destination IP of the user
	 */
	@Override
	public void pingUser(IPStruct destination) {
		this.pingPacket(this.getSequence(), destination, 3, 3000);
	}
	
	/* ---------------------- Criacao das mensagens enviadas para a rede ------------------------ */
	
	/**
	 * Creates a SENSE message and sends it to the Transmission Handler
	 * This message waits for acknowledge and is retransmited 2 times
	 * @param sequence sequence number to use in the packet
	 * @see Packet
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 * @see MessageType
	 * @see IPStruct
	 */
	private synchronized void sensePacket(int sequence) {
		try {
			Packet msg = new Packet(MessageType.SENSE, this.user, this.broadcast, sequence, null);
			this.sendPacket(msg, 2, 2000);
			this.logMessage("sensePacket", "Enviada mensagem SEQUENCE com o sequence number: " + sequence);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a ACTIVE message and sends it to the Transmission Handler
	 * @param sequence sequence number to be used in the message
	 * @param destination destination of the message; this can be a broadcast message or a unicast message
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 */
	@Override
	public synchronized void activePacket(int sequence, IPStruct destination) {
		try {
			LinkedList<MessageData> tmpList = new LinkedList<MessageData>();
			tmpList.add(new MessageData(MessageDataType.NUMBER, String.valueOf(this.user.getTotalNews())));
			tmpList.add(new MessageData(MessageDataType.DATE, String.valueOf(this.user.getLastNewDate())));
			Packet msg = new Packet(MessageType.ACTIVE, this.user, destination, sequence, tmpList);
			this.sendPacket(msg);
			this.logMessage("activePacket", "Enviada mensagem ACTIVE com o sequence number: " + sequence);		
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a DUPLICATE message and sends it to the Transmission Handler
	 * This message is send when other user try to login it the same nick of the local user
	 * @param sequence sequence number to be used in the message
	 * @param destination  destination of the message; the unicast address of the user trying to login
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 */
	@Override
	public synchronized void duplicatePacket(int sequence, IPStruct destination) {
		try {
			Packet msg = new Packet(MessageType.DUPLICATE, this.user, destination, sequence, null);
			this.sendPacket(msg);
			this.logMessage("duplicatePacket", "Enviada mensagem DUPLICATE com o sequence number: " + sequence);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a PING message and sends it to the Transmission Handler
	 * This one can be send to the broadcast or to an user to know if it's alive and it's information
	 * If it's a broadcast message it doesn't have acknowledge; if is an unicast message is send to the user with acknowledge
	 * @param sequence sequence number to be used on the message
	 * @param destination destination of the message; can be to the broadcast address or to an unicast address of an user
	 * @param times number of times to retransmit the message
	 * @param timeout time to wait between the retransmissions
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 */
	private void pingPacket(int sequence, IPStruct destination, int times, int timeout) {
		
		try {
			Packet tmpPacket = new Packet(MessageType.PING, this.user, destination, sequence, null);
			if (destination.equals(broadcast))
				this.sendPacket(tmpPacket);
			else this.sendPacket(tmpPacket, times, timeout);
			this.logMessage("pingPacket", "Enviada mensagem PING com o sequence number: " + sequence);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a TITLES message and sends it to the Transmission Handler
	 * This message have acknowledge and it's retransmited 3 times with a timeout of 3 seconds
	 * @param sequence sequence number to be used in the message
	 * @param destination unicast IP address of the user to send the message
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 */
	private void titlesPacket(int sequence, IPStruct destination) {
		
		try {
			Packet tmpPacket = new Packet(MessageType.TITLES, this.user, destination, sequence, null);
			this.sendPacket(tmpPacket, 3, 3000);
			this.logMessage("titlesPacket", "Enviada mensagem TITLES com o sequence number: " + sequence);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a PROVIDES_TITLES message and sends it to the Transmission Handler
	 * It is an answer to TITLES message and is send to the unicast address of the TITLES message sender
	 * @param sequence sequence number to be used in the message
	 * @param destination IP of the TITLES message sender
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 */
	@Override
	public synchronized void provideTitlesPacket(int sequence, IPStruct destination) {
		
		LinkedList<MessageData> titlesList;
		Packet tmpPacket;
		
		if ((titlesList = readTitleFiles(readDirFiles())) != null) {
			try {
				tmpPacket = new Packet(MessageType.PROVIDE_TITLES, this.user, destination, sequence, titlesList);
				this.sendPacket(tmpPacket);
			} catch (IOException ex) {
				Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		else {
			this.logMessage("provideTitlesPacket", "Erro ao ler os ficheiros!");
		}
	}
	
	/**
	 * Creates a NEWS message and sends it to the Transmission Handler and the TCP server to receive the news
	 * It can retrieve all the messages from the user or only a number specified by the user
	 * @param sequence sequence number to use on the packet
	 * @param destination destinatino of the packet; the unicast address of the user to retrieve the news
	 * @param newsCount number of latest news to retrieve
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see MessageData
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 */
	private void newsPacket(int sequence, IPStruct destination, int newsCount) {

		LinkedList<MessageData> dataList = new LinkedList<MessageData>();

		try {
			serverTCP = new TCPServerNews((INewsWindow) this.newsWindow, (this.broadcast.getPort() + 1001));
			dataList.add(new MessageData(MessageDataType.NUMBER, String.valueOf(newsCount)));
			dataList.add(new MessageData(MessageDataType.PORT, String.valueOf(serverTCP.getPort())));
			Packet tmpPacket = new Packet(MessageType.NEWS, this.user, destination, sequence, dataList);
			this.logMessage("newsPacket", "Enviada mensagem NEWS com o sequence number: " + sequence);
			this.sendPacket(tmpPacket, 3, 3000);
		} catch (IOException ex) {
			this.logMessage("newsPacket", "Não foi possivel enviar a mensagem de news");
			dataList = null;
			this.serverTCP = null;
		}
	}
	
	/**
	 * Creates a NEWS_ACK message and sends it to the Transmission Handler
	 * It is normally send to response a NEWS message
	 * @param sequence sequence number to be used in the message
	 * @param destination unicast address of the NEWS sender
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 */
	@Override
	public synchronized void newsAckPacket(int sequence, IPStruct destination) {
		try {
			Packet tmpPacket = new Packet(MessageType.NEWS_ACK, this.user, destination, sequence, null);
			this.sendPacket(tmpPacket);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a RANK message and sends it to the Transmission Handler
	 * It's send to an user to retrieve ohter user rank
	 * @param sequence sequence number to be used in the message
	 * @param destination unicast address of the user to send the message
	 * @param nick user identification to retrieve the rank
	 * @see MessageData
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet, int, int) 
	 */
	private void rankPacket(int sequence, IPStruct destination, String nick) {
		
		try {
			LinkedList<MessageData> dataList = new LinkedList<MessageData>();
			dataList.add(new MessageData(MessageDataType.TARGET, nick));
			Packet tmpPacket = new Packet(MessageType.RANK, this.user, destination, sequence, dataList);
			this.sendPacket(tmpPacket, 3, 3000);
			
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Creates a RANK_ACK message and sends it to the Transmission Handler
	 * It is normally send to response a RANK message
	 * It sends the rank of a requested user
	 * @param sequence sequence number to be used on the message
	 * @param destination unicast address of the user to send the message
	 * @param targetUser user identification of the user who's rank is been asked
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see MessageData
	 * @see MessageDataType
	 */
	@Override
	public synchronized void rankAckPacket(int sequence, IPStruct destination, String targetUser) {

		UserStruct tmpUser = this.newsWindow.getUserFromName(targetUser);
		if ((tmpUser != null) && (tmpUser.getRank() != 0)) {
			try {
				LinkedList<MessageData> dataList = new LinkedList<MessageData>();
				dataList.add(new MessageData(MessageDataType.TARGET, targetUser));
				dataList.add(new MessageData(MessageDataType.SCORE, String.valueOf(tmpUser.getRank())));
				Packet tmpPacket = new Packet(MessageType.RANK_ACK, this.user, destination, sequence, dataList);
				this.sendPacket(tmpPacket);
				this.logMessage("rankAckPacket", "Enviada mensagem de RANK_ACK");
			} catch (IOException ex) {
				Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	/**
	 * Creates a BYE message and sends it to the Transmission Handler
	 * This message is send to the broadcast address
	 * @param sequence sequence number to be used on the message
	 * @param destination broadcast address
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 */
	private void byePacket(int sequence, IPStruct destination) {
		
		try {
			Packet tmpPacket = new Packet(MessageType.BYE, this.user, destination, sequence, null);
			this.sendPacket(tmpPacket);
			this.logMessage("byePacket", "Enviada mensagem BYE com o sequence number: " + sequence);
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}
	
	/* --------------------------------------------------------------------------------------------------------------- */
	
	/**
	 * Sends RANK messages to all the connected users requesting a user rank
	 * It creates the lists to calculate the rank when all the messages are received
	 * @param userNick name of the user to get the rank
	 * @see UserStruct
	 * @see UDPClientNews#rankPacket(int, inews.dataStructures.IPStruct, java.lang.String) 
	 * @see INewsWindow#getUserListIterator() 
	 */
	private synchronized void getRankForUser(String userNick) {
		
		UserStruct tmpUser;
		Iterator<UserStruct> usersList = this.newsWindow.getUserListIterator();
		
		if ((tmpUser = this.newsWindow.getUserFromName(userNick)) != null) {
			this.usersCount.put(userNick, 1);
			this.usersRank.put(userNick, new Rank(tmpUser.getRank(), 1));
		}
		else {
			this.usersCount.put(userNick, 0);
			this.usersRank.put(userNick, new Rank());
		}
		
		while (usersList.hasNext()) {
			tmpUser = usersList.next();
			if (!userNick.toString().equals(tmpUser.getName().toString())) {
				this.rankPacket(this.getSequence(), tmpUser.getIp(), userNick);
				this.logMessage("getRankForUser", "Enviado pedido rank para utilizador " + tmpUser.getName());
			}
		}
	}
	
	/**
	 * Sends the message to the TransmissionHandler
	 * @param packet packet with the data to send
	 * @throws IOException 
	 * @see Packet
	 * @see TransmissionHandler#send(inews.dataStructures.Packet) 
	 */
	private void sendPacket(Packet packet) throws IOException {
		this.udpConn.send(packet);
		this.logMessage("sendPacket", "Pacote Enviado - Novo Sequence Number: " + this.sequenceNumber + "!");
	}
	
	/**
	 * Sends the message to the TransmissionHandler with acknowledge
	 * @param packet packet with data to send
	 * @param retries number of retries
	 * @param timeout time between the retries
	 * @throws IOException 
	 * @see Packet
	 * @see TransmissionHandler#sendWithAck(inews.dataStructures.Packet, int, int) 
	 */
	private void sendPacket(Packet packet, int retries, int timeout) throws IOException {
		this.udpConn.sendWithAck(packet, retries, timeout);
		this.logMessage("sendPacket", "Pacote Enviado - Novo Sequence Number: " + this.sequenceNumber + "!");
	}
	
	/**
	 * Waits for the message of the TCP server and prints it to the NewsWindow
	 * @param user user sending the news
	 * @see NavigableMap
	 * @see TCPServerNews
	 * @see INewsWindow#getUserListIterator() 
	 */
	@Override
	public void receiveNews(String user) {
		
		String receivedMessage = new String();
		String[] tokens;
		
		String commandResult = "";
		
		try {
			
			this.logMessage("receivedNews", "Vou por o servidor de TCP em escuta");
			receivedMessage = serverTCP.waitForMessage();
			
			serverTCP.endServer();
			serverTCP = null;
			
			// old code when the application do not sends the titles in the TCP connection
//			NavigableMap<Long, String> userTitlesList = this.newsWindow.getUserFromName(user).getNewsList();
//			
//			tokens = receivedMessage.split("(\0)");
//			for (int i = 0; i < tokens.length ; i++) {
//				commandResult += "\n" + userTitlesList.values().toArray()[userTitlesList.values().size() - tokens.length + i].toString() + ":"
//						+"\n" + tokens[i] + "\n";
//			}
			
			// code to parce the received message and prints to the windows output area
			tokens = receivedMessage.split("(\0)");
			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].replaceFirst("\n", ":\n");
				for (String eachLine : tokens[i].split("\n")) {
					commandResult += "\n" + eachLine;
				}
				
				if (i < (tokens.length - 1)) {
					commandResult += "\n\t\t------------------------------------------------------------\n";
				}
			}
			
			this.newsWindow.appendCommandResultToOutputArea("news " + user, commandResult);
			
		} catch (IOException ex) {
			Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}
	
	/**
	 * Processes the received message of a rank and if all the ranks for the users are received prints the results
	 * @param user user to provide rank
	 * @param rank rank value to the user
	 * @param valid true if the user response or false otherwise
	 */
	@Override
	public synchronized void processRankForUser(String user, int rank, boolean valid) {
		
		boolean allValuesReceived = true;
		Iterator<Integer> tmpUsersCount = null;
		Entry<String, Rank> tmpUsersRank = null;
		String command = null, commandResult = null;
		
		if (valid) {
			this.usersRank.get(user).addToValue(rank);
		}
		this.usersCount.put(user, this.usersCount.get(user) + 1);
		
		tmpUsersCount = this.usersCount.values().iterator();
		
		while (tmpUsersCount.hasNext()) {
			if (tmpUsersCount.next() < this.usersListSize) {
				allValuesReceived = false;
				break;
			}
		}
		
		if (allValuesReceived) {
			
			command = "rank";
			
			if (this.usersRank.size() == 1)
				command += " " + this.usersRank.firstKey();
			
			commandResult = ("\n<nick>\t\t<No of rank values>\t\t<Average rank score>\n");
			while ((tmpUsersRank = this.usersRank.pollFirstEntry()) != null) {
				if (tmpUsersRank.getValue().isValid()) {
					commandResult += ("\n" + tmpUsersRank.getKey()
							+ "\t\t" + tmpUsersRank.getValue().getCount()
							+ "\t\t" + tmpUsersRank.getValue().getRankMed());
				}
				else {
					commandResult += ("\n" + tmpUsersRank.getKey() + "\t\t - \t\t -");
				}
			}
			this.newsWindow.appendCommandResultToOutputArea(command, commandResult);
			this.usersCount = null;
			this.usersRank = null;
		}
		
	}
	
	/**
	 * Read all the titles from the files in the news dir
	 * @param listOfFiles list of Files in the news dir
	 * @return a list it all the titles
	 * @see File
	 * @see UDPClientNews#readDirFiles() 
	 */
	private LinkedList<MessageData> readTitleFiles(File[] listOfFiles) {
		
		int finalIdxName;
		
		LinkedList<MessageData> filesTitles = null;
		BufferedReader fileReader;
		
		if (listOfFiles.length > 0) {
			filesTitles = new LinkedList<MessageData>();
			
			filesTitles.add(new MessageData(MessageDataType.NUMBER, Integer.toString(listOfFiles.length)));
		
			for (File newsFile : listOfFiles) {

				try {
					fileReader = new BufferedReader(new FileReader(newsFile));

					try {
						finalIdxName = newsFile.getName().lastIndexOf(".");
						finalIdxName = finalIdxName != -1 ? finalIdxName : newsFile.getName().length();
						
						filesTitles.add(new MessageData(MessageDataType.TITLE, fileReader.readLine()));
						
						filesTitles.add(new MessageData(MessageDataType.DATE, newsFile.getName().substring(0, finalIdxName)));
						
						this.logMessage("readTitleFiles", "Titulo ficheiro " + newsFile.getName() + " lido!");
					}
					finally {
						fileReader.close();
					}

				} catch (IOException ex) {
					this.logMessage("readTitlesFile", "Nao foi possivel abrir o ficheiro para leitura");

					Logger.getLogger(UDPClientNews.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		
		return filesTitles;
	}
	
	/**
	 * Reads the news directory and returns a list of Files
	 * @return list of Files
	 * @see File
	 */
	@Override
	public File[] readDirFiles() {
		
		File dir = new File("_news");
		if (dir == null)
			return null;
		
		FilenameFilter filtro = new FilenameFilter() {

			@Override
			public boolean accept(File file, String string) {
				return ((!string.startsWith(".")) && string.toLowerCase().endsWith("txt"));
			}
		};

		return dir.listFiles(filtro);
	}
	
	/**
	 * Prints the message to the system output and to the log area of the NewsWindow
	 * @param func function sending the message
	 * @param message message to print
	 * @see INewsWindow#appendTextToLogArea(java.lang.String) 
	 */
	private void logMessage(String func, String message) {
		this.newsWindow.appendTextToLogArea("[UDPClientNews]:["+func+"] -> " + message);
		System.out.println("[UDPClientNews]:["+func+"] -> " + message);
	}
	
	/**
	 * Wait some time
	 * @param timeToWait time to wait in seconds
	 */
	private void waitSomeTime(long timeToWait) {
		
		long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < (timeToWait * 1000));
	}
	
	/*
	 * Class with the rank to a user
	 * Keeps the sum of double values and the number of users that provide rank
	 */
	private class Rank {
		private double value;
		private int count;
		
		/**
		 * Initializes all values to zero
		 */
		Rank() {
			this.value = 0.0;
			this.count = 0;
		}
		
		/**
		 * Initializes the class values with the provided
		 * @param value value rank provided
		 * @param count users count
		 */
		Rank(double value, int count) {
			this.value = value;
			this.count = count;
		}

		/**
		 * Returns the number of users that have provided valid ranks
		 * @return number of valid ranks
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Returns the double rank value
		 * @return 
		 */
		public double getValue() {
			return value;
		}
		
		/**
		 * Returns the rank average
		 * @return the average
		 */
		public double getRankMed() {
			return (double)(this.value/((double)this.count));
		}
		
		/**
		 * Add a new valid rank value
		 * If the value is valid is added to the actual value and the count is incremented
		 * @param value value to sum
		 */
		public void addToValue(double value) {
			if (value != 0.0) {
				this.value += (double)value;
				this.count++;
			}
		}
		
		/**
		 * Increment the number of valid ranks
		 */
		public void incrementCount() {
			this.count++;
		}
		
		/**
		 * Check if the user rank is valid
		 * @return 
		 */
		public boolean isValid() {
			return ((this.count != 0) && (this.value != 0.0));
		}
	}

	/**
	 * Sets the transmission handler
	 * @param clientSocket transmission handler
	 */
	public void setClientSocket(UDPConnection clientSocket) {
		this.udpConn = clientSocket;
	}
	
	/**
	 * Returns the sequence value and updates it to the new one
	 * @return the sequence number to use
	 */
	private int getSequence() {
		return this.sequenceNumber++;
	}
	
}