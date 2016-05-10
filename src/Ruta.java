import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Clase con los atributos, metodos y constructores para trabajar con datos de la tabla de encaminamiento
 *
 */
public class Ruta {

	private Vecino vecino;
	private String direccionIP;
	private String mascara;
	private Integer len;
	private String nextHop;
	private Integer coste;
	private long timer; // tiempo transcurrido desde la llegada del paquete

	@Override
	public String toString() {
		if (this.getMascara().length() > 14) {
			return this.getDireccionIP() + "\t" + this.getMascara() + "\t" + this.nextHop + "\t\t" + this.coste;
		} else {
			return this.getDireccionIP() + "\t" + this.getMascara() + "\t\t" + this.nextHop + "\t\t" + this.coste;
		}
	}

	public Vecino getVecino() {
		return this.vecino;
	}

	public String getDireccionIP() {
		return this.direccionIP;
	}

	public String getMascara() {
		return this.mascara;
	}

	public Integer getLen() {
		return this.len;
	}

	public String getNextHop() {
		return this.nextHop;
	}

	public Integer getCoste() {
		return this.coste;
	}

	public void setCoste(Integer coste) {
		this.coste = coste;
	}

	public long getTimer() {
		return this.timer;
	}

	/**
	 * Devuelve true si la Ruta introducida cumple lo necesario para ser añadida o actualizada en la tabla
	 * @param tabla TreeMap con todos los datos de la tabla
	 * @param rutaNueva objeto Ruta con datos recibidos
	 * @return true si hay que añadir rutaNueva a la tabla
	 */
	public Boolean Bellman_Ford(TreeMap<String, Ruta> tabla, Ruta rutaNueva) {
		Set<String> setTabla = tabla.keySet();
		Iterator<String> it = setTabla.iterator();
		boolean existeEnTabla = false;
		Ruta rutaTabla = null;

		while (it.hasNext()) {
			rutaTabla = tabla.get(it.next());

			if (rutaTabla.getDireccionIP().compareTo(rutaNueva.getDireccionIP()) == 0) {
				existeEnTabla = true;
				if (rutaTabla.getCoste() == rutaNueva.getCoste()) {
					return false;
				}

				// Comprobar si en la tabla tenemos esa direccion con mayor coste
				if (rutaTabla.getCoste() > rutaNueva.getCoste()) {
					return true;
				}

				// Comprobar si tenemos esa direccion con mismo next hop y mayor coste
				if (rutaTabla.getNextHop().compareTo(rutaNueva.getNextHop()) == 0 && rutaTabla.getCoste() != rutaNueva.getCoste()) {
					return true;
				}
			}
		}
		if (!existeEnTabla) {// Si no existe en la tabla se anhade
			return true;
		}
		// Si no se cumplen
		return false;
	}

