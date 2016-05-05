import java.nio.ByteBuffer;

public class PaqueteRIPv2 {
	// Cabecera: pag 20 RFC
	/*
	 * 1 octeto
	 * comand: =2 // respuesta |||| =1 para request
	 * version: =2
	 * 
	 * 2 octetos
	 * contraseña =0
	 */

	// Campos: pag 31 RFC
	// 2 octetos cada uno
	private Byte[] AddressFamilyId; // =2
	private Byte[] RouteTag; // =0
	// 4 octetos cada uno
	private Byte[] IpAddress;
	private Byte[] SubnetMask;
	private Byte[] NextHop; // =0
	private Byte[] Metric;

	/*
	 * mensaje = paqueteRIPv2.construirCabecera();
	 * it
	 * i =20;
	 * while(){
	 * put(construirpaquete(),i,20)
	 * i +=20;
	 * }
	 */

	public static ByteBuffer construirCabecera() {
		ByteBuffer cabecera = ByteBuffer.allocate(4);
		cabecera.put((byte) 2).put((byte) 2);
		cabecera.rewind();
		return cabecera;
	}

	public ByteBuffer construirPaquete(Ruta ruta) {
		ByteBuffer datos = ByteBuffer.allocate(20);
		datos.putShort((short) 2).putShort((short) 0);
		// Meter IP

		// Meter mascara
		String mascara = ruta.getMascara().substring(1, ruta.getMascara().length());
		String[] mascaraDividida = mascara.split("\\.");
		datos.put((byte) Integer.parseInt(mascaraDividida[0])).put((byte) Integer.parseInt(mascaraDividida[1])).put((byte) Integer.parseInt(mascaraDividida[2])).put((byte) Integer.parseInt(mascaraDividida[3]));

		// Meter Next Hop
		datos.putInt(0);

		// Meter Coste
		datos.putInt(ruta.getCoste());
		datos.rewind();
		return datos;
	}

	public PaqueteRIPv2() {

	}
}
