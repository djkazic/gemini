package packets.data;

public class DataTypes {

	public static final byte PUBKEY_DATA    = 0x01;
	public static final byte MUTEX_DATA     = 0x03;
	public static final byte PEERLIST_DATA  = 0x05;
	public static final byte SEARCH_DATA    = 0x07;
	public static final byte BLOCK_DATA     = 0x09;
	public static final byte CACHE_DATA     = 0x11;
	public static final byte EXTVIS_DATA    = 0x13;
	public static final byte CACHEPULL_DATA = 0x15;
	public static final byte HOSTPORT_DATA  = 0x17;

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
