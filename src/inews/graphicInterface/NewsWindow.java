package inews.graphicInterface;

import inews.MyTimer;
import inews.client.UDPClientNews;
import inews.dataStructures.IPStruct;
import inews.dataStructures.UserStruct;
import inews.interfaces.INewsWindow;
import inews.interfaces.NewsWindowClient;
import inews.interfaces.UDPConnection;
import inews.server.TransmissionHandler;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * @author Jo�o Paulo Barraca <jpbarraca@ua.pt>
 * 
 * This class provides a basic interface for command input, and text output.
 */
public class NewsWindow extends JFrame implements INewsWindow, Runnable {
	
	private static final long serialVersionUID = 3421135597049702610L;
	public static final String DATE_FORMAT_NOW = "HH:mm:ss";
	public static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss";
	
	private UserStruct me;
	private IPStruct broadcast;
	
	private UDPClientNews clientNews = null;
	private TransmissionHandler udpConnector = null;
	
	private NavigableMap<String, UserStruct> ipUsers = null;
	
	private int userLogged = 0;
	
	private MyTimer timer = null;

	//The text output areas
	JTextArea outputArea;
	JTextArea newsArea;
	JTextArea logArea;

	//The text input areas
	JTextField inputArea;
	JLabel label;

	//Handles Window Events
	private WindowListener windowListener = new WindowListener() {

		public void windowActivated(WindowEvent e) {}

		/**
		 * Handles the event of the window becoming closed.
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
		 */
		public void windowClosed(WindowEvent e) {
			//Exit the application
			killTimer();
			if(clientNews != null)
				clientNews.windowClosed();
			
		}

		/**
		 * Handles the event of a user trying to close the window
		 * 
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			//Disposing the Window
			e.getWindow().dispose();
		}

		/**
		 * Handles the event of a user trying to deactivate the window
		 *
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowDeactivated(WindowEvent e) {
			
		}
		
		/**
		 * Handles the event of a user trying to de-minimize the window
		 * 
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowDeiconified(WindowEvent e) {
			
			
		}
		
		/**
		 * Handles the event of a user trying to minimize the window
		 *
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowIconified(WindowEvent e) {
			
		}

		/**
		 * Handles the event of a user starting the window
		 * 
		 * @param e The event to be processed
		 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowOpened(WindowEvent e) {
			if(clientNews != null)
				clientNews.windowOpened();
		}
		
	};
	
	//Handle Key Events
	private KeyListener keyListener = new KeyListener() {
		/**
		 * Handles the event of a keypress. If the key is an ENTER, the text is
		 * sent to the client and the input is cleared.
		 * 
		 * @param e The event to be processed
		 * 
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 0x0a) {
				if (inputArea.getText().length() > 0) {

					if (clientNews != null)
						clientNews.textAvailable(inputArea.getText() + '\n');

					inputArea.setText(new String());
				}
			}
		}

		/**
		 * Handles the event of a key being released after it was pressed.
		 * 
		 * @param e The event to be processed
		 * 
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
	
		}

		/**
		 * Handles the event of a key being provided after it was pressed.
		 * 
		 * @param e The event to be processed
		 * 
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			
		}
	};

	/**
	 * Calculates the local time
	 * 
	 * @return String with local time
	 */
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * Main Constructor for the ChatWindow class Accepts a ChatWindowClient as
	 * parameter
	 * 
	 * @param name The nick name to use
	 * @param cl The object instance of the class implementing the NewsWindowClient interface
	 */
	public NewsWindow(UserStruct me, IPStruct broadcast) {
		
		/* inicializacoes parametros entrada */
		this.me = me;
		this.broadcast = broadcast;
		
		initializeComunication();
		initializeComponents();
		
		timer = MyTimer.getInstance((INewsWindow)this, (NewsWindowClient)clientNews);
	}
	
	/**
	 * Initialize all the NewsWindow components
	 */
	private void initializeComponents() {
		
		label = new JLabel("Nick: " + me.getName());

		outputArea = new JTextArea();
		newsArea = new JTextArea();
		logArea = new JTextArea();

		inputArea = new JTextField();

		getContentPane().add(label, BorderLayout.NORTH);

		outputArea.setEditable(false);
		outputArea.setRows(20);
		outputArea.setColumns(80);
		outputArea.setFont(new Font("Courier", 12, 12));
		
		JScrollPane textScroll = new JScrollPane(outputArea);
		textScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		logArea.setEditable(false);
		logArea.setRows(5);
		logArea.setColumns(40);
		logArea.setFont(new Font("Courier", 10, 10));

		
		newsArea.setEditable(false);
		newsArea.setRows(30);
		newsArea.setColumns(40);
		newsArea.setFont(new Font("Courier", 12, 12));
		
		JScrollPane newsScroll = new JScrollPane(newsArea);
		newsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		newsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Add a text input area
		inputArea.setEditable(true);
		inputArea.setColumns(40);
		inputArea.addKeyListener(keyListener);
		inputArea.setEnabled(false);
		
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		JPanel westPane = new JPanel();
		JPanel eastPane = new JPanel();
		
		JSplitPane westSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroll, logScroll);
		westSplit.setDividerLocation(0.75);
		westPane.setLayout(new BorderLayout());
		eastPane.setLayout(new BorderLayout()); 
		
		westPane.add(inputArea, BorderLayout.NORTH);
		westPane.add(westSplit, BorderLayout.CENTER);
		
		eastPane.add(newsScroll);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPane, eastPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.50);
		
