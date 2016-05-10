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

public class Rip {
	public static void main(String[] args) throws IOException {

		/*
		 * PENDIENTE:
		 * triggered updates enviar solo cambios
		 * el tamaño del paquete es variable
		 * 
		 * disenho correcto de la tabla por pantalla
		 * no mostrar propia direccion en tabla
		 * Hacer archivo de texto con mejoras
		 * 
		 * Mejoras:
		 * HECHO --> Split Horizon: No se envía las direcciones de la tabla a la dirección NextHop
		 * HECHO --> Triggered Updates: Envío inmediato de la tabla cuando haya un cambio
		 * Un nodo puede tener el fichero vacio al inicio
		 */
		
		final int tiempoSubirCoste = 20*1000;	//tiempo para ponerle coste infinito a una entrada de la tabla
		final int tiempoEliminar = 30*1000;		//tiempo para eliminar entrada de la tabla
		int tiempoMedioEnvio = 10*1000;	//tiempo medio de envio
		int varianzaEnvio = 2*1000;	//tiempo de envio comprendido entre (tiempoMedioEnvio+-varianzaEnvio)
		final String interfaz = "wlan0";	//nombre de interfaz donde obtener IP por defecto
		
		
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
		int numeroAleatorio = 10000;

		do {
			System.out.print(ii);
			ii++;
			
			//Subir coste o borrar entradas antiguas de la tabla
			Set<String> setTabla = tabla.keySet();
			Iterator<String> it = setTabla.iterator();
			boolean borrado = true;	//Variable para volver a inicializar set e iterator si borramos alguna entrada de la tabla
			boolean cambiosEnTabla = false;
			while(borrado){
				try{
					setTabla = tabla.keySet();
					it = setTabla.iterator();
					while (it.hasNext()) {
						String key = it.next();
						GregorianCalendar horaActual = new GregorianCalendar();
						//Si llevamos mas de tiempoEliminar sin recibir esta entrada, la borramos
						if((tabla.get(key).getCoste() >= 16 && (horaActual.getTime().getTime()-tabla.get(key).getTimer()) > tiempoEliminar && tabla.get(key).getNextHop().compareTo(local.getDireccion()) !=0)){
							vecinos.remove(tabla.get(key).getDireccionIP());
							tabla.remove(key);
							borrado = true;
							continue;
						}
						//Si llevamos mas de tiempoSubirCoste sin recibir esta entrada, subimos coste a 16
						if((tabla.get(key).getTimer() != 0 && (horaActual.getTime().getTime()-tabla.get(key).getTimer()) > tiempoSubirCoste && tabla.get(key).getNextHop().compareTo(local.getDireccion()) !=0)){
							tabla.get(key).setCoste(16);;
							cambiosEnTabla = true; //Triggered updates
							continue;
						} 
						borrado=false;
					}
				} catch(ConcurrentModificationException e3){
				}
			}
			
			// Mostrar tabla inicial periodicamente
			setTabla = tabla.keySet();
			it = setTabla.iterator();
			System.out.println("\nDireccion IP" + "\t" + "Mascara" + "\t\t\t" + "Siguiente salto" + "\t" + "Coste" + "\t" + "tiempo");
			while (it.hasNext()) {
				System.out.println(tabla.get(it.next()));
			}
			// Escuchamos datagramas entrantes
			byte[] mensajeBits = new byte[504];
			try {
				GregorianCalendar tiempoInicial = new GregorianCalendar();
				long milisegInicial = tiempoInicial.getTimeInMillis();
				if (interrumpido && (difMiliseg < numeroAleatorio)) {
					System.out.println("tiempo restante: " + (numeroAleatorio - difMiliseg));
					numeroAleatorio -= difMiliseg;
					socket.setSoTimeout(numeroAleatorio);
				} else {
					numeroAleatorio = (int) (Math.random()*((tiempoMedioEnvio+varianzaEnvio)-(tiempoMedioEnvio-varianzaEnvio)+1)+(tiempoMedioEnvio-varianzaEnvio));	//(Max-min+1)+min
					System.out.println("tiempo reiniciado: " + numeroAleatorio);
					socket.setSoTimeout(numeroAleatorio);
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
					GregorianCalendar horaRecibido = new GregorianCalendar();
					Ruta temp = new Ruta(mensajeSinCabecera, i, datagrama.getAddress(), datagrama.getPort(),horaRecibido.getTime().getTime());
					// Actualizar timer
					if(temp.actualizarTimer(tabla,temp)){
						tabla.put(temp.getDireccionIP(), temp);
					}
					// Comprobar Bellman-Ford
					if (temp.getDireccionIP().compareTo(local.getDireccion()) != 0 && temp.Bellman_Ford(tabla, temp)) {
						// Sustituir en tabla
						System.out.println("paso BF :" + temp.getDireccionIP() + " " + temp.getCoste());
						tabla.put(temp.getDireccionIP(), temp);
						// Anadir a TreeMap vecinos para poder enviar a partir de ahora
						vecinos.put(datagrama.getAddress().toString(), new Vecino(temp.getNextHop().substring(1) + ":" + datagrama.getPort()));
						cambiosEnTabla = true;
					}
					i++;
				}
				if(cambiosEnTabla)throw new SocketTimeoutException();

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
