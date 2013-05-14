package inews.dataStructures;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Class with the information of a user
 * @see Comparable
 * @author Luis Portela
 */
public final class UserStruct implements Comparable<UserStruct>, Serializable {
	
	private String name;
	private IPStruct ip;
	private int totalNews;
	private long lastNewDate;
	
	private int rank = 0;
	private long lastConn = 0;
	
	private NavigableMap<Long, String> newsList = null;
	
	/**
	 * Initializes a new user with the name and IP provided
	 * @param name name of the new user
	 * @param ip IP of the machine
	 * @see IPStruct
	 */
	public UserStruct(String name, IPStruct ip) {
		this.name = name;
		this.ip = new IPStruct(ip);
		this.totalNews = 0;
		this.lastNewDate = 0;
		this.newsList = new TreeMap<Long, String>();
		this.rank = 0;
		this.lastConn = System.currentTimeMillis();
	}
	
	/**
	 * Initializes a new user with the information of other user struct
	 * @param user user struct with the information
	 */
	public UserStruct(UserStruct user) {
		this(user.getName(), user.getIp());
		this.totalNews = user.getTotalNews();
		this.lastNewDate = user.getLastNewDate();
		this.rank = user.getRank();
		this.newsList = new TreeMap<Long, String>(this.getNewsList());
	}

	/**
	 * Returns the user machine IP
	 * @return user machine IP
	 * @see IPStruct
	 */
	public IPStruct getIp() {
		return ip;
	}

	/**
	 * Defines a new IP to the user
	 * @param ip new IP to the user
	 * @see IPStruct
	 */
	public void setIp(IPStruct ip) {
		this.ip = ip;
	}

	/**
	 * Returns the name
	 * @return name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Defines a new name to the user
	 * @param name 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the total news of this user
	 * @return total news of the user
	 */
	public int getTotalNews() {
		return totalNews;
	}

	/**
	 * Defines the number of news to the user
	 * @param totalNews new number of news
	 */
	public void setTotalNews(int totalNews) {
		this.totalNews = totalNews;
	}
	
	/**
	 * Checks if the user name is valid
	 * @return true if valid ; false otherwise
	 */
	public boolean isValid() {
		return !this.getName().equalsIgnoreCase("");
	}

	/**
	 * Returns the last news date as long
	 * @return date as long
	 * @see Long
	 */
	public long getLastNewDate() {
		return lastNewDate;
	}

	/**
	 * Defines the new last news date
	 * @param lastNewDate last news date as long
	 */
	public void setLastNewDate(long lastNewDate) {
		this.lastNewDate = lastNewDate;
	}
	
	/**
	 * Add a new to the news list of the user
	 * @param time time of news as long
	 * @param newsFile name of the newsFile
	 */
	public void addNewToList(long time, String newsFile){
		
		this.newsList.put(time, newsFile);
		this.totalNews++;
		if (time > this.lastNewDate)
			this.lastNewDate = time;
	}

	/**
	 * Returns the rank of the user
	 * @return value of the rank
	 */
	public int getRank() {
		return this.rank;
	}

	/**
	 * Defines a new rank to the user
	 * @param rank new rank value
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Returns the user news list
	 * @return user news list
	 */
	public NavigableMap<Long, String> getNewsList() {
		return newsList;
	}

	/**
	 * Sets a new news list to the user
	 * @param newsList new news list
	 */
	public void setNewsList(NavigableMap<Long, String> newsList) {
		this.newsList = newsList;
	}
	
	/**
	 * Clear the user news list, the value of total news and the last news date
	 */
	public void clearNewsList(){
		this.newsList = new TreeMap<Long, String>();
		this.totalNews = 0;
		this.lastNewDate = 0;
	}

	/**
	 * Returns the last user connection received
	 * @return value of last connection
	 */
	public long getLastConn() {
		return lastConn;
	}
	
	/**
	 * Updates the last connection of the user
	 */
	public void updateLastConn() {
		this.lastConn = System.currentTimeMillis();
	}
	
	/**
	 * Formats the output of the user information with the provided separator
	 * @param separator separator of the user information
	 * @return user information in a string
	 */
	public String toString(String separator) {
		
		return this.getName() + separator + separator + this.getIp() + separator + separator + this.getTotalNews() + separator + separator
				+ new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new java.util.Date(this.getLastNewDate() * 1000));
	}
	
	/**
	 * User information in a string
	 * @return String with the user information
	 */
	@Override
	public String toString(){
		return "Nome: " + this.getName() + " - "
				+"Num. Noticias: " +this.getTotalNews() + " - "
				+"Data Ultima Noticia: " + new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new java.util.Date(this.getLastNewDate() * 1000)) + " - "
				+ "IP: " + this.getIp();
	}

//	@Override
//	public int compareTo(UserStruct t) {
//		return (int)Math.round((this.getIp().compareTo(t.getIp()) + this.getName().compareTo(t.getName()))/2.0f);
//	}
	
	/**
	 * Compares two users
	 * @param t other user to compare
	 * @return int
	 */
	@Override
	public int compareTo(UserStruct t) {
		return this.getName().compareTo(t.getName());
	}
	
	/**
	 * Compares two users
	 * @param t user to compare
	 * @return true if equal ; false otherwise
	 */
	public boolean equals(UserStruct t) {
		return (this.compareTo(t) == 0) ? true : false;
	}
}