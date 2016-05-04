import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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

		TreeMap<String, Vecino> vecinos = new TreeMap<String, Vecino>();
		TreeMap<String, Subred> subredes = new TreeMap<String, Subred>();
		TreeMap<String, VectorDistancias> tabla = new TreeMap<String, VectorDistancias>();

		// Creo objeto vecino con los datos del ordenador y meto en tabla
		Vecino local = new Vecino(args);
		System.out.println(local.getDireccion()); // HERRAMIENTA: Ver IP local
		vecinos.put(local.getDireccion(), local);
		tabla.put(local.getDireccion(), new VectorDistancias(local, "local")); // Añadimos IP propia a la tabla para enviar

		// Leer fichero
		FileInputStream flujo_entrada = null;
		try {
			flujo_entrada = new FileInputStream("ripconf-" + local.getDireccion().substring(1, local.getDireccion().length()) + ".topo");
		} catch (FileNotFoundException NoExisteFichero) {
			System.out.println("Fichero inexistente");
			System.exit(0);
		}
		Scanner entrada = new Scanner(flujo_entrada);

		// Direcciones de vecinos y subredes en TreeMap
		while (entrada.hasNext()) {
			String lectura = entrada.nextLine();
			if (lectura.contains("/")) {
				Subred subred = new Subred(lectura);
				subredes.put(subred.getDireccion(), subred);
				tabla.put(subred.getDireccion(), new VectorDistancias(subred, "subred"));
			} else {
				Vecino vecino = new Vecino(lectura);
				vecinos.put(vecino.getDireccion(), vecino);
				tabla.put(vecino.getDireccion(), new VectorDistancias(vecino, "vecino"));
			}
		}
		entrada.close();

		// Mostrar tabla inicial
		System.out.println("Direccion IP" + "\t\t" + "Mascara" + "\t\t\t\t" + "Siguiente salto" + "\t\t" + "Coste");
		Set<String> setTabla = tabla.keySet();
		Iterator<String> it = setTabla.iterator();
		while (it.hasNext()) {
			System.out.println(tabla.get(it.next()));
		}

		// Escuchamos datagramas
		InetAddress yo = InetAddress.getLocalHost();
		DatagramSocket socket = new DatagramSocket(local.getPuerto(), local.getInet());
		do {
			System.out.println("Inicio");
			byte[] mensajeBits = new byte[1500];
			try {
				socket.setSoTimeout(2000);
				DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length);
				socket.receive(datagrama);
				System.out.println(new String(datagrama.getData()));
				/*
				 * if (Recibimos datagrama){
				 * anotamos cuantos segundos quedan hasta 10
				 * para volver en el mismo instante y que
				 * siempre se envie un pk cada 10 segundos
				 * 
				 * y salimos del try
				 * }
				 */
			} catch (SocketTimeoutException e) { // ¿InterruptedIOException?
				String mensaje = new String("hola");
				mensajeBits = mensaje.getBytes();

				Iterator<String> it2 = setTabla.iterator();
				String key = null;
				VectorDistancias aux = null;
				try {
					while (it2.hasNext()) {
						key = it2.next();
						aux = tabla.get(key);
						if (!aux.getDireccionIP().equals(local.getDireccion())) {
							DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length, aux.getVecino().getInet(), aux.getVecino().getPuerto()); // Direccion destino y puerto destino
							socket.send(datagrama);
						}
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			/*
			 * Si entro algun datagrama, lo comparamos con nuestra tabla
			 * y volvemos al bucle
			 */

		} while (true);
	}
}
