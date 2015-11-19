package data;

public class Data {

	private String type;
	private Object payload;
	
	public Data(String type, Object payload) {
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