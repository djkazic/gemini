package net.api;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import io.Downloader;
import io.FileUtils;
import io.block.BlockedFile;

public class Play extends ServerResource {
	
	@Post("application/text")
	public String process(JsonRepresentation entity) {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			JSONObject json = entity.getJsonObject();
			if (json.length() > 0) {
				BlockedFile testBf = FileUtils.getBlockedFile(json.getString("query"));
				if (testBf != null && !testBf.isComplete()) {
					Thread downloadThread = (new Thread(new Downloader(FileUtils.getBlockedFile(json.getString("query")), false)));
					downloadThread.start();
					downloadThread.join();
					//responseJSON.put("value", "downloading");
				}
				return testBf.getPath();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
