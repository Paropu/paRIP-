
public class PaqueteRIPv2 {
	// Falta cabecera?

	// Pagina 31 de las especificaciones
	// 2 octetos cada uno
	private Byte[] AddressFamilyId; // =2
	private Byte[] RouteTag; // =0
	// 4 octetos cada uno
	private Byte[] IpAddress;
	private Byte[] SubnetMask;
	private Byte[] NextHop; // =0
	private Byte[] Metric;
}
