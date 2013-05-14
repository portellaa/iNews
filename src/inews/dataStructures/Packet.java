package inews.dataStructures;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Class with the information of a message or packet sended to the network
 * Its used to build the packet before send and to read the received message
 * @see MessageType
 * @see UserStruct
 * @see IPStruct
 * @author Luis Portela
 */
public class Packet implements Serializable {
	
	private final MessageType type;
	private final UserStruct user;
	private final IPStruct dataPacketIP;
	private int sequence;
	
	private final LinkedList<MessageData> dataList;
	
	/**
	 * Initializes a packet with the provided information
	 * Keeps all information needed to the packet to be sent
	 * @param type type of message
	 * @param user user sending
	 * @param dataPacketIP destination IP to send
	 * @param sequence sequence number to be used in the message
	 * @param extraList list with the data different of TYPE, SEQUENCE, USER
	 * @see MessageType
	 * @see UserStruct
	 * @see IPStruct
	 * @see MessageData
	 */
	public Packet(MessageType type, UserStruct user, IPStruct dataPacketIP, int sequence, LinkedList<MessageData> extraList) {
		
		this.type = type;
		this.user = new UserStruct(user);
		this.dataPacketIP = new IPStruct(dataPacketIP);
		this.sequence = sequence;
		if (extraList != null)
			this.dataList = new LinkedList<MessageData>(extraList);
		else this.dataList = extraList;
	}
	
	/**
	 * Builds a packet from a received message
	 * Parses the message and create the needed value
	 * @param buf buffer with byte data
	 * @param length size of the buffer
	 * @param source source of the message
	 * @see MessageDataType
	 * @see MessageData
	 */
	public Packet(byte buf[], int length, IPStruct source) {
		
		// sender of the message
		this.dataPacketIP = source;
		
		String content = new String(buf, 0, length);
		content = content.substring(0, content.indexOf('\0'));
		String[] tokens = content.split("\n");
		int actualIdx = 0;
		
		// usual information in all the packets
		this.type = MessageType.valueOf(tokens[actualIdx++]);
		this.user = new UserStruct(tokens[actualIdx++].substring("USER: ".length()), this.dataPacketIP);
		this.sequence = Integer.parseInt(tokens[actualIdx++].substring("SEQUENCE: ".length()));
		
		// creates a news list
		this.dataList = new LinkedList<MessageData>(); // foi criado aqui senão o constructor queixa-se que não foi inicializada
		
		if (actualIdx < tokens.length) {
			
			int strIdx;

			// parse the different data of the packet and adds to the LinkedList
			while (actualIdx < tokens.length) {
				strIdx = tokens[actualIdx].indexOf(": ");

				this.dataList.add(new MessageData(MessageDataType.valueOf(tokens[actualIdx].substring(0, strIdx).toUpperCase()), 
						tokens[actualIdx].substring(strIdx+2)));
				actualIdx++;
			}
		}
	}

	/**
	 * Returns the IP packet
	 * @return IP of the packet
	 * @see IPStruct
	 */
	public IPStruct getPacketIP() {
		return dataPacketIP;
	}

	/**
	 * Returns the sequence number of the packet/message
	 * @return int - sequence number
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * Returns the packet type
	 * @return MessageType message type
	 * @see MessageType
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Returns the user who sends the packet
	 * @return UserStruct user of packet
	 * @see UserStruct
	 */
	public UserStruct getUser() {
		return user;
	}

	/**
	 * Returns the unusual list of data
	 * @return data list
	 * @see MessageData
	 */
	public LinkedList<MessageData> getDataList() {
		return this.dataList;
	}
	
	/**
	 * Returns byte array with the information on the class
	 * @return byte array with the information
	 * @see MessageData
	 */
	public byte[] getBytes(){
		
		String tmp;
		MessageData tmpEntry;
		// usual information of a packet
		tmp = this.type.toString() + "\n" +
				"USER: " + this.user.getName() + "\n"+
				"SEQUENCE: " + String.valueOf(this.sequence);
		
		// the unusual information if it exists
		if (this.dataList != null) {
			
			LinkedList<MessageData> tmpDataList = new LinkedList<MessageData>(this.dataList);
			
			tmp = tmp + "\n";
			
			while ((tmpEntry = tmpDataList.pollFirst()) != null) {
				
				tmp = tmp + (tmpEntry.getKey() + ": " + tmpEntry.getValue());
				
				if (tmpDataList.size() != 0) tmp = tmp + "\n";
			}
		}
		return (tmp + '\0').getBytes();
	}
	
	/**
	 * Builds and returns the datagrama packet to send to the network
	 * @param host host datagram packet destination
	 * @param port port of the remote user
	 * @return DatagramPacket builded datagram packet to send
	 * @see DatagramPacket
	 * @throws UnknownHostException 
	 */
	public DatagramPacket buildDatagramPacket(String host, int port) throws UnknownHostException {
		
		byte buf[] = this.getBytes();
		return new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port);
	}
	
	/**
	 * Builds and returns the datagrama packet to send to the network
	 * @return DatagramPacket builded datagram packet to send
	 * @see Packet#buildDatagramPacket(java.lang.String, int) 
	 * @see DatagramPacket
	 * @throws UnknownHostException 
	 */
	public DatagramPacket buildDatagramPacket() throws UnknownHostException {
		return this.buildDatagramPacket(this.dataPacketIP.getIp(), this.dataPacketIP.getPort());
	}
}