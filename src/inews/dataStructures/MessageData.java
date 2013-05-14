package inews.dataStructures;

import java.io.Serializable;

/**
 * Class to keep the information of the not usual fields on the packets
 * The not usual fields are the fields different from TYP, USER and SEQUENCE
 * @author Luis Portela
 */
public class MessageData implements Serializable {
	
	private final MessageDataType key;
	private final String value;
	
	/**
	 * Initializes a new instance of this class with the provided information
	 * @param key type of the field to send in the packet
	 * @param value value of the field
	 * @see Packet
	 * @see MessageDataType
	 */
	public MessageData (MessageDataType key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the key of the message
	 * The key is the TYPE of the field
	 * @return the type of the field
	 * @see MessageDataType
	 */
	public MessageDataType getKey() {
		return key;
	}

	/**
	 * Returns the value of the field
	 * @return a string with the value
	 */
	public String getValue() {
		return value;
	}
}
