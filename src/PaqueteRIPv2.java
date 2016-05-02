
public class PaqueteRIPv2 {
	// Falta cabecera?

	// 2 octetos
	private Byte[] AddressFamilyId; // =2
	private Byte[] RouteTag; // =0
	// 4 octetos
	private Byte[] IpAddress;
	private Byte[] SubnetMask;
	private Byte[] NextHop; // =0
	private Byte[] Metric;
}
