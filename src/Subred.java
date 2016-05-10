
/**
 * Clase con los atributos, metodos y constructores para trabajar con las subredes que están a distancia 1
 *
 */
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

	/**
	 * Transforma numero entero en String A.B.C.D
	 * @return String con la mascara en el formato A.B.C.D
	 */
	public String len2int() {
		int unos = Integer.parseInt(this.len);
		String mascara = "/";
		if(unos>32) {
			System.out.println("ERROR en formato de mascara");
			System.exit(0);
		}
		for (int i = 4; i != 0; i--) {
			if (unos >= 8) {
				mascara = mascara.concat("255.");
				unos -= 8;
				continue;
			}
			if (unos == 7) {
				mascara = mascara.concat("254.");
				unos -= 7;
				continue;
			}
			if (unos == 6) {
				mascara = mascara.concat("252.");
				unos -= 6;
				continue;
			}
			if (unos == 5) {
				mascara = mascara.concat("248.");
				unos -= 5;
				continue;
			}
			if (unos == 4) {
				mascara = mascara.concat("240.");
				unos -= 4;
				continue;
			}
			if (unos == 3) {
				mascara = mascara.concat("224.");
				unos -= 3;
				continue;
			}
			if (unos == 2) {
				mascara = mascara.concat("192.");
				unos -= 2;
				continue;
			}
			if (unos == 1) {
				mascara = mascara.concat("128.");
				unos -= 1;
				continue;
			}
			if (unos == 0) {
				mascara = mascara.concat("0.");
			}
		}
		mascara = mascara.substring(0, mascara.length() - 1);
		return mascara;
	}

	/**
	 * Constructor para subredes del fichero
	 * @param subred
	 */
	public Subred(String subred) {
		String[] separar = subred.split("/");
		this.direccionIP = "/" + separar[0];
		this.len = separar[1];
	}
}
