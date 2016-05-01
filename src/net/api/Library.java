package net.api;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import atrium.Core;
import io.FileUtils;
import io.block.BlockedFile;

public class Library extends ServerResource {
	
	@Post("application/text")
	public String process(JsonRepresentation entity) {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			JSONObject json = entity.getJsonObject();
			if (json.length() > 0) {
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("<div class=\"panel panel-default lib-panel\">");
					sb.append("<table class=\"table table-hover\" style=color:#333>");
					sb.append("<thead><tr><th>#</th><th>TRACK</th><th>FILENAME</th></tr></thead>");

					sb.append("<tbody>");

					int i = 0;
					for (BlockedFile bf : Core.blockDex) {
						if (bf.isComplete()) {
							sb.append("<tr>");
							sb.append("<td class=\"td-minus\">" + (i + 1) + "</td>");
							sb.append("<td class=\"td-plus res-play\" id=\"" + bf.getChecksum() + "\">");
							sb.append("<a href=\"#\">");
							sb.append("<i class=\"fa fa-play-circle-o\"></i>");
							sb.append("</a>");
							sb.append("</td>");
							sb.append("<td>");
							sb.append(FileUtils.removeExtension(bf.getPointer().getName()));
							sb.append("</td>");
							sb.append("</tr>");
						}
						i++;
					}

					sb.append("</tbody>");
					sb.append("</table>");
					sb.append("</div>");
					responseJSON.put("value", sb.toString());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
