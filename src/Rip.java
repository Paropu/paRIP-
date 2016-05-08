import static java.lang.Math.toIntExact;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Rip {
	public static void main(String[] args) throws IOException {

		/*
		 * PENDIENTE:
		 * eliminar de la tabla si lleva mas de 30 segundos sin llegar (Se cae un nodo)
		 * que en lugar de 10 segundos sea un numero aleatorio (p.e. entre 8 y 12)
		 * disenho correcto de la tabla por pantalla
		 * 
		 * Mejoras:
		 * HECHO --> Split Horizon: No se envía las direcciones de la tabla a la dirección NextHop
		 * Triggered Updates: Envío inmediato de la tabla cuando haya un cambio
		 */

		final String interfaz = "wlan0";
		final int cuantaAtras = 5000;
		TreeMap<String, Vecino> vecinos = new TreeMap<String, Vecino>();
		TreeMap<String, Subred> subredes = new TreeMap<String, Subred>();
		TreeMap<String, Ruta> tabla = new TreeMap<String, Ruta>();

		// Creo objeto vecino con los datos del ordenador y meto en tabla
		Vecino local = new Vecino(args, interfaz);
		System.out.println(local.getDireccion()); // HERRAMIENTA: Ver IP local
		vecinos.put(local.getDireccion(), local);
		tabla.put(local.getDireccion(), new Ruta(local, "local")); // Anadimos IP propia a la tabla para enviar

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
		int ii = 0;
		boolean interrumpido = false;
		int difMiliseg = 0;

		do {
			System.out.print(ii);
			ii++;
			// Mostrar tabla inicial periodicamente
			System.out.println("\nDireccion IP" + "\t\t" + "Mascara" + "\t\t\t\t" + "Siguiente salto" + "\t\t" + "Coste");
			Set<String> setTabla = tabla.keySet();
			Iterator<String> it = setTabla.iterator();
			while (it.hasNext()) {
				System.out.println(tabla.get(it.next()));
			}

			// Escuchamos datagramas entrantes
			byte[] mensajeBits = new byte[504];
			try {
				GregorianCalendar tiempoInicial = new GregorianCalendar();
				long milisegInicial = tiempoInicial.getTimeInMillis();
				if (interrumpido && (difMiliseg < cuantaAtras)) {
					System.out.println("tiempo restante: " + (cuantaAtras - difMiliseg));
					socket.setSoTimeout((cuantaAtras - difMiliseg));
				} else {
					System.out.println("tiempo reiniciado: " + cuantaAtras);
					socket.setSoTimeout(cuantaAtras);
				}
				interrumpido = false;
				DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length);
				socket.receive(datagrama);

				// Si llega un datagrama, lo procesamos
				// Quitar cabecera
				interrumpido = true;
				GregorianCalendar tiempoFinal = new GregorianCalendar();
				long milisegFinal = tiempoFinal.getTimeInMillis();
				difMiliseg = toIntExact(milisegFinal - milisegInicial);

				ByteBuffer bufferSinCabecera = ByteBuffer.allocate(500);
				bufferSinCabecera.put(mensajeBits, 4, 500); // Quitamos cabecera
				bufferSinCabecera.rewind();
				byte[] mensajeSinCabecera = new byte[500];
				bufferSinCabecera.get(mensajeSinCabecera);

				int i = 0;
				while (mensajeSinCabecera[1 + (i * 20)] == 2) {
					// Crear objeto ruta
					Ruta temp = new Ruta(mensajeSinCabecera, i, datagrama.getAddress(), datagrama.getPort());
					// Comprobar Bellman-Ford
					if (temp.getDireccionIP().compareTo(local.getDireccion()) != 0 && temp.Bellman_Ford(tabla, temp)) {
						// Sustituir en tabla
						System.out.println("paso BF :" + temp.getDireccionIP() + " " + temp.getCoste());
						tabla.put(temp.getDireccionIP(), temp);
						// Anadir a TreeMap vecinos para poder enviar a partir de ahora
						vecinos.put(datagrama.getAddress().toString(), new Vecino(temp.getNextHop().substring(1) + ":" + datagrama.getPort()));
					}
					i++;
				}

			} catch (SocketTimeoutException e) {
				System.out.println("Se acabo el tiempo");

				// Enviamos a vecinos
				Set<String> setVecinos = vecinos.keySet();
				Iterator<String> itVecinos = setVecinos.iterator();
				Vecino aux = null;

				// Construimos datos
				try {
					while (itVecinos.hasNext()) {
						// Creamos mensaje con datos de la tabla
						ByteBuffer bufferSalida = ByteBuffer.allocate(504);
						// Construimos cabecera
						bufferSalida.put(Ruta.construirCabecera());
						String dirDestino = itVecinos.next();
						bufferSalida.put(Ruta.construirPaquete(tabla, vecinos.get(dirDestino).getDireccion()));
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
						aux = vecinos.get(dirDestino);
						if (aux.getDireccion().compareTo(local.getDireccion()) != 0) {
							System.out.println("Envio a " + aux.getDireccion() + ":" + aux.getPuerto());
							DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length, aux.getInet(), aux.getPuerto()); // Direccion destino y puerto destino
							socket.send(datagrama);
						}

					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		} while (true);
	}
}
