
public class Subred {
	private String direccionIP;
	private String len;

	public String getDireccion() {
		return this.direccionIP;
	}

	public void setDireccion(String direccion) {
		this.direccionIP = direccion;
	}

	public String GetLen() {
		return this.len;
	}

	public void setLen(String len) {
		this.len = len;
	}

	public Subred(String subred) {
		String[] separar = subred.split("/");
		this.direccionIP = "/" + separar[0];
		this.len = separar[1];
	}

}