	/**
	 * Actualiza el atributo timer de las entradas de la tabla que acaban de llegar de nuevo
	 * @param tabla
	 * @param rutaNueva
	 * @return
	 */
	public Boolean actualizarTimer(TreeMap<String, Ruta> tabla, Ruta rutaNueva) {
		Set<String> setTabla = tabla.keySet();
		Iterator<String> it = setTabla.iterator();
		Ruta rutaTabla = null;
		while (it.hasNext()) {
			rutaTabla = tabla.get(it.next());

			if (rutaTabla.getDireccionIP().compareTo(rutaNueva.getDireccionIP()) == 0) {
				if (rutaTabla.getCoste() == rutaNueva.getCoste()) {
					if (rutaTabla.getNextHop().compareTo(rutaNueva.getNextHop()) == 0 && rutaTabla.getTimer() < rutaNueva.getTimer()) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Devuelve la cabecera del formato RIPv2
	 * @return
	 */
	public static ByteBuffer construirCabecera() {
		ByteBuffer cabecera = ByteBuffer.allocate(4);
		cabecera.put((byte) 2).put((byte) 2);
		cabecera.rewind();
		return cabecera;
	}

	/**
	 * Devuelve los campos de datos para enviar con el formato y tamaño correctos
	 * @param tabla
	 * @param dirDestino
	 * @return
	 */
	public static ByteBuffer construirPaquete(TreeMap<String, Ruta> tabla, String dirDestino) {
		Set<String> setTabla = tabla.keySet();
		Iterator<String> itTabla = setTabla.iterator();
		ByteBuffer datos = ByteBuffer.allocate(500);
		int i = 0;

		while (itTabla.hasNext()) {
			String key = itTabla.next();
			// Split Horizon
			if (tabla.get(key).getNextHop().compareTo(dirDestino) != 0) {
				datos.putShort((short) 2).putShort((short) 0);
				// Meter IP
				String direccion = tabla.get(key).getDireccionIP().substring(1, tabla.get(key).getDireccionIP().length());
				String[] direccionDividida = direccion.split("\\.");
				datos.put((byte) Integer.parseInt(direccionDividida[0])).put((byte) Integer.parseInt(direccionDividida[1])).put((byte) Integer.parseInt(direccionDividida[2])).put((byte) Integer.parseInt(direccionDividida[3]));

				// Meter mascara
				String mascara = tabla.get(key).getMascara().substring(1, tabla.get(key).getMascara().length());
				String[] mascaraDividida = mascara.split("\\.");
				datos.put((byte) Integer.parseInt(mascaraDividida[0])).put((byte) Integer.parseInt(mascaraDividida[1])).put((byte) Integer.parseInt(mascaraDividida[2])).put((byte) Integer.parseInt(mascaraDividida[3]));

				// Meter Next Hop
				datos.putInt(0);

				// Meter Coste
				datos.putInt(tabla.get(key).getCoste());
				i++;
			}
		}
		datos.rewind();
		byte[] aux = new byte[i * 20];
		datos.get(aux);
		ByteBuffer datosSalida = ByteBuffer.allocate(i * 20);
		datosSalida.put(aux);
		datosSalida.rewind();
		return datosSalida;
	}

	/**
	 * Calcula el tamaño que debe de tener el paquete
	 * @param tabla
	 * @param dirDestino
	 * @return
	 */
	public static int averiguarTamanho(TreeMap<String, Ruta> tabla, String dirDestino) {
		Set<String> setTabla = tabla.keySet();
		Iterator<String> itTabla = setTabla.iterator();
		int i = 0;

		while (itTabla.hasNext()) {
			String key = itTabla.next();
			if (tabla.get(key).getNextHop().compareTo(dirDestino) != 0) {
				i++;
			}
		}
		return (i * 20) + 4;
	}

	/*
	 * Constructores
	 */

	/**
	 * Constructor de vecinos del fichero para añadir a tabla
	 * @param vecino
	 * @param tipo
	 */
	public Ruta(Vecino vecino, String tipo) {
		this.vecino = vecino;
		this.direccionIP = vecino.getDireccion();
		this.mascara = "/255.255.255.255";
		this.len = 32;
		this.nextHop = vecino.getDireccion();
		this.timer = 0;
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}

	/**
	 * Constructor de subredes del fichero para añadir a tabla
	 * @param subred
	 * @param tipo
	 */
	public Ruta(Subred subred, String tipo) {
		this.direccionIP = subred.getDireccion();
		this.mascara = subred.len2int();
		this.len = Integer.parseInt(subred.getLen());
		this.nextHop = subred.getDireccion();
		this.timer = 0;
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}

	/**
	 * Constructor con la informacion de los mensajes entrantes
	 * @param mensajeBits datagrama de entrada
	 * @param i contador de que registro se debe tratar esta vez
	 * @param direccionMensajero direccion IP del vecino que lo ha enviado
	 * @param puertoMensajero puerto del vecino que lo ha enviado
	 * @param timer
	 */
	public Ruta(byte[] mensajeBits, int i, InetAddress direccionMensajero, int puertoMensajero, long timer) {
		this.direccionIP = new String("/" + Byte.toUnsignedInt(mensajeBits[4 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[5 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[6 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[7 + (i * 20)]));
		this.mascara = new String("/" + Byte.toUnsignedInt(mensajeBits[8 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[9 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[10 + (i * 20)]) + "." + Byte.toUnsignedInt(mensajeBits[11 + (i * 20)]));
		this.coste = 1 + (mensajeBits[19 + (i * 20)]);
		this.nextHop = new String(direccionMensajero.toString());
		this.vecino = new Vecino(direccionMensajero + ":" + puertoMensajero);
		this.timer = timer;
	}
}
