package packets.data;

public class Data {

	private byte type;
	private Object payload;
	
	public Data() {
		type = 0x00;
		payload = null;
	}
	
	public Data(byte type, Object payload) {
		this.type = type;
		this.payload = payload;
	}
	
	public byte getType() {
		return type;
	}
	
	public Object getPayload() {
		return payload;
	}
}