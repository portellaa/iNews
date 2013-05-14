package inews.dataStructures;

/**
 * Class with the information of a message that needs to be retransmited and/or acknowledge
 * @author Luis Portela
 * @see Packet
 */
public class AckMessage {
	
	private final int sequenceNumber;
	private final int timeout;
	private final Packet sendPacket;
	
	private int retries;
	private boolean ackReceived;
	
	/**
	 * Initializes this class with the provided values
	 * @param sequenceNumber sequence number of the message to be sent
	 * @param retries number of retries to send the message
	 * @param timeout timeout between the messages
	 * @param sendPacket packet to send
	 * @see Packet
	 */
	public AckMessage(int sequenceNumber, int retries, int timeout, Packet sendPacket) {
		
		this.retries = retries;
		this.sequenceNumber = sequenceNumber;
		this.timeout = timeout;
		this.ackReceived = false;
		this.sendPacket = sendPacket;
	}

	/**
	 * Checks if the acknowledge as been received
	 * @return true if the acknowledge as received ; false otherwise
	 */
	public boolean isAckReceived() {
		return ackReceived;
	}

	/**
	 * Returns the number of retries of this acknowledge message
	 * @return number of retries
	 */
	public int getRetries() {
		
		int tmpRetries = this.retries;
		
		this.retries = tmpRetries != 0 ? (tmpRetries) - 1 : 0;
		
		return tmpRetries;
	}

	/**
	 * Returns the sequence number
	 * @return sequence number
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Returns the timeout between the messages
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Returns the packet data of this message
	 * @return packet data
	 * @see Packet
	 */
	public Packet getSendPacket() {
		return sendPacket;
	}
	
	/**
	 * Set this message as have received the acknowledge
	 */
	public void ackReceived() {
		this.ackReceived = true;
	}
}
