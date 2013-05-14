package inews.dataStructures;

import java.io.Serializable;

/**
 * Enumerator with all the possible packet types
 * @author Luis Portela
 * @see Packet
 */
public enum MessageType implements Serializable {
	
	/**
	 * Checks the network from a user with the same name as the one provided on the packet
	 */
	SENSE (0),
	/**
	 * Response to a SENSE packet if the user trying to login is equal to local user
	 */
	DUPLICATE (1),
	/**
	 * Requests the information of the all the network hosts or an host
	 */
	PING (2),
	/**
	 * Sends the information of the host
	 */
	ACTIVE (3),
	/**
	 * Request the titles of an user
	 */
	TITLES (4),
	/**
	 * Response to a TITLES message with the user titles
	 */
	PROVIDE_TITLES (5),
	/**
	 * Request news from a user
	 */
	NEWS (6),
	/**
	 * Acknowledge to a NEWS message to the NEWS sender start the TCP Server
	 */
	NEWS_ACK (7),
	/**
	 * Request a rank packet
	 */
	RANK (8),
	/**
	 * Response to the rank packet with the rank value
	 */
	RANK_ACK (9),
	/**
	 * Sends the message to leave the network
	 */
	BYE (10);
	
	private int type;
	
	/**
	 * Initializes a new message type with the provided code
	 * @param type 
	 */
	private MessageType(int type) {
		this.type = type;
	}
	
	/**
	 * Returns the name of the type that match the provided index on the MessageType array
	 * @param index index to search
	 * @return the matched MessageType
	 */
	public static MessageType getTypeName(int index) {
		return MessageType.values()[index];
	}
	
	/**
	 * Returns the code of the message
	 * @return the code of the message
	 */
	public int getType (){
		return this.type;
	}
	
	/**
	 * Returns a byte array with the message type
	 * @return byte array with the message type
	 */
	public byte[] getTypeByte() {
		return this.name().getBytes();
	}
	
	/**
	 * Returns a message type with the provided name
	 * @param name name of the message
	 * @return the type with the name
	 */
	public MessageType getMessageType(String name) {
		return MessageType.valueOf(name);
	}
	
	/**
	 * Formats the type to string and returns it
	 * @return a string with the name
	 */
	@Override
	public String toString() {
		return this.name();
	}
	
	/**
	 * Compares two message types
	 * @param type type to compare
	 * @return true if equal ; false otherwise
	 */
	public boolean equals(MessageType type) {
		return this.getType() == type.getType();
	}
}