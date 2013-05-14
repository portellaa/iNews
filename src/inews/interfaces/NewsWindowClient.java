package inews.interfaces;

import inews.dataStructures.IPStruct;
import java.io.File;

/**
 * @author Jo�o Paulo Barraca <jpbarraca@ua.pt>
 *
 * Interface class to use the NewsWindow
 *
 */
public interface NewsWindowClient {
	/**
	 * This method is called when new text was provided by the user in the Input Text Area
	 * 
	 * @param arg
	 */
	public void textAvailable(String arg);
	
	/**
	 * 	This method is called when the window is closed
	 * 
	 */
	public void windowClosed();
	
	/**
	 * 	This method is called when the window is started
	 *
	 */
	public void windowOpened();
	
	/**
	 * Sends the initial SENSE packets
	 * Used by the NewsWindows when it starts
	 */
	public void applicationStarted();
	
	/**
	 * Request the information of an unknown user
	 * @param destination IP of the user
	 * @see IPStruct
	 */
	public void requestUnknownUserInfo(IPStruct destination);
	
	/**
	 * Sends the local user info to the network
	 */
	public void sendMyInfo();
	
	/**
	 * Sends a ping packet to the user with the IP destination
	 * @param destination IP of the user
	 */
	public void pingUser(IPStruct destination);
	
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
	public void duplicatePacket(int sequence, IPStruct destination);
	
	/**
	 * Creates a ACTIVE message and sends it to the Transmission Handler
	 * @param sequence sequence number to be used in the message
	 * @param destination destination of the message; this can be a broadcast message or a unicast message
	 * @see Packet
	 * @see MessageType
	 * @see IPStruct
	 * @see UDPClientNews#sendPacket(inews.dataStructures.Packet) 
	 */
	public void activePacket(int sequence, IPStruct destination);
	
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
	public void provideTitlesPacket(int sequence, IPStruct destination);
	
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
	public void newsAckPacket(int sequence, IPStruct destination);
	
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
	public void rankAckPacket(int sequence, IPStruct destination, String targetUser);
	
	/**
	 * Reads the news directory and returns a list of Files
	 * @return list of Files
	 * @see File
	 */
	public File[] readDirFiles();
	
	/**
	 * Waits for the message of the TCP server and prints it to the NewsWindow
	 * @param user user sending the news
	 * @see NavigableMap
	 * @see TCPServerNews
	 * @see INewsWindow#getUserListIterator() 
	 */
	public void receiveNews(String user);
	
	/**
	 * Processes the received message of a rank and if all the ranks for the users are received prints the results
	 * @param user user to provide rank
	 * @param rank rank value to the user
	 * @param valid true if the user response or false otherwise
	 */
	public void processRankForUser(String user, int rank, boolean valid);
}
