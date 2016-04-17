import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Rip {

	/*
	 * 1º Procesar argumento de entrada, u obtener la IP del ¿interna?
	 * - interfaz eth0 IP asociada
	 * 2º Nombre del archivo: "ripconf-A.B.C.D.topo"
	 * - leer y procesar este fichero
	 * 172.19.45.9:4444 <-- IPs vecinos + puerto
	 * ------
	 * ------
	 * I.J.K.L/25 <-- Subred finalizada
	 * 3º Crear sockets.[UDP]
	 * - Configurar un tier en el socket
	 * DatagramSocket
	 * do{
	 * try{
	 * receive(); //Proceso paquete, Actualizo tabla;
	 * } catch{ //Gestiona el vencimiento del temporizador
	 * //Envía update a vecino
	 * //¿Reset? temporizador
	 * }
	 * } while (true);
	 * 
	 * Bellman-Ford!!!
	 */

	public static void main(String[] args) {
		// Comprobar datos de entrada y asignar direccion y puerto
		IP ip = new IP(args);
		InetAddress addr = null;
		if (ip.getDireccion().equals("ERROR")) {
			System.out.println("Argumentos incorrectos");
			return;
		}
		// Muestro direccion IP y puerto
		// System.out.println(ip);

		// Leer fichero y guardar vecinos en ArrayList direccionVecinos
		FileInputStream flujo_entrada = null;
		try {
			flujo_entrada = new FileInputStream("ripconf-192.168.0.34.topo");
		} catch (FileNotFoundException NoExisteFichero) {
			System.out.println("Fichero inexistente");
			System.exit(0);
		}
		Scanner entrada = new Scanner(flujo_entrada); // Se crea un objeto para escanear la linea del fichero
		ArrayList<IP> direccionVecinos = new ArrayList<IP>();
		while (entrada.hasNextLine()) {
			direccionVecinos.add(new IP(entrada.nextLine()));
		}
		entrada.close();

		// Enviar y recibir datagramas
		Integer port = new Integer(ip.getPuerto());
		DatagramSocket socket;
		byte[] buffer = new byte[50]; // Tamano maximo del mensaje
		String mensaje = new String(buffer);

		// Enviar datagramas
		/*
		 * 
		 */

		// Abrir puerto para recibir datagramas
		/*
		 * bucle recorriendo el TreeMap de las IPs del archivo .topo
		 */

	}
}
