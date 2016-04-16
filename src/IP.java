import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Clase para guardar y obtner las direcciones IP
 * @author PB
 *
 */
public class IP {

	private String direccion;
	private String puerto;

	/* METODOS */
	@Override
	public String toString() {
		return direccion + ":" + puerto;
	}

	public String getDireccionETH0() {
		try {
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) e.nextElement();
				Enumeration e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ip = (InetAddress) e2.nextElement();
					if ("wlan0".equals(ni.getName())) { // Cambiar linea por eth0
						return ip.toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "ERROR";
	}

	/* GETTERS Y SETTERS */
	public String getDireccion() {
		return this.direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getPuerto() {
		return this.puerto;
	}

	public void setPuerto(String puerto) {
		this.puerto = puerto;
	}

	/* CONSTRUCTOR */
	public IP() {
	}

	/**
	 * Este constructor asigna direccion y puerto en función de los datos introducidos por el usuario por parámetro
	 * @param args String de entrada del main()
	 */

	public IP(String[] args) {
		if (args.length == 0) {
			this.direccion = this.getDireccionETH0(); // Obtiene y asigna la direccion IP del puerto "eth0"
			this.puerto = "5512";
		} else {
			String[] entrada = args[0].split(":");
			if (entrada.length == 1) {
				this.direccion = entrada[0];
				this.puerto = "5512";
			} else if (entrada.length == 2) {
				this.direccion = entrada[0];
				this.puerto = entrada[1];
			} else {
				this.direccion = "ERROR";
			}
		}
	}
}
