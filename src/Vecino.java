import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Clase con los atributos, metodos y constructores para trabajar con los routers que están a distancia 1
 *
 */
public class Vecino {
	private String direccionIP;
	private Integer puerto;

	public String getDireccion() {
		return this.direccionIP;
	}

	public void setDireccion(String direccion) {
		this.direccionIP = direccion;
	}

	public Integer getPuerto() {
		return this.puerto;
	}

	public void setPuerto(Integer puerto) {
		this.puerto = puerto;
	}

	/**
	 * Devuelve la direccion IPv4 de la interfaz introducida
	 * @return IP de interfaz introducida
	 */
	public String getDireccionETH0(String interfaz) {
		InetAddress ip = null;
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					ip = e2.nextElement();
					if (interfaz.equals(ni.getName())) { // Cambiar linea por eth0
						if (ip instanceof Inet4Address) {
							return ip.toString();
						}
					}
				}

			}
			System.out.println("ERROR, no se ha encontrado una IP para " + interfaz);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("ERROR, no se ha encontrado una IP para " + interfaz);
			System.exit(0);
		}
		return null;
	}

	/**
	 * Devuelve InetAddress del String introducido
	 * @return direccion IP en forma Inetaddress
	 */
	public InetAddress getInet() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(this.direccionIP.substring(1, this.direccionIP.length()));
		} catch (Exception e) {
		}
		return addr;
	}

	/**
	 * Contructor de objeto Vecino para los datos del propio ordenador
	 * @param args
	 * @param interfaz
	 */
	public Vecino(String[] args, String interfaz) {
		if (args.length == 0) { // Si no tiene datos de entrada
			this.direccionIP = this.getDireccionETH0(interfaz); // Obtiene y asigna la direccion IP del puerto dado
			this.puerto = 5512;
		} else {
			String[] entrada = args[0].split(":");
			if (entrada.length == 1) { // Si no tiene un puerto asignado
				this.direccionIP = "/" + entrada[0];
				this.puerto = 5512;
			} else if (entrada.length == 2) {
				this.direccionIP = "/" + entrada[0];
				this.puerto = Integer.parseInt(entrada[1]);
			} else {
				System.out.println("ERROR recogiendo datos en constructor Vecino");
				System.exit(0);
			}
		}
	}

	/**
	 * Constructor para vecinos del archivo
	 * @param vecino
	 */
	public Vecino(String vecino) {
		String[] separar = vecino.split(":");
		this.direccionIP = "/" + separar[0];
		if (separar.length == 2) {
			this.puerto = Integer.parseInt(separar[1]);
		} else {
			this.puerto = 5512;
		}
	}

	/**
	 * Constructor para vecinos recibidos que no están originalmente en el archivo
	 * @param ruta
	 */
	public Vecino(Ruta ruta) {
		this.setDireccion(ruta.getDireccionIP());
		this.setPuerto(ruta.getVecino().getPuerto());
	}
}