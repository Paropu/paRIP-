import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Vecino {
	private String direccionIP;
	private String puerto;

	// Se puede modificar toString() si queremos

	public String getDireccion() {
		return this.direccionIP;
	}

	public void setDireccion(String direccion) {
		this.direccionIP = direccion;
	}

	public String getPuerto() {
		return this.puerto;
	}

	public void setPuerto(String puerto) {
		this.puerto = puerto;
	}

	/**
	 * 
	 * @return IP de interfaz introducida
	 */
	public String getDireccionETH0() {
		String interfaz = "wlan0";
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ip = e2.nextElement();
					if (interfaz.equals(ni.getName())) { // Cambiar linea por eth0
						return ip.toString();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR, no se ha encontrado una IP para " + interfaz);
			System.exit(0);
		}
		return null;
	}

	/**
	 * 
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

	public Vecino(String[] args) { // Constructor local
		if (args.length == 0) { // Si no tiene datos de entrada
			this.direccionIP = this.getDireccionETH0(); // Obtiene y asigna la direccion IP del puerto "eth0"
			this.puerto = "5512";
		} else {
			String[] entrada = args[0].split(":");
			if (entrada.length == 1) { // Si no tiene un puerto asignado
				this.direccionIP = "/" + entrada[0];
				this.puerto = "5512";
			} else if (entrada.length == 2) {
				this.direccionIP = "/" + entrada[0];
				this.puerto = entrada[1];
			} else {
				System.out.println("ERROR recogiendo datos en constructor Vecino");
				System.exit(0);
			}
		}
	}

	public Vecino(String vecino) { // Constructor para vecinos del archivo
		String[] separar = vecino.split(":");
		this.direccionIP = "/" + separar[0];
		if (separar.length == 2) {
			this.puerto = separar[1];
		} else {
			this.puerto = "5512";
		}
	}
}