package packets.requests;

public class RequestTypes {
	
	public static final byte PUBKEY    = 0x02;
	public static final byte MUTEX     = 0x04;
	public static final byte PEERLIST  = 0x06;
	public static final byte SEARCH    = 0x08;
	public static final byte BLOCK     = 0x10;
	public static final byte CACHE     = 0x12;
	public static final byte EXTVIS    = 0x14;
	public static final byte CACHEPULL = 0x16;
	public static final byte HOSTPORT  = 0x18;
	
}
