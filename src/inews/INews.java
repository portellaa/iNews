package inews;

import inews.dataStructures.IPStruct;
import inews.dataStructures.UserStruct;
import inews.graphicInterface.NewsWindow;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author meligaletiko
 */
public class INews {

	/**
	 * 
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		
		UserStruct me = null;
		IPStruct ip, broadcast = null;
		
		Scanner sc = new Scanner(System.in);
		
		NewsWindow newsWindows = null;
		
		if (args.length > 0) {
			try {
				ip = new IPStruct(Inet4Address.getLocalHost().getHostAddress().toString(), Integer.parseInt(args[1]));
				me = new UserStruct(args[0], ip);
				
				System.out.println("Eu: " + me);
				System.out.println("Meu IP: " + ip);
				
				String tmp, regex = "";
				
				System.out.println("\nIp(" + ip + ") correcto?");
				System.out.println("\nCaso não seja escolha uma das opções em baixo.");
				
				InetAddress[] allInterfaces = InetAddress.getAllByName(Inet4Address.getLocalHost().getCanonicalHostName());
				
				for (int i = 0; i < allInterfaces.length; i++) {
					if (allInterfaces[i].getHostAddress().matches("([0-9]{1,3}.){3}[0-9]{1,3}")) {
						System.out.println("[" + i + "] : " + allInterfaces[i].getHostAddress());
						regex += i + "|";
					}
				}
				
				do {
					tmp = sc.nextLine();
				} while (((tmp.length() > 1) && (!tmp.matches(regex))) && (!tmp.matches("([0-9]{1,3}.){3}[0-9]{1,3}")));
				
				if (tmp.length() > 1) {
					try {
						ip = new IPStruct(tmp);
						ip.setPort(Integer.parseInt(args[1]));
					} catch (NumberFormatException ex) {
						System.out.println("Introduza um IP valido!");
					}
				}
				else if (tmp.length() > 0) {
					ip = new IPStruct(allInterfaces[Integer.valueOf(tmp)].getHostAddress());
				}
				
				me.setIp(ip);
				broadcast = new IPStruct("255.255.255.255", ip.getPort());
				
				System.out.println("Eu: " + me);
				System.out.println("Meu IP: " + ip);
				
				if (args.length > 2) {
					broadcast = new IPStruct(args[2]);
				}
				
				newsWindows = new NewsWindow(me, broadcast);
				newsWindows.run();
						
			} catch (UnknownHostException ex) {
				System.out.println("Não foi possivel abrir a ligacao");
			} catch (NullPointerException ex) { 
				System.out.println("Introduza um IP valido!");
			} catch (NumberFormatException ex) {
				System.out.println("Introduza um IP valido!");
			}
		}
	}
}
