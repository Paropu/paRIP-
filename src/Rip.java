public class Rip {

	/*
	 * 1º Procesar argumento de entrada, u obtener la IP del ¿interna?
	 * - interfaz eth0 IP asociada
	 * 2º Nombre del archivo: "ripconf-A.B.C.D topo"
	 * - leer y procesar este fichero
	 * 172.19.45.9:4444 <-- IPs vecinos + puerto
	 * ------
	 * ------
	 * I.J.K.L/25 <-- Subred finalizada
	 * 3º Crear sockets.[UDP]
	 * - Configurar un tier en el socket
	 * DatagramSocket
	 * do{
	 * try{
	 * receive(); //Proceso paquete, Actualizo tabla;
	 * } catch{ //Gestiona el vencimiento del temporizador
	 * //Envía update a vecino
	 * //¿Reset? temporizador
	 * }
	 * } while (true);
	 * 
	 * Bellman-Ford!!!
	 */

	public static void main(String[] args) {
		// Comprobar datos de entrada
		IP ip = new IP(args);
		if (ip.getDireccion().equals("ERROR")) {
			System.out.println("Argumentos incorrectos");
			return;
		}
		ip.toString();
		System.out.println(ip.getDireccion() + " " + ip.getPuerto());
	}
}
