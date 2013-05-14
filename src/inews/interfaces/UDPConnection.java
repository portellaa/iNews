package inews.interfaces;

import inews.dataStructures.AckMessage;
import inews.dataStructures.Packet;

/**
 * Interface with the methods to send messages to the network
 * Check for acknowledges
 * @author Luis Portela
 */
public interface UDPConnection {
	
	/**
	 * Sends packets to the network
	 * @param sendPacket packet to send
	 * @see Packet
	 */
	public void send(Packet sendPacket);
	
	/**
	 * Add to the acknowledge message list the received message and creates a thread to process it
	 * @param sendPacket message to be sent and wait for acknowledge or to be retransmited
	 * @param retries number of times that the message should be send if not ack is received
	 * @param timeout time between retries
	 * @see Packet
	 */
	public void sendWithAck(Packet sendPacket, int retries, int timeout);
	
	/**
	 * Allows to change the running status
	 * @param running new thread status
	 */
	public void setRunning(boolean running);
	
	/**
	 * Returns the message in the list with the provided sequence number
	 * @param sequence sequence number to search
	 * @return message if it exists
	 * @see AckMessage
	 */
	public AckMessage getAck(int sequence);
	
	/**
	 * Sets the message on the messages list with the provided sequence number as received
	 * @param sequence sequence number of the message to mark as received
	 */
	public void receivedAck(int sequence);
	
	/**
	 * Remove a mensagem da lista de mensagens com acknowledge com o sequence number
	 * @param sequence sequence number da mensagem a retirar da lista
	 */
	public void removeAck(int sequence);

	/**
	 * Check if the program is still running
	 *
	 * @return true if is alive; false otherwise
	 */
	public boolean isRunning();
}
