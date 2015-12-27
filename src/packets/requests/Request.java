package packets.requests;

public class Request {

	private byte type;
	private Object payload;
	
	public Request() {
		type = 0x00;
		payload = null;
	}
	
	public Request(byte type, Object payload) {
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