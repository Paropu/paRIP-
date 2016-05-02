
public class VectorDistancias {

	/*
	 * Pagina 8 de las especificaciones
	 * - address: in IP implementations of these algorithms, this will be the IP address of the host or network.
	 * - router: the first router along the route to the destination.
	 * - interface: the physical network which must be used to reach the first router. ¿Esta se puede obviar?
	 * - metric: a number, indicating the distance to the destination.
	 * - timer: the amount of time since the entry was last updated.
	 */

	private String direccionIP;
	private String nextHop; // DireccionIP???
	private Integer coste;

	@Override
	public String toString() {
		return this.getDireccionIP() + "\t\t" + this.nextHop + "\t\t" + this.coste;
	}

	public String getDireccionIP() {
		return this.direccionIP;
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
		this.nextHop = vecino.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}

	public VectorDistancias(Subred subred, String tipo) {
		this.direccionIP = subred.getDireccion();
		this.nextHop = subred.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}

	}
}
