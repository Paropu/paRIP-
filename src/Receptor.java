import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Receptor {
	public Receptor(InetAddress dirIP, Integer puerto) {
		byte[] buffer = new byte[50];
		try {
			DatagramSocket socket = new DatagramSocket(puerto, dirIP); // Asigno el puerto por el que van a entrar los datagramas
			DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, dirIP, puerto);
			// SocketAddress addr = new SocketAddress() { };
			/// socket.connect(addr); //Creo que debemos usarlo para que solo reciba en ese puerto
			socket.receive(datagram);
			String mensaje = new String(buffer);
			System.out.println(mensaje);
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
