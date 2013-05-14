package inews.dataStructures;

import java.io.Serializable;

/**
 * Estrutura de dados IP
 * @author Luis Portela
 */
public class IPStruct implements Comparable<IPStruct>, Serializable {
	
	private final int ip[];
	
	private int port = 0;
	
	/**
	 * Contructor que aceita os octetos dum IP divididos e uma porta
	 * @param firstOct primeiro octeto
	 * @param secondOct segundo octeto
	 * @param thirdOct terceiro octeto
	 * @param fourthOct quarto octeto
	 * @param port  porta
	 */
	public IPStruct(int firstOct, int secondOct, int thirdOct, int fourthOct, int port){
		
		this.ip = new int[4];
		
		this.ip[0] = firstOct;
		this.ip[1] = secondOct;
		this.ip[2] = thirdOct;
		this.ip[3] = fourthOct;
		this.port = port;
	}
	
	/**
	 * Constructor que aceita como entrada uma String formatada xxx.xxx.xxx.xxx:xyzw
	 * @param newIp  Estruct
	 */
	public IPStruct(String newIp){
		
		this.ip = new int[4];
		this.port = 0;
		String tmp[] = newIp.split("\\.");
		int index, i = 0;
		
		while (i < 3) {
			this.ip[i] = Integer.parseInt(tmp[i]);
			i++;
		}
		
		this.port = ((index = tmp[i].indexOf(":")) > 0) ? Integer.parseInt(tmp[i].substring(index+1)) : 0;
		this.ip[i] = (index > 0) ? Integer.parseInt(tmp[i].substring(0, index)) : Integer.parseInt(tmp[i]);
	}
	
	/**
	 * Constructor que aceita como parametro de entrada uma String no formato xxx.xxx.xxx.xxx:xyzw e uma porta sendo a porta indicada na string
	 * substituida pela porta passada como parametro
	 * @param ip string no formato xxx.xxx.xxx.xxx ou xxx.xxx.xxx.xxx:xyzw
	 * @param port  nova porta
	 */
	public IPStruct(String ip, int port){
		this(ip);
		this.port = port;
	}
	
	/**
	 * Constructor que aceita como parametro um objecto do mesmo tipo
	 * @param ip  objecto do tipo IPStruct
	 * @see IPStruct
	 */
	public IPStruct(IPStruct ip) {
		this(ip.getFirtstOct(), ip.getSecondOct(), ip.getThirdOct(), ip.getFourthOct(), ip.getPort());
	}
	
	/**
	 * Constructor que aceita como parametro um objecto do mesmo tipo e uma porta como parametro de entrada substituindo a porta pela passada por parametro
	 * @param ip objecto do tipo IPStruct
	 * @param port  nova porta
	 * @see IPStruct
	 */
	public IPStruct(IPStruct ip, int port) {
		this(ip.getFirtstOct(), ip.getSecondOct(), ip.getThirdOct(), ip.getFourthOct(), port);
	}

	/**
	 * Metodo que devolve o primeiro octeto deste IP
	 * @return Integer primeiro octeto
	 */
	public int getFirtstOct() {
		return this.ip[0];
	}

	/**
	 * Metodo que devolve o segundo octeto deste IP
	 * @return Integer segundo octeto
	 */
	public int getSecondOct() {
		return this.ip[1];
	}

	/**
	 * Metodo que devolve o terceiro octeto deste IP
	 * @return Integer terceiro octeto
	 */
	public int getThirdOct() {
		return this.ip[2];
	}
	
	/**
	 * Metodo que devolve o quarto octeto deste IP
	 * @return Integer quarto octeto
	 */
	public int getFourthOct(){
		return this.ip[3];
	}
	
	/**
	 * Metodo que devolve o IP sem a porta numa String
	 * @return String com o IP sem a porta
	 */
	public String getIp() {
		return (this.ip[0] + "." + this.ip[1] + "." + this.ip[2] + "." + this.ip[3]);
	}
	
	/**
	 * Metodo que devolve a porta da estructura IP
	 * @return Integer porta
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Metodo que permite alterar a porta duma estructura de dados IPStruct
	 * @param port nova porta
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Metodo que devolve a estrucura de dados IP em string com a porta se esta estiver definida
	 * @return String com o IP com/sem porta
	 */
	@Override
	public String toString(){
		return (this.ip[0] + "." + this.ip[1] + "." + this.ip[2] + "." + this.ip[3]) + (this.port != 0 ? ":" + this.port : "");
	}

	/**
	 * Metodo que permite comparar duas estructuras e devolver a diferenca entre elas
	 * 
	 * @param t Estructura IPStruct a comparar
	 * @return 
	 * @see Comparable
	 * @see IPStruct
	 */
	@Override
	public int compareTo(IPStruct t) {
		
		int result = ((this.getFirtstOct() + this.getSecondOct() + this.getThirdOct() + this.getFourthOct()) - (t.getFirtstOct() + t.getSecondOct() + t.getThirdOct() + t.getFourthOct()));
		return result/(result == 0 ? 1 : Math.abs(result));
	}

	/**
	 * Metodo que permite comparar duas estructuras e verificar se estas são iguais
	 * @param t estructura IP a comparar
	 * @return false - Estructuras diferentes
	 * true - Estructura iguais
	 */
	public boolean equals(IPStruct t){
		return this.compareTo(t) == 0 ? true : false;
	}
}