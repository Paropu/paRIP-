import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.util.TreeMap;

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
		Router origen = new Router(args);

		// Muestro direccion IP y puerto
		System.out.println("Direccion IP: " + origen);

		// Leer fichero y guardar vecinos en ArrayList direccionVecinos
		FileInputStream flujo_entrada = null;
		try {
			flujo_entrada = new FileInputStream("ripconf-" + origen.getDireccion().substring(1, origen.getDireccion().length()) + ".topo");
		} catch (FileNotFoundException NoExisteFichero) {
			System.out.println("Fichero inexistente");
			System.exit(0);
		}

		Scanner entrada = new Scanner(flujo_entrada); // Se crea un objeto para escanear la linea del fichero
		// Cambiar por TreeMap
		TreeMap<String, Router> direccionVecinos = new TreeMap<String, Router>();
		while (entrada.hasNext()) {
			String lectura = entrada.nextLine();
			Router vecino = new Router(lectura);
			String[] redAlcanzable = lectura.split("/");
			if (redAlcanzable.length == 2) { // Dos casos en fichero entrada
				direccionVecinos.put(redAlcanzable[0], vecino); // Red alcanzable (Ruta conectada)
			} else {
				direccionVecinos.put(vecino.getDireccion(), vecino); // Router adyacente
			}
		}
		entrada.close();
		System.out.println("Lista de IPs del archivo: " + direccionVecinos);

		// Enviar y recibir datagramas
		Integer port = new Integer(origen.getPuerto());
		DatagramSocket socket;
		byte[] buffer = new byte[50]; // Tamano maximo del mensaje
		String mensaje = new String(buffer);

		// Enviar datagramas
		/*
		 * 
		 */
		// for (int i = 0; i != direccionVecinos.size(); i++) {
		// Emisor emisor = new Emisor(direccionVecinos.get(i).getInet(), new Integer(direccionVecinos.get(i).getPuerto()));
		// }

		// Abrir puerto para recibir datagramas
		/*
		 * bucle recorriendo el TreeMap de las IPs del archivo .topo
		 */

	}
}
