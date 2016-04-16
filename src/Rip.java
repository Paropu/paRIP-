import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Rip {

	/*
	 * 1º Procesar argumento de entrada, u obtener la IP del ¿interna?
	 * - interfaz eth0 IP asociada
	 * 2º Nombre del archivo: "ripconf-A.B.C.D topo"
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
		// System.out.println(ip); // Muestro direccion IP y puerto
		try {
			addr = InetAddress.getByName(ip.getDireccion().substring(1, ip.getDireccion().length()));
		} catch (Exception e) {
		}

		Integer port = new Integer(ip.getPuerto());
		DatagramSocket socket;
		byte[] buffer = new byte[50]; // Tamano maximo del mensaje
		String mensaje = new String(buffer);

		// Enviar datagramas
		try {
			socket = new DatagramSocket();
			DatagramPacket datagram = new DatagramPacket(mensaje.getBytes(), mensaje.length(), addr, port);

			socket.send(datagram);
			socket.close();
		} catch (Exception e) {

		}

		// Abrir puerto para recibir datagramas

		try {
			socket = new DatagramSocket(port, addr); // Asigno el puerto por el que van a entrar los datagramas
			DatagramPacket datagram = new DatagramPacket(buffer, port);
			// SocketAddress addr = new SocketAddress() {
			// };
			/// socket.connect(addr);
			socket.receive(datagram);
			System.out.println(mensaje);
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
