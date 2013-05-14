package inews;

import inews.dataStructures.UserStruct;
import inews.interfaces.INewsWindow;
import inews.interfaces.NewsWindowClient;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class simulates a timer
 * @author Luis Portela
 * @see INewsWindow
 * @see NewsWindowClient
 */
public class MyTimer extends Thread {
	
	private static MyTimer mySelf = null;
	
	private INewsWindow newsWindow = null;
	private NewsWindowClient newsClient = null;
	
	private boolean running = true;
	
	/**
	 * Initializes a new timer with the provided information
	 * @param newsWindow instance of the news window interface
	 * @param newsClient instance of the news window client
	 * @see INewsWindow
	 * @see NewsWindowClient
	 */
	private MyTimer(INewsWindow newsWindow, NewsWindowClient newsClient) {
		
		this.newsWindow = (INewsWindow)newsWindow;
		this.newsClient = (NewsWindowClient)newsClient;
	}
	
	/**
	 * Returns the instance of this class
	 * If there is any instance of this class returns if otherwise creates a one
	 * @param newsWindow instance of the news window interface
	 * @param newsClient instance of the news window client
	 * @return the unique instance of this class
	 * @see INewsWindow
	 * @see NewsWindowClient
	 */
	public static MyTimer getInstance(INewsWindow newsWindow, NewsWindowClient newsClient) {
		if (mySelf == null)
			mySelf = new MyTimer((INewsWindow)newsWindow, (NewsWindowClient)newsClient);
		
		return mySelf;
	}

	/**
	 * Running timer
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(60000);
			
			while (running) {
				try {
					this.logMessage("run()", "Vou Verificar noticias!");
					
					// checks the news dir for changes
					this.checkNewsDir();
					// sleeps 30 seconds
					Thread.sleep(30000);
					
					if (!running) break;
					
					this.logMessage("run()", "Vou Verificar Ultimas Ligacoes dos Clientes!");
					// checks the last connection for the users
					this.checkUserLastConn();
					// sleep for 20 seconds
					Thread.sleep(20000);
					
				} catch (InterruptedException ex) {
					this.logMessage("run()", "Thread interrompida");
					Logger.getLogger(MyTimer.class.getName()).log(Level.SEVERE, null, ex);
				}
				
			}
			this.logMessage("run()", "Timer Terminado!");
		} catch (InterruptedException ex) {
			this.logMessage("run()", "Thread interrompida");
		}
		
	}
	
	/**
	 * Checks if all the users have connected in the last 60 seconds and if don't sends PING packets
	 */
	private void checkUserLastConn() {
		
		Iterator<UserStruct> usersList = this.newsWindow.getUserListIterator();
		UserStruct pingUser;
		
		while (usersList.hasNext()) {
			
			pingUser = usersList.next();
			
			if ((System.currentTimeMillis() - pingUser.getLastConn()) > 60000) {
				this.newsClient.pingUser(pingUser.getIp());
			}
		}
	}
	
	/**
	 * Checks the user news directory for changes
	 * If the number of files is different sends the news list
	 * If are different names sends the news list
	 * If 
	 */
	private synchronized void checkNewsDir() {
		
		UserStruct tmpUserStruct;
		int sendPacket = 0;
		
		File dir = new File("_news");
		if (dir == null)
			return ;
		
		FilenameFilter filtro = new FilenameFilter() {

			@Override
			public boolean accept(File file, String string) {
				return ((!string.startsWith(".")) && string.toLowerCase().endsWith("txt"));
			}
		};
		
		this.logMessage("checkNewsDir", "Directorio lido com sucesso!");
		
		File[] listOfFiles = dir.listFiles(filtro);
		tmpUserStruct = this.newsWindow.getMe();
		
		if (tmpUserStruct.getTotalNews() != listOfFiles.length) {
			this.logMessage("checkNewsDir", "Conteudo directorio diferente ler novamente!");
			sendPacket = this.readNewsDir(listOfFiles, 0);
		}
		else {
			this.logMessage("checkNewsDir", "Conteudo directorio parece igual mas vou ler novamente!");
			sendPacket = this.readNewsDir(listOfFiles, 1);
		}
		
		if (sendPacket == 1) {
			this.logMessage("checkNewsDir", "A enviar a nova informacao!");
			this.newsClient.sendMyInfo();	
		}
		
	}
	
	/**
	 * Reads the news directory
	 * @param listOfFiles list of files
	 * @param check what is needed to check
	 * @return if the news directory is different
	 */
	private int readNewsDir(File[] listOfFiles, int check) {
		
		UserStruct tmpUser;
		NavigableMap<Long, String> userNewsList, tmpList;
		Long unixTimeStamp;
		int differentValues = 0;

		tmpUser = this.newsWindow.getMe();
		int finalIdxName;
		
		if (check == 1) {
			userNewsList = tmpUser.getNewsList();
			tmpList = new TreeMap<Long, String>();

			for (File newsFile : listOfFiles) {
				System.out.println("Nome Noticia: "+newsFile.getName());
				finalIdxName = newsFile.getName().lastIndexOf(".");
				finalIdxName = finalIdxName != -1 ? finalIdxName : newsFile.getName().length();
				unixTimeStamp = Long.parseLong(newsFile.getName().substring(0, finalIdxName));
				tmpList.put(unixTimeStamp, newsFile.getName());
				if ((differentValues == 0) && (!userNewsList.containsKey(unixTimeStamp))) {
					this.logMessage("checkNewsDir", "Conteudo directorio diferente!");
					differentValues = 1;
					tmpUser.setNewsList(tmpList);
				}
			}
//			if (differentValues == 1)
//				tmpUser.setNewsList(tmpList);
			
		}
		else {
			tmpUser.clearNewsList();
			for (File newsFile : listOfFiles) {
				System.out.println("Nome Noticia: "+newsFile.getName());
				finalIdxName = newsFile.getName().lastIndexOf(".");
				finalIdxName = finalIdxName != -1 ? finalIdxName : newsFile.getName().length();
				tmpUser.addNewToList(Long.parseLong(newsFile.getName().substring(0, finalIdxName)), newsFile.getName());
				
				differentValues = 1;
			}
		}
		return differentValues;
	}
	
	/**
	 * Prints the message to the system output and to the news window log area
	 * @param func
	 * @param message 
	 */
	private void logMessage(String func, String message) {
		this.newsWindow.appendTextToLogArea("[MyTimer]:["+func+"] -> " + message);
		System.out.println("[MyTimer]:["+func+"] -> " + message);
	}
	
	/**
	 * Finalizes the timer class
	 */
	public void endTimer() {
		try {
			this.running = false;
			this.finalize();
		} catch (Throwable ex) {
			Logger.getLogger(MyTimer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
