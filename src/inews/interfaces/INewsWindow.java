package inews.interfaces;

import inews.dataStructures.IPStruct;
import inews.dataStructures.UserStruct;
import java.util.Iterator;

/**
 *
 * @author Luis Portela
 */
public interface INewsWindow {
	
	/**
	 *  Sets the current nick to the string provided
	 * @param nick The nick name to use
	 */
	public void setNick(String nick);
	
	/**
	 * Writes a String to the Output Text Area
	 * @param text The text String to be written
	 */
	public void appendTextToOutputArea(String text);
	
	/**
	 * Writes two Strings containing a command and its result to the Output Text Area
	 * 
	 * @param command The command entered by the user
	 * @param result The output of the command entered
	 */
	public void appendCommandResultToOutputArea(String command, String result);

	/**
	 * Writes a String to the Log Text Area
	 * @param text The text String to be written
	 */
	public void appendTextToLogArea(String text);
	
	/**
	 * Writes a String to the News Text Area
	 * @param text The text String to be written
	 */
	public void appendTextToNewsArea(String text);
	
	/**
	 * Writes a news title and its content to the News Text Area
	 * 
	 * @param title The news title
	 * @param content The news content
	 */
	public void appendNewsToNewsArea(String title, String content);
	
	/**
	 * Writes a news title, its content and its creator to the News Text Area
	 * 
	 * @param user The User which published the news
	 * @param title The news title
	 * @param content The news content
	 */
	public void appendUserNewsToNewsArea(String user, String title, String content);

	/**
	 * Clears the Output Text Area 
	 */
	public void clearOutputArea();

	/**
	 * Clears the Input Text Area 
	 */
	public void clearInputArea();
	
	/**
	 * Clears the Log Text Area 
	 */
	public void clearLogArea();

	/**
	 * Clears the News Text Area 
	 */
	public void clearNewsArea();
	
	/**
	 * Sets the user as successfully logged in and notify the method waiting
	 * It will initiate the program
	 * @see NewsWindow#startApplication() 
	 */
	public void successfulLogin();
	
	/**
	 * Sets the user as not logged in and notify the method waiting
	 * It will terminate the program
	 * @see NewsWindow#startApplication() 
	 */
	public void failedLogin();
	
	/**
	 * Add a user with a nick to the usersList
	 * @param nick nick of the user its used as the identifier on the users list
	 * @param user User Struct to add
	 * @return the User Struct added or a existing User Struct updated
	 * @see UserStruct
	 */
	public UserStruct addToList(String nick, UserStruct user);
	
	/**
	 * Removes the user with the name provided from the users list and refresh the users list on the news window
	 * @param nick name or nick of the user to remove
	 */
	public void removeFromListByName(String nick);
	
	/**
	 * Removes the user with the IP provided
	 * @param ip machine identification of the user
	 * @see IPStruct
	 */
	public void removeFromListByIP(IPStruct ip);
	
	/**
	 * Returns the user struct specified with the name provided
	 * @param nick name of the user to return the user struct
	 * @return the user struct if exists or null if it not exists
	 */
	public UserStruct getUserFromName(String nick);
	
	/**
	 * Returns the user struct using the IP provided
	 * @param ip machine identification of the user
	 * @return the user struct if IP its been used by any user or null otherwise
	 */
	public UserStruct getUserByIP(IPStruct ip);
	
	/**
	 * Returns an iterator to the users list
	 * @return iterator to the users list
	 * @see Iterator
	 * @see UserStruct
	 */
	public Iterator<UserStruct> getUserListIterator();
	
	/**
	 * Returns the number of users connected
	 * @return int - number of users connected
	 */
	public int getUsersCount();
	
	/**
	 * Search the users list from an user with the specified IP
	 * @param ip machine identification of the user
	 * @return true if the user exists; false if don't
	 */
	public boolean containsKey(IPStruct ip);
	
	/**
	 * Search the users list if contains a user with the key name
	 * @param name name of the user to search
	 * @return true if the user exists; false if don't
	 * @see UserStruct
	 */
	public boolean containsKey(String name);
	
	/**
	 * Search the users list for an UserStruct equals to the provided
	 * @param user user struct to search
	 * @return true if the user exists; false if don't
	 * @see UserStruct
	 */
	public boolean containsValue(UserStruct user);
	
	/**
	 * Clear the user list
	 */
	public void clearUsersList();
	
	/**
	 * Enable the application to be used by the user after sending the active and ping packets
	 */
	public void enableApplication();
	
	/**
	 * Terminates the timer, the udp connection and close/finalizes the application
	 * @param message message to print when the application closes
	 * @param exitValue cause
	 */
	public void closeApplication(String message, int exitValue);
	
	/**
	 * Prints the user info by its IP
	 * @param ip ip of the user to print
	 * @see IPStruct
	 */
	public void printUserInfoByIP(IPStruct ip);
	
	/**
	 * Read the local user news directory
	 * @return 1 if it successfully reads the directory
	 */
	public int readNewsDir();
	
	/**
	 * Returns my user struct
	 * @return the user struct for the local user
	 * @see UserStruct
	 */
	public UserStruct getMe();
	
	/**
	 * Print the users list to the window
	 */
	public void refreshUsersList();
}