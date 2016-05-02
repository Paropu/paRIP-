
public class VectorDistancias {

	/*
	 * Direccion IP ----------- (Puerto) ------------ Next Hop --------------- (Mascara) --------------Coste
	 * 
	 */

	private String direccionIP;
	private String NextHop;
	private String mascara;
	private Integer coste;

	public String getDireccionIP() {
		return this.direccionIP;
	}

	public String getMascara() {
		return this.mascara;
	}

	public Integer getCoste() {
		return this.coste;
	}

	/*
	 * Constructores
	 */

	public VectorDistancias(Vecino vecino, String tipo) {
		this.direccionIP = vecino.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
		} else {
			this.coste = 1;
		}
	}

	public VectorDistancias(Subred subred, String tipo) {
		this.direccionIP = subred.getDireccion();
		if (tipo.contains("local")) {
			this.coste = 0;
			this.mascara = "32";
		} else {
			this.coste = 1;
			this.mascara = subred.getLen();
		}

	}
}
