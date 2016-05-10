import static java.lang.Math.toIntExact;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ConcurrentModificationException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Clase principal, contiene la estructura fundamental
 *
 */
public class Rip {
	public static void main(String[] args) throws IOException {

		/**
		 * La interfaz por defecto es eth0
		 * Envio en media cada 10 segundos, con un maximo de 12 segundos y un minimo de 8
		 * Asigno coste infinito al cabo de 30 segundos y borro de la tabla tras 60 segundos
		 */

		int tiempoSubirCoste = 30 * 1000; 		// tiempo para coste infinito a una entrada de la tabla
		int tiempoEliminar = 60 * 1000; 		// tiempo eliminar entrada de la tabla
		int tiempoMedioEnvio = 10 * 1000;
		int varianzaEnvio = 2 * 1000; 			// tiempo de espera entre (tiempoMedioEnvio+-varianzaEnvio)
		String interfaz = "eth0"; 				// nombre de interfaz IP por defecto

		TreeMap<String, Vecino> vecinos = new TreeMap<String, Vecino>(); 	// vecinos (Coste 1)
		TreeMap<String, Subred> subredes = new TreeMap<String, Subred>(); 	// subredes (Coste 1)
		TreeMap<String, Ruta> tabla = new TreeMap<String, Ruta>();		 	// datos de la tabla
		TreeMap<String, Ruta> cambios = new TreeMap<String, Ruta>(); 		// Triggered Updates

		// Creo objeto vecino con los datos del ordenador y meto en tabla
		Vecino local = new Vecino(args, interfaz);
		// System.out.println(local.getDireccion()); // HERRAMIENTA: Ver IP local
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

		DatagramSocket socket = new DatagramSocket(local.getPuerto(), local.getInet());
		boolean cambiosEnTabla = false; 	// Triggered Updates
		boolean interrumpido = false; 		// true si entró un datagrama durante el tiempo de espera
		int difMiliseg = 0; 				// In. tiempo entre crear DatagramPacket y recibir
		int numeroAleatorio = 0; 			// In. tiempo de espera

		// Inicio de bucle infinito
		do {
			cambios.clear(); // Borramos TreeMap de Triggered Updates
			Set<String> setTabla = tabla.keySet();
			Iterator<String> it = setTabla.iterator();
			boolean borrado = true; // Variable para volver a inicializar set e iterator si borramos alguna entrada de la tabla
			cambiosEnTabla = false;

			// Comprobar timers para aumentar coste o borrar de la tabla
			while (borrado) {
				try {
					setTabla = tabla.keySet();
					it = setTabla.iterator();
					while (it.hasNext()) {
						String key = it.next();
						GregorianCalendar horaActual = new GregorianCalendar();
						// Si llevamos mas de tiempoEliminar sin recibir esta entrada, la borramos
						if ((tabla.get(key).getCoste() >= 16 && (horaActual.getTime().getTime() - tabla.get(key).getTimer()) > tiempoEliminar && tabla.get(key).getNextHop().compareTo(local.getDireccion()) != 0)) {
							vecinos.remove(tabla.get(key).getDireccionIP());
							tabla.remove(key);
							borrado = true;
							continue;
						}
						// Si llevamos mas de tiempoSubirCoste sin recibir esta entrada, subimos coste a 16
						if ((tabla.get(key).getTimer() != 0 && (horaActual.getTime().getTime() - tabla.get(key).getTimer()) > tiempoSubirCoste && tabla.get(key).getNextHop().compareTo(local.getDireccion()) != 0) && tabla.get(key).getCoste() < 16) {
							tabla.get(key).setCoste(16);
							cambios.put(tabla.get(key).getDireccionIP(), tabla.get(key));
							cambiosEnTabla = true; // Triggered updates
							continue;
						}
						borrado = false;
					}
				} catch (ConcurrentModificationException e3) {
				}
			}

			// Mostrar tabla periodicamente
			setTabla = tabla.keySet();
			it = setTabla.iterator();
			String key2 = null;
			System.out.println("\n\nDireccion IP" + "\t" + "Mascara" + "\t\t\t" + "Siguiente salto" + "\t\t" + "Coste");
			if (setTabla.size() > 1) {
				while (it.hasNext()) {
					key2 = it.next();
					if (tabla.get(key2).getDireccionIP().compareTo(local.getDireccion()) != 0)
						System.out.println(tabla.get(key2));
				}
			} else {
				System.out.println("No hay ninguna entrada en la tabla");
			}

			// Escuchamos datagramas entrantes
			byte[] mensajeBits = new byte[504];
			try {
				GregorianCalendar tiempoInicial = new GregorianCalendar();
				long milisegInicial = tiempoInicial.getTimeInMillis(); // Guardamos tiempo empezar escucha
				if (interrumpido && (difMiliseg < numeroAleatorio)) {
					numeroAleatorio -= difMiliseg;
					// System.out.println("tiempo restante: " + numeroAleatorio); //HERRAMIENTA ver tiempo
					socket.setSoTimeout(numeroAleatorio); // Seguimos contando por cuando llego el mensaje
				} else {
					numeroAleatorio = (int) (Math.random() * ((tiempoMedioEnvio + varianzaEnvio) - (tiempoMedioEnvio - varianzaEnvio) + 1) + (tiempoMedioEnvio - varianzaEnvio)); // (Max-min+1)+min
					// System.out.println("tiempo reiniciado: " + numeroAleatorio); //HERRAMIENTA ver tiempo
					socket.setSoTimeout(numeroAleatorio);
				}
				interrumpido = false;
				DatagramPacket datagrama = new DatagramPacket(mensajeBits, mensajeBits.length);
				socket.receive(datagrama);

				// Si llega un datagrama, lo procesamos
				// Quitar cabecera
				interrumpido = true;
				GregorianCalendar tiempoFinal = new GregorianCalendar();
				long milisegFinal = tiempoFinal.getTimeInMillis(); 			// Guardamos tiempo de llegada
				difMiliseg = toIntExact(milisegFinal - milisegInicial); 	// Calculamos tiempo escuchando

				ByteBuffer bufferSinCabecera = ByteBuffer.allocate(500);
				bufferSinCabecera.put(mensajeBits, 4, 500); // Quitamos cabecera
				bufferSinCabecera.rewind();
				byte[] mensajeSinCabecera = new byte[500];
				bufferSinCabecera.get(mensajeSinCabecera);

				int i = 0; // contador de registros
				while (mensajeSinCabecera[1 + (i * 20)] == 2 || i == 24) {
					// Crear objeto ruta
					GregorianCalendar horaRecibido = new GregorianCalendar();
					Ruta temp = new Ruta(mensajeSinCabecera, i, datagrama.getAddress(), datagrama.getPort(), horaRecibido.getTime().getTime());
					// Actualizar timer
					if (temp.actualizarTimer(tabla, temp)) {
						tabla.put(temp.getDireccionIP(), temp);
					}
					// Comprobar Bellman-Ford
					if (temp.getDireccionIP().compareTo(local.getDireccion()) != 0 && temp.Bellman_Ford(tabla, temp)) {
						// Sustituir en tabla
						tabla.put(temp.getDireccionIP(), temp);
						cambios.put(temp.getDireccionIP(), temp);
						// Anadir a TreeMap vecinos para poder enviar a partir de ahora
						vecinos.put(datagrama.getAddress().toString(), new Vecino(temp.getNextHop().substring(1) + ":" + datagrama.getPort()));
						cambiosEnTabla = true;
					}
					i++;
				}
				if (cambiosEnTabla)
					throw new SocketTimeoutException(); // Triggered updates

			} catch (SocketTimeoutException e) {
				// Enviamos a vecinos
				Set<String> setVecinos = vecinos.keySet();
				Iterator<String> itVecinos = setVecinos.iterator();
				Vecino aux = null;

				// Construimos datos
				try {
					// Bucle para cada destino (Split horizon)
					while (itVecinos.hasNext()) {
						String dirDestino = itVecinos.next();
						// Creamos mensaje con datos de la tabla
						int tamanho = 0;
						if (cambiosEnTabla) { // Si Triggered updates, enviamos solo los cambios
							tamanho = Ruta.averiguarTamanho(cambios, vecinos.get(dirDestino).getDireccion());// Calculamos tamanho de envio
						} else { // Si no, toda la tabla
							tamanho = Ruta.averiguarTamanho(tabla, vecinos.get(dirDestino).getDireccion());// Calculamos tamanho de envio
						}
						if (tamanho > 504) { // Limite de enviar 25 entradas en la tabla
							tamanho = 504;
						}
						byte[] mensajeEnvioBits = new byte[tamanho];

						ByteBuffer bufferSalida = ByteBuffer.allocate(tamanho);
						// Construimos cabecera
						bufferSalida.put(Ruta.construirCabecera());
						// Construimos datos
						if (cambiosEnTabla) {
							bufferSalida.put(Ruta.construirPaquete(cambios, vecinos.get(dirDestino).getDireccion()));
						} else {
							bufferSalida.put(Ruta.construirPaquete(tabla, vecinos.get(dirDestino).getDireccion()));
						}
						// Introducimos en byte[]
						bufferSalida.rewind();
						bufferSalida.get(mensajeEnvioBits, 0, tamanho);
						aux = vecinos.get(dirDestino);
						// Enviamos a vecinos
						if (aux.getDireccion().compareTo(local.getDireccion()) != 0) {
							// System.out.println("Envio a " + aux.getDireccion() + ":" + aux.getPuerto()); //HERRAMIENTA: Ver envios
							DatagramPacket datagrama = new DatagramPacket(mensajeEnvioBits, mensajeEnvioBits.length, aux.getInet(), aux.getPuerto()); // Direccion destino y puerto destino
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
