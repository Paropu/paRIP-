import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Rip {
	public static void main(String[] args) throws SocketException, UnknownHostException {

		/*
		 * Obtener IP y puerto introducidos por linea de comandos.
		 * Si no se ha introducido nada, se busca la IP del terminal y se asigna el puerto 5512.
		 * 
		 * Leer fichero.topo y crear dos treemaps (Vecinos y Subredes) con la informacion contenida.
		 * 
		 * while(true){
		 * Se programa un timer de 10 segundos
		 * Si durante ese tiempo se recibe algun datagrama, es procesado y se actualliza su vector de distancias, y si corresponde, se modifica su tabla de encaminamiento. (Bellman-Ford)
		 * catch(){
		 * Cuando el tiempo se termine, se enviara un datagrama a todos los terminales vecinos y subredes con la informacion de su vector de distancias.
		 * }
		 * }
		 */

		/*
		 * PENDIENTE:
		 * pasar por parametro "eth0"
		 * diseño correcto de la tabla por pantalla
		 * control del tiempo dentro del bucle
		 * mejoras
		 */

		TreeMap<String, Vecino> vecinos = new TreeMap<String, Vecino>();
		TreeMap<String, Subred> subredes = new TreeMap<String, Subred>();
		TreeMap<String, Ruta> tabla = new TreeMap<String, Ruta>();

		// Creo objeto vecino con los datos del ordenador y meto en tabla
		Vecino local = new Vecino(args);
		System.out.println(local.getDireccion()); // HERRAMIENTA: Ver IP local
		vecinos.put(local.getDireccion(), local);
		tabla.put(local.getDireccion(), new Ruta(local, "local")); // Añadimos IP propia a la tabla para enviar

		// Leer fichero
		FileInputStream flujo_entrada = null;
		try {
			flujo_entrada = new FileInputStream("ripconf-" + local.getDireccion().substring(1, local.getDireccion().length()) + ".topo");
		} catch (FileNotFoundException NoExisteFichero) {
			System.out.println("Fichero inexistente");
			System.exit(0);
		}

		// Construimos vecinos y subredes, y metemos en TreeMap
		Scanner entrada = new Scanner(flujo_entrada);
		while (entrada.hasNext()) {
			String lectura = entrada.nextLine();
			if (lectura.contains("/")) {
				Subred subred = new Subred(lectura);
				subredes.put(subred.getDireccion(), subred);
				tabla.put(subred.getDireccion(), new Ruta(subred, "subred"));
			} else {
				Vecino vecino = new Vecino(lectura);
				vecinos.put(vecino.getDireccion(), vecino);
				tabla.put(vecino.getDireccion(), new Ruta(vecino, "vecino"));
			}
		}
		entrada.close();

		// Escuchamos datagramas
		DatagramSocket socket = new DatagramSocket(local.getPuerto(), local.getInet());
		do {
			// Mostrar tabla inicial periodicamente
			System.out.println("Direccion IP" + "\t\t" + "Mascara" + "\t\t\t\t" + "Siguiente salto" + "\t\t" + "Coste");
			Set<String> setTabla = tabla.keySet();
			Iterator<String> it = setTabla.iterator();
			while (it.hasNext()) {
				System.out.println(tabla.get(it.next()));
			}

			byte[] mensajeBits = new byte[504];
			try {
				socket.setSoTimeout(5000);
				DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length);
				socket.receive(datagrama);

				ByteBuffer bufferSinCabecera = ByteBuffer.allocate(500);
				bufferSinCabecera.put(mensajeBits, 4, 500); // Quitamos cabecera
				bufferSinCabecera.rewind();
				byte[] mensajeSinCabecera = new byte[500];
				bufferSinCabecera.get(mensajeSinCabecera);

				/*
				 * for (int i = 0; i < 50; i++) {
				 * System.out.print(Byte.toUnsignedInt(bufferSinCabecera.get()));
				 * }
				 * System.out.println();
				 */
				int i = 0;
				while (mensajeSinCabecera[1 + (i * 20)] == 2) {
					// Crear objeto ruta
					Ruta temp = new Ruta(mensajeSinCabecera, i, datagrama.getAddress());
					// Comprobar Bellman-Ford
					if (temp.getDireccionIP().compareTo(local.getDireccion()) != 0 && temp.Bellman_Ford(tabla, temp)) {
						// Sustituir en tabla
						tabla.put(temp.getDireccionIP(), temp);
					}
					i++;
				}

			} catch (SocketTimeoutException e) {
				// Creamos mensaje con datos de la tabla
				ByteBuffer bufferSalida = ByteBuffer.allocate(504); // Creo ByteBuffer de 20 bytes

				// Construimos cabecera
				bufferSalida.put(Ruta.construirCabecera());

				// Construimos datos
				Iterator<String> it2 = setTabla.iterator();
				String key = null;
				Ruta ruta = null;
				try {
					while (it2.hasNext()) {
						bufferSalida.put(Ruta.construirPaquete(tabla.get(it2.next())));
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				// Introducimos en byte[]
				bufferSalida.rewind();
				bufferSalida.get(mensajeBits, 0, 504);

				/*
				 * // HERRAMIENTA ver bytes
				 * int i = 0;
				 * for (; i < 4; i += 2) {
				 * System.out.print(mensajeBits[i] + " ");
				 * System.out.print(mensajeBits[i + 1] + "   ");
				 * }
				 * System.out.println();
				 * for (int j = 1; j < 4; j++) {
				 * for (; i < (j * 20) + 4; i += 2) {
				 * System.out.print(mensajeBits[i] + "  ");
				 * System.out.print(mensajeBits[i + 1] + "\t");
				 * }
				 * System.out.println();
				 * }
				 */

				// Enviamos a vecinos
				Set<String> setVecinos = vecinos.keySet();
				Iterator<String> it3 = setVecinos.iterator();
				String key2 = null;
				Vecino aux = null;
				try {
					while (it3.hasNext()) {
						key2 = it3.next();
						aux = vecinos.get(key2); // Cambiar por TreeMap vecinos
						if (!aux.getDireccion().equals(local.getDireccion())) {
							DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length, aux.getInet(), aux.getPuerto()); // Direccion destino y puerto destino
							socket.send(datagrama);
						}
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (true);
	}
}
