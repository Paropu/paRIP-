import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.TreeMap;

public class OLDRip {

	/*
	 * Procesar argumento de entrada, u obtener la IP del interna
	 * - interfaz eth0 IP asociada
	 * Nombre del archivo: "ripconf-A.B.C.D.topo"
	 * - leer y procesar este fichero
	 * 172.19.45.9:4444 <-- IPs vecinos + puerto
	 * ------
	 * ------
	 * I.J.K.L/25 <-- Subred finalizada
	 * Crear sockets.[UDP]
	 * - Configurar un timer en el socket
	 * DatagramSocket
	 * do{
	 * try{
	 * receive(); //Proceso paquete, Actualizo tabla;
	 * } catch{ //Gestiona el vencimiento del temporizador
	 * //Envia update a vecino
	 * //Reset? temporizador
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

		// Direcciones de vecinos en TreeMap
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

		do {
			Integer puertoOrigen = new Integer(origen.getPuerto());
			DatagramSocket socket;
			byte[] buffer = new byte[50]; // Tamano maximo del mensaje (No se que numero es aun)
			String mensaje = new String(buffer);
			// Recibimos datagramas durante 10 segundos
			try {
				// Escuchamos datagramas entrantes durante 10 segundos
				// Hay que poner en algun punto el tiempo de vida, aun no encontre donde
				socket = new DatagramSocket(puertoOrigen, origen.getInet());
				DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, origen.getInet(), puertoOrigen);
				socket.receive(datagrama);
				socket.close();
				/*
				 * if (Recibimos datagrama){
				 * anotamos cuantos segundos quedan hasta 10
				 * para volver en el mismo instante y que
				 * siempre se env�e un pk cada 10 segundos
				 * 
				 * y salimos del try
				 * }
				 */
			} catch (SocketTimeoutException e) {

				/*
				 * Enviamos nuestra tabla de encaminamiento a todos los vecinos
				 * 
				 */
				// CONSEJO RAUL: la primera vez solo enviar red conectada e IP propia

			} catch (IOException e) {
				e.printStackTrace();
			}

			// Si entro algun datagrama, lo comparamos con nuestra tabla
			// y volvemos al bucle
		} while (true);
	}
}
