package requests;

public class Request {

	private String type;
	private Object payload;
	
	public Request(String type, Object payload) {
		this.type = type;
		this.payload = payload;
	}
	
	public String getType() {
		return type;
	}
	
	public Object getPayload() {
		return payload;
	}
}