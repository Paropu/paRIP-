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

		/*
		 * // Abrir puerto para recibir datagramas
		 * Integer port = new Integer(ip.getPuerto());
		 * DatagramSocket socket;
		 * try {
		 * socket = new DatagramSocket(port);
		 * byte[] buffer = new byte[50]; // Tamano maximo del mensaje
		 * DatagramPacket datagram = new DatagramPacket(buffer, port);
		 * SocketAddress addr = new SocketAddress() {
		 * 
		 * };
		 * socket.connect();
		 * socket.receive(datagram);
		 * String mensaje = new String(buffer);
		 * System.out.println(mensaje);
		 * socket.close();
		 * } catch (Exception e) {
		 * e.printStackTrace();
		 * }
		 */
	}
}
