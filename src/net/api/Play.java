package net.api;

import java.net.URLEncoder;

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
		//TODO: thread this
		try {
			JSONObject json = entity.getJsonObject();
			if (json.length() > 0) {
				BlockedFile testBf = FileUtils.getBlockedFile(json.getString("query"));
				if (testBf != null && !testBf.isComplete()) {
					Thread downloadThread = (new Thread(new Downloader(FileUtils.getBlockedFile(json.getString("query")), false)));
					downloadThread.start();
					downloadThread.join();
				}
				String path = testBf.getPath();
				path = path.replace("#", "%23");
				URLEncoder.encode(path, "UTF-8")
				                  .replaceAll("\\+", "%20")
				                  .replaceAll("\\%21", "!")
				                  .replaceAll("\\%27", "'")
				                  .replaceAll("\\%28", "(")
				                  .replaceAll("\\%29", ")")
				                  .replaceAll("\\%7E", "~");
				
				String audioEmbed = "<audio id=\"player\" controls=\"\" autoplay=\"\" name=\"media\" style=\"width: 100%\">"
								  + "<source src=\"" + path + "\" type=\"audio/mp3\"></audio>";
				responseJSON.put("value", audioEmbed);
				responseJSON.put("title", "<p>" + FileUtils.removeExtension(testBf.getPointer().getName()) + "</p>");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
