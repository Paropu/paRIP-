public class PaqueteRIPv2 {
	// Cabecera: pag 20 RFC
	/*
	 * 1 octeto
	 * comand:
	 * version:
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

	/*
	 * public Byte[] construirCabecera() {
	 * 
	 * }
	 * 
	 * public Byte[] contruirPaquete(TreeMap<String, Ruta> tabla) {
	 * 
	 * }
	 */
}
