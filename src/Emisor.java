import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Emisor {
	public Emisor(InetAddress IPdestino, Integer puertoDestino) {
		String mensaje = new String("hola");
		byte[] buffer = mensaje.getBytes();
		try {
			DatagramSocket socket = new DatagramSocket();
			// socket.connect(IPdestino, puertoDestino);
			DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, IPdestino, puertoDestino);
			socket.send(datagrama);
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}