package net.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public final class HttpRequest implements Runnable {

	final static String CRLF = "\r\n";
	Socket socket;

	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	public void run() {
		try {
			processRequest();
		} catch (Exception ex) {}
	}

	private void processRequest() throws Exception {
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(
		    socket.getOutputStream());

		BufferedReader br = new BufferedReader(
		    new InputStreamReader(is));

		String requestLine = br.readLine();

		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();
		String fileName = tokens.nextToken();
		if(fileName.equals("/")) {
			fileName += "index.html";
		}
		fileName = "web" + fileName;

		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if(fileExists) {
			statusLine = "HTTP/1.1 200 OK: ";
			contentTypeLine = "Content-Type: " +
			                  contentType(fileName) + CRLF;
		} else {
			statusLine = "HTTP/1.1 404 Not Found: ";
			contentTypeLine = "Content-Type: text/html" + CRLF;
			entityBody = "<html>"
						+ "<title>Oops!</title>"
						+ "File Not Found on Internal Server"
						+ "</html>";
		}

		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		os.writeBytes(CRLF);

		if(fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}
		
		os.close();
		br.close();
		socket.close();
	}

	private static void sendBytes(FileInputStream fis, OutputStream os)
	throws Exception {
		byte[] buffer = new byte[1024];
		int bytes = 0;

		while((bytes = fis.read(buffer)) != -1 ) {
			os.write(buffer, 0, bytes);
		}
	}
	private static String contentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html"))
			return "text/html";
		else if(fileName.endsWith(".jpg"))
			return "text/jpg";
		else if(fileName.endsWith(".gif"))
			return "text/gif";
		else if(fileName.endsWith(".gif"))
			return "image/gif";
		else if(fileName.endsWith(".svg"))
			return "image/svg+xml";
		else if(fileName.endsWith(".css"))
			return "text/css";
		else if(fileName.endsWith(".js"))
			return "application/javascript";
		else if(fileName.endsWith(".woff"))
			return "application/font-woff";
		
		return "application/octet-stream";
	}
}