		getContentPane().add(splitPane,BorderLayout.CENTER);
		
		addWindowListener(windowListener);
		
		// Pack and set visible
		pack();
	}
	
	/**
	 *  Sets the current nick to the string provided
	 * @param nick The nick name to use
	 */
	@Override
	public void setNick(String nick) {
		label.setText("Nick: " + nick);
	}

	/**
	 * Writes a String to the Output Text Area
	 * @param text The text String to be written
	 */
	@Override
	public void appendTextToOutputArea(String text) {

		outputArea.append(text);
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}
	
	/**
	 * Writes two Strings containing a command and its result to the Output Text Area
	 * 
	 * @param command The command entered by the user
	 * @param result The output of the command entered
	 */
	@Override
	public void appendCommandResultToOutputArea(String command, String result) {

		outputArea.append("\nCommand: "+command+"\n"+result+"\n");
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}

	/**
	 * Writes a String to the Log Text Area
	 * @param text The text String to be written
	 */
	@Override
	public void appendTextToLogArea(String text) {
		logArea.append(now()+" : "+text+"\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
	
	/**
	 * Writes a String to the News Text Area
	 * @param text The text String to be written
	 */
	@Override
	public void appendTextToNewsArea(String text) {
		newsArea.append(now()+" : "+text+"\n\n");
		newsArea.setCaretPosition(newsArea.getDocument().getLength());
	}
	
	/**
	 * Writes a news title and its content to the News Text Area
	 * 
	 * @param title The news title
	 * @param content The news content
	 */
	@Override
	public void appendNewsToNewsArea(String title, String content) {

		newsArea.append("Title: "+title+"\n");
		newsArea.append(content+"\n\n");

		newsArea.setCaretPosition(newsArea.getDocument().getLength());
	}
	
	/**
	 * Writes a news title, its content and its creator to the News Text Area
	 * 
	 * @param user The User which published the news
	 * @param title The news title
	 * @param content The news content
	 */
	@Override
	public void appendUserNewsToNewsArea(String user, String title, String content) {

		newsArea.append(user+"\nTitle: "+title+"\n");
		newsArea.append(content+"\n\n");
		newsArea.setCaretPosition(newsArea.getDocument().getLength());
	}

	/**
	 * Clears the Output Text Area 
	 */
	@Override
	public void clearOutputArea() {
		outputArea.setText(new String());
	}

	/**
	 * Clears the Input Text Area 
	 */
	@Override
	public void clearInputArea(){
		inputArea.setText(new String());
	}
	
	/**
	 * Clears the Log Text Area 
	 */
	@Override
	public void clearLogArea(){
		logArea.setText(new String());
	}


	/**
	 * Clears the News Text Area 
	 */
	@Override
	public void clearNewsArea(){
		newsArea.setText(new String());
	}

	/**
	 *  Sets the value of the ChatWindowClient
	 *  The object will be used to dispatch events
	 *  
	 * @param cl The object instance of the class implementing the NewsWindowClient interface
	 */
	public void setClient(UDPClientNews cl) {
		clientNews = cl;
	}

	// Minhas funções
	
	/**
	 * Initialize the classes needed for comunication
	 * @see UDPClientNews
	 * @see TransmissionHandler
	 */
	private void initializeComunication() {

		// Retrieves the instance for the NewsWindowClient
		this.clientNews = UDPClientNews.getInstance((UDPConnection)this.udpConnector, me, broadcast, (NewsWindow)this);
		// Retrieves the instance for the UDP Communication Handler
		this.udpConnector = TransmissionHandler.getInstance(me, (INewsWindow) this, (NewsWindowClient) this.clientNews);
		// Sets the udp Connector on the NewsWindowClient
		this.clientNews.setClientSocket(((UDPConnection)udpConnector));
		
		// Starts the thread that handles the UDP connection
		this.udpConnector.start();
	}
	
	/**
	 * Read the local user news directory
	 * @return 1 if it successfully reads the directory
	 */
	@Override
	public int readNewsDir() {
		
		File dir = new File("_news");
		if (dir == null)
			return 0;
		
		FilenameFilter filtro = new FilenameFilter() {

			@Override
			public boolean accept(File file, String string) {
				return ((!string.startsWith(".")) && string.toLowerCase().endsWith("txt"));
			}
		};
		
		File[] listOfFiles = dir.listFiles(filtro);
		int finalIdxName;
		
		for (File newsFile : listOfFiles) {
			System.out.println("Nome Noticia: "+newsFile.getName());
			finalIdxName = newsFile.getName().lastIndexOf(".");
			finalIdxName = finalIdxName != -1 ? finalIdxName : newsFile.getName().length();
			this.me.addNewToList(Long.parseLong(newsFile.getName().substring(0, finalIdxName)), newsFile.getName());
		}
		
		return 1;
	}
	
	/**
	 * Send all the initial packets to the network and all the lists needed and the timer if the user successfully logins
	 * @see UDPClientNews
	 */
	private synchronized void startApplication() {
		
		this.clientNews.applicationStarted();
		
		if (this.readNewsDir() != 1) {
			this.closeApplication("Não foi possivel ler a pasta de noticias", -2);
		}
		
		while (this.userLogged == 0) {
			try {
				wait();
			} catch (InterruptedException ex) {
				System.out.println("Wait enterrompido");
				userLogged = -1;
			}
		}
		
		if (userLogged == 1) {
			this.ipUsers = new TreeMap<String, UserStruct>();
			this.setNick(this.me.getName().toString());
			this.setVisible(true);
			this.requestFocus();
			this.inputArea.requestFocus();
			this.timer.start();
		}
		else this.closeApplication("Utilizador Duplicado", -2);
	}
	
	/**
	 * Sets the user as successfully logged in and notify the method waiting
	 * It will initiate the program
	 * @see NewsWindow#startApplication() 
	 */
	@Override
	public synchronized void successfulLogin() {
		this.userLogged = 1;
		notify();
	}
	
	/**
	 * Sets the user as not logged in and notify the method waiting
	 * It will terminate the program
	 * @see NewsWindow#startApplication() 
	 */
	@Override
	public synchronized void failedLogin() {
		this.userLogged = -1;
		notify();
	}
	
	/**
	 * Enable the application to be used by the user after sending the active and ping packets
	 */
	@Override
	public void enableApplication() {
		inputArea.setEnabled(true);
		appendTextToLogArea("Application input enabled!");
		appendTextToNewsArea("Application Started!");
		appendTextToOutputArea("Application Started");
	}
	
	/**
	 * Terminates the timer, the udp connection and close/finalizes the application
	 * @param message message to print when the application closes
	 * @param exitValue cause
	 */
	@Override
	public void closeApplication(String message, int exitValue) {
		
		System.out.println("[NewsWindow]:[CloseApplication] -> " + message + "!");
		
		this.setVisible(false);
		this.clientNews = null;
		this.udpConnector = null;
		this.me = null;
		this.killTimer();
		try {
			this.finalize();
		} catch (Throwable ex) {
			System.out.println("[NewsWindow]:[CloseApplication] -> Nao foi possivel finalizar as variaveis!");
		}
		System.exit(exitValue);
	}
	
	/**
	 * Kill the timer
	 * @see MyTimer
	 */
	private void killTimer() {
		this.timer.endTimer();
	}
	
	/**
	 * Start the application
	 */
	@Override
	public void run(){
		startApplication();
	}
	
	/**
	 * Print the users list to the window
	 */
	@Override
	public void refreshUsersList() {
		
		UserStruct tmpUser;
		Iterator<UserStruct> usersList = this.getUserListIterator();
		this.clearNewsArea();
		
		while (usersList.hasNext()) {
			tmpUser = usersList.next();
			this.printUserInfo(tmpUser);
		}
	}
	
	/**
	 * Prints the user info by its IP
	 * @param ip ip of the user to print
	 * @see IPStruct
	 */
	@Override
	public void printUserInfoByIP(IPStruct ip) {
		
		UserStruct tmpUserStruct = this.getUserByIP(ip);
		
		if (tmpUserStruct != null) {
			this.printUserInfo(tmpUserStruct);
			tmpUserStruct.updateLastConn();
		}
	}
	
	/**
	 * Prints all the user info by name
	 * @param user name of user to print
	 * @see UserStruct
	 */
	private void printUserInfo(UserStruct user) {
		
		if (user != null) {
			
			this.appendTextToLogArea("Utilizador Actualizado: " + user.toString());
			this.appendTextToNewsArea("Utilizador: " + user.getName()+
					"\nTota Noticias: " + user.getTotalNews() + 
					"\nData Ultima: " + formatDate(user.getLastNewDate()));
		}
	}
	
	/**
	 * Formates the date provided by a long value to a string with DATE_FORMAT
	 * @param unixTimeStamp long value to convert in date
	 * @return formated String with the date
	 * @see NewsWindow#DATE_FORMAT
	 */
	private String formatDate(Long unixTimeStamp) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		return dateFormat.format(new Date(unixTimeStamp * 1000));
	}

	// Metodos relacionados com operacoes na lista de utilizadores
	
	/**
	 * Add a user with a nick to the usersList
	 * @param nick nick of the user its used as the identifier on the users list
	 * @param user User Struct to add
	 * @return the User Struct added or a existing User Struct updated
	 * @see UserStruct
	 */
	@Override
	public synchronized UserStruct addToList(String nick, UserStruct user) {
		
		try {
			if (!this.containsKey(nick)) {

				this.ipUsers.put(nick, user);
				this.appendTextToLogArea("Novo utilizador adicionado -> Utilizador: "+user.toString());			
			}
			else {
				this.appendTextToLogArea("Devolvido Utilizador ja existente");
			}
		}
		catch (NullPointerException ex) {
			return null;
		}
		
		this.refreshUsersList();
		
		return this.getUserFromName(nick);
	}
	
	/**
	 * Removes the user with the name provided from the users list and refresh the users list on the news window
	 * @param nick name or nick of the user to remove
	 */
	@Override
	public synchronized void removeFromListByName(String nick) {
		try {
			this.ipUsers.remove(nick);
			System.out.println("Removido Utilizador: " + nick);
			this.appendTextToLogArea("Removido Utilizador: " + nick);
		}
		catch (NullPointerException ex) {
			this.appendTextToLogArea("Falha a remover o utilizador: " + nick);
		}
		
		this.refreshUsersList();
	}
	
	/**
	 * Removes the user with the IP provided
	 * @param ip machine identification of the user
	 * @see IPStruct
	 */
	@Override
	public synchronized void removeFromListByIP(IPStruct ip) {
		
		try {
			UserStruct tmpUser = this.getUserByIP(ip);

			if (tmpUser != null) {
				this.ipUsers.remove(tmpUser.getName());
			}
		}
		catch (NullPointerException ex) {
			this.appendTextToLogArea("Falha a remover o IP: " + ip);
		}
		
		this.refreshUsersList();
	}
	
	/**
	 * Returns the user struct specified with the name provided
	 * @param nick name of the user to return the user struct
	 * @return the user struct if exists or null if it not exists
	 */
	@Override
	public synchronized UserStruct getUserFromName(String nick) {
		UserStruct tmpUser = this.ipUsers.get(nick);
		if (tmpUser != null) {
			tmpUser.updateLastConn();
		}
		return tmpUser;
	}
	
	/**
	 * Returns the user struct using the IP provided
	 * @param ip machine identification of the user
	 * @return the user struct if IP its been used by any user or null otherwise
	 */
	@Override
	public synchronized UserStruct getUserByIP(IPStruct ip) {
		
		UserStruct tmpUser = null;
		boolean haveUsers = true;
		Iterator<UserStruct> usersList = this.getUserListIterator();
		
		while (haveUsers && usersList.hasNext()) {
			try {
				tmpUser = usersList.next();
				
				if (tmpUser.getIp().equals(ip)) {
					haveUsers = false;
					tmpUser.updateLastConn();
				}
			}
			catch (NoSuchElementException ex) {
				haveUsers = false;
				tmpUser = null;
			}
		}
		return tmpUser;
	}
	
	/**
	 * Returns an iterator to the users list
	 * @return iterator to the users list
	 * @see Iterator
	 * @see UserStruct
	 */
	@Override
	public Iterator<UserStruct> getUserListIterator() {
		try {
			return this.ipUsers.values().iterator();
		}
		catch(NullPointerException ex) {
			return null;
		}
	}
	
	/**
	 * Search the users list from an user with the specified IP
	 * @param ip machine identification of the user
	 * @return true if the user exists; false if don't
	 */
	@Override
	public synchronized boolean containsKey(IPStruct ip) {
		return this.getUserByIP(ip) == null ? false : true;
	}
	
	/**
	 * Search the users list if contains a user with the key name
	 * @param name name of the user to search
	 * @return true if the user exists; false if don't
	 * @see UserStruct
	 */
	@Override
	public synchronized boolean containsKey(String name) {
		try {
			return this.ipUsers.containsKey(name);
		}
		catch(NullPointerException ex) {
			return false;
		}
	}
	
	/**
	 * Search the users list for an UserStruct equals to the provided
	 * @param user user struct to search
	 * @return true if the user exists; false if don't
	 * @see UserStruct
	 */
	@Override
	public synchronized boolean containsValue(UserStruct user) {
		Object tmpUsers[] = this.ipUsers.values().toArray();
		for (int i = 0; i < tmpUsers.length; i++) {
			if (((UserStruct)tmpUsers[i]).equals(user))
				return true;
		}
		return false;
	}
	
	/**
	 * Clear the user list
	 */
	@Override
	public void clearUsersList() {
		this.ipUsers = new TreeMap<String, UserStruct>();
		this.refreshUsersList();
	}
	
	/**
	 * Returns the number of users connected
	 * @return int - number of users connected
	 */
	@Override
	public synchronized int getUsersCount() {
		try {
			return this.ipUsers.size();
		}
		catch (NullPointerException ex) {
			return 0;
		}
	}

	/**
	 * Returns my user struct
	 * @return the user struct for the local user
	 * @see UserStruct
	 */
	@Override
	public UserStruct getMe() {
		return this.me;
	}
}