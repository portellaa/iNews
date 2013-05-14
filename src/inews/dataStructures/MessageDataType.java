package inews.dataStructures;

import java.io.Serializable;

//TODO Remover métodos desnecessarios

/**
 * Enumerator with the types of not usual fields of the message
 * The not usual fields of a message are TYP, SEQUENCE and USER
 * @see Packet
 * @see MessageType
 * @author Luis Portela
 */
public enum MessageDataType implements Serializable {
	
	/**
	 * Number of news to send
	 */
	NUMBER(0),
	/**
	 * Date in the long format
	 */
	DATE(1),
	/**
	 * Title of the news
	 */
	TITLE(2),
	/**
	 * Port of the TCP server waiting for connection
	 */
	PORT(3),
	/**
	 * User to provide the score
	 */
	TARGET(4),
	/**
	 * Rank value to the user
	 */
	SCORE(5);
	
	private int type;
	
	/**
	 * Initializes a new type
	 * @param type number of the type
	 */
	private MessageDataType(int type) {
		this.type = type;
	}
	
	/**
	 * Returns the name of a type of the message
	 * @param index index on the MessageDataType Array
	 * @return 
	 */
	public static MessageDataType getTypeName(int index) {
		return MessageDataType.values()[index];
	}
	
	/**
	 * Returns the code of the type
	 * @return the code of the message
	 */
	public int getType (){
		return this.type;
	}
	
	/**
	 * Returns the name in a byte array
	 * @return 
	 */
	public byte[] getTypeByte() {
		return this.name().getBytes();
	}
	
	/**
	 * Returns the type that match the name provided
	 * @param name name of the type
	 * @return the type matching the provided name
	 */
	public MessageDataType getMessageType(String name) {
		return MessageDataType.valueOf(name);
	}
	
	/**
	 * Returns a string with the name
	 * @return the name of the type
	 */
	@Override
	public String toString() {
		return this.name();
	}
	
	/**
	 * Compares to types if there are equal
	 * @param type type to compare
	 * @return true if there are equal ; false otherwise
	 */
	public boolean equals(MessageDataType type) {
		return this.getType() == type.getType();
	}
}