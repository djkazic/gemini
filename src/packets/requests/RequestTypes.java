package packets.requests;

public class RequestTypes {

	public static final byte PUBKEY_REQS    = 0x02;
	public static final byte MUTEX_REQS     = 0x04;
	public static final byte PEERLIST_REQS  = 0x06;
	public static final byte SEARCH_REQS    = 0x08;
	public static final byte BLOCK_REQS     = 0x10;
	public static final byte CACHE_REQS     = 0x12;
	public static final byte EXTVIS_REQS    = 0x14;
	public static final byte CACHEPULL_REQS = 0x16;
	public static final byte HOSTPORT_REQS  = 0x18;

}
