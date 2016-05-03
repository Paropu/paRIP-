
public class VectorDistancias {

	/*
	 * Pagina 8 de las especificaciones
	 * - address: in IP implementations of these algorithms, this will be the IP address of the host or network.
	 * - router: the first router along the route to the destination.
	 * - metric: a number, indicating the distance to the destination.
	 * 
	 * - timmer y interface NO SE IMPLEMENTAN
	 */

	private String direccionIP;
	private String mascara;
	private String nextHop; // DireccionIP???
	private Integer coste;

	@Override
	public String toString() {
		return this.getDireccionIP() + "\t\t" + this.getMascara() + "\t\t" + this.nextHop + "\t\t" + this.coste;
	}

	public String getDireccionIP() {
		return this.direccionIP;
	}

	public String getMascara() {
		return this.mascara;
	}

	public String getNextHop() {
		return this.nextHop;
	}

	public Integer getCoste() {
		return this.coste;
	}

	/*
	 * Constructores
	 */

	public VectorDistancias(Vecino vecino, String tipo) {
		this.direccionIP = vecino.getDireccion();
		this.mascara = "/255.255.255.255";
		this.nextHop = vecino.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}

	public VectorDistancias(Subred subred, String tipo) {
		this.direccionIP = subred.getDireccion();
		this.mascara = subred.len2int();
		this.nextHop = subred.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}
}
