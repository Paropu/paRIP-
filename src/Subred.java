
public class Subred {
	private String direccionIP;
	private String len;

	public String getDireccion() {
		return this.direccionIP;
	}

	public void setDireccion(String direccion) {
		this.direccionIP = direccion;
	}

	public String getLen() {
		return this.len;
	}

	public void setLen(String len) {
		this.len = len;
	}

	// Constructor para subredes del fichero
	public Subred(String subred) {
		String[] separar = subred.split("/");
		this.direccionIP = "/" + separar[0];
		this.len = separar[1];
	}

	// Constructor para anunciar red propia avecinos
	public Subred(Vecino local) {
		this.direccionIP = local.getDireccion();
		this.len = "32";
	}

}
