import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.TreeMap;

public class RIP {
	public static void main(String[] args) {

		/*
		 * Obtener IP y puerto introducidos por linea de comandos.
		 * Si no se ha introducido nada, se busca la IP del terminal y se asigna el puerto 5512.
		 * 
		 * Leer fichero.topo y crear dos treemaps (Vecinos y Subredes) con la información contenida.
		 * 
		 * while(true){
		 * Se programa un timer de 10 segundos
		 * Si durante ese tiempo se recibe algún datagrama, es procesado y se actualliza su vector de distancias, y si corresponde, se modifica su tabla de encaminamiento. (Bellman-Ford)
		 * catch(){
		 * Cuando el tiempo se termine, se enviará un datagrama a todos los terminales vecinos y subredes con la información de su vector de distancias.
		 * }
		 * }
		 */

		TreeMap<String, Vecino> vecinos = new TreeMap<String, Vecino>();
		TreeMap<String, Subred> subredes = new TreeMap<String, Subred>();

		// Creo objeto vecino con los datos del ordenador
		Vecino local = new Vecino(args);
		vecinos.put(local.getDireccion(), local);

		// Leer fichero y guardar vecinos en ArrayList direccionVecinos
		FileInputStream flujo_entrada = null;
		try {
			flujo_entrada = new FileInputStream("ripconf-" + local.getDireccion().substring(1, local.getDireccion().length()) + ".topo");
		} catch (FileNotFoundException NoExisteFichero) {
			System.out.println("Fichero inexistente");
			System.exit(0);
		}
		Scanner entrada = new Scanner(flujo_entrada); // Se crea un objeto para escanear la linea del fichero

		// Direcciones de vecinos y subred en TreeMap
		while (entrada.hasNext()) {
			String lectura = entrada.nextLine();
			if (lectura.contains("/")) {
				Subred subred = new Subred(lectura);
				subredes.put(subred.getDireccion(), subred);
			} else {
				Vecino vecino = new Vecino(lectura);
				vecinos.put(vecino.getDireccion(), vecino);
			}
		}
		entrada.close();

		do {
			Integer puertoLocal = new Integer(local.getPuerto()); // Pasar puerto de String a int
			DatagramSocket socket;
			byte[] buffer = new byte[50]; // Tamano maximo del mensaje (No se que numero es aun)
			String mensaje = new String(buffer);
			try {
				socket = new DatagramSocket(); // Completar campos
				socket.setSoTimeout(10000);
				DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, local.getInet(), puertoLocal);
				socket.receive(datagrama);
				/*
				 * if (Recibimos datagrama){
				 * anotamos cuantos segundos quedan hasta 10
				 * para volver en el mismo instante y que
				 * siempre se env�e un pk cada 10 segundos
				 * 
				 * y salimos del try
				 * }
				 */
				socket.close();
			} catch (SocketTimeoutException e) {

			} catch (IOException e) {
				e.printStackTrace();
			}
			// Si entro algun datagrama, lo comparamos con nuestra tabla
			// y volvemos al bucle

		} while (true);
	}
}
