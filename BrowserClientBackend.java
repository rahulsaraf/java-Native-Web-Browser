import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Rahul & Abhimanyu
 * this class is responsible for providing html content along with downloading image data.
 * as input this class gets URL and it downloads the image and sends the html content.
 *
 */
public class BrowserClientBackend {

	// socket and Input & Output Stream
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	
	private InputStream inputStream;
	private static String URL;
	private static String host;
	private static int port;
	private static boolean htmlPage;
	
	private static String currentHtmlDir;

	public static String DELIMITER = "/";
	public static String HTTP = "http://";
	//page not found
	public static String errorPage = "<html><title>Page Not Found</title><body><p>The requested URL was not found on this server</p></body></html>";

	
	/**
	 * @param args
	 * @return html content of the page
	 * 
	 * This method is written by Abhimanyu
	 * This method is responsible for taking the URL as argument and it gives html content of the page
	 */
	public String getHtmlContent(String args[] ){
		htmlPage = false;currentHtmlDir=null;host = null;
		
		if (args.length < 1) {
			System.out.println("Usage:  client hostname port");
			System.exit(1);
		}

		URL = args[0];		
		getHostName(URL);
		if (args.length == 1 && URL.lastIndexOf(":") < 5) {
			port = 80;
		} else if(URL.contains(":") && !(URL.lastIndexOf(":") < 5)){
			String portString = URL.substring(URL.lastIndexOf(":") + 1, URL.length());
			port = Integer.valueOf(portString.replace(DELIMITER, ""));
			host = URL.replace(portString, "").replace(HTTP, "").replace(":", "").split(DELIMITER)[0];
		}else{
			port = Integer.valueOf(args[1]);
		}
		if (!URL.endsWith("html") && URL.endsWith(DELIMITER)) {
			URL = URL + "index.html";
		}
		if(URL.endsWith("html")){
			htmlPage = true;
		}
		currentHtmlDir = URL.replace(URL.split(DELIMITER)[URL.split(DELIMITER).length - 1], "");
		currentHtmlDir = currentHtmlDir.replace(host, "").replace(HTTP, "").replace(":", "").replace(String.valueOf(port), "");
		BrowserClientBackend client = new BrowserClientBackend();
		client.listenSocket(host, port);
		return client.communicate();
	}
	
	/**
	 * @param url
	 * @return hostName
	 * This method is responsible for extracting hostname from URL
	 * This method is written by Rahul to get hostName
	 */
	private static String getHostName(String url) {
		
		url = url.replaceFirst(HTTP, "");
		String[] links = url.split(DELIMITER);
		if(host == null){
			host = links[0];	
		}
		return links[0];
	}

	/**
	 * @return HtmlContent
	 * This method is written by Rahul
	 */
	public String communicate() {
		String htmlResponse = null;
		try {
			//this section is written by Abhimanyu to extract html from host server
			if (htmlPage) {
				htmlResponse = getHTMLContent(URL, null, false);
				if(htmlResponse.contains("HTTP/1.1 404 Not Found")){
					htmlResponse = errorPage;
				}else{
					htmlResponse = htmlResponse.substring(htmlResponse.indexOf("/n/n/n/n") + 4, htmlResponse.length());
					String[] lines = htmlResponse.split("/n/n");
					for (String line : lines) {
						parseLine(line);
						System.out.println(line);
					}
					htmlResponse = htmlResponse.replace("/n/n", "/n/n/n");
				}
				
			}
			//this section is written by Rahul to extract image from host server
			 else {
				String fileName = URL.split("/")[URL.split("/").length - 1];
				htmlResponse = getHTMLContent(URL, fileName, true);
				htmlResponse = "<img src ="+URL+">";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return htmlResponse;
		
	}

	/**
	 * @param urlPath
	 * @param fileName
	 * @param isImage
	 * @return htmlContent
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * This method is written by Rahul and Abhimanyu together, one part gets the html content and other part downloads the image
	 */
	private String getHTMLContent(String urlPath, String fileName,Boolean isImage) throws IOException, ClassNotFoundException {
		String request = null;
		
		request = createHttpRequest(urlPath, fileName, isImage);
		//request for image section
		if(isImage && urlPath.contains(HTTP)){
			initialize(getHostName(urlPath), port);
			out.println(request);
			out.println("Host:" + getHostName(urlPath) +":"+port+ "\r\n\r\n");	
		}
		//request for html section
		else{
			initialize(host, port);
			out.println(request);
			out.println("Host:" + host +":"+port+ "\r\n\r\n");
		}
		
		out.flush();
		return readResponse(fileName, isImage);
	}

	/**
	 * @param urlPath
	 * @param fileName
	 * @param isImage
	 * @return httpRequest
	 * 
	 * this method is written by Rahul to create a request string to get image/ html from host server
	 * 
	 */
	private String createHttpRequest(String urlPath, String fileName,Boolean isImage) {
		String request = "/";
		if(!isImage){
			request = urlPath.replace(HTTP,"").replace(host, "").replace(":", "").replace(String.valueOf(port), "").replace("index.html", "");
		}else{
			request = urlPath;
		}
		return "GET " + request + " HTTP/1.0";
	}

	/**
	 * @param line
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * this method is written by Rahul, to extract image information from Html content. after getting the image tag, this method calls the method with image info
	 * such as image URL, host
	 */
	private void parseLine(String line) throws IOException,
			ClassNotFoundException {
		String path;
		String imageName;
		if (line.contains("<img")) {
			String[] contents = line.split(" ");
			for (String content : contents) {
				if (content.startsWith("src")) {
					path = content.replace("src=", "").replace("\"", "").replace(">", "");
					if(path.contains(DELIMITER) || path.contains(HTTP)){
						imageName = path.split(DELIMITER)[path.split(DELIMITER).length - 1];	
					}else{
						imageName = path;
					}
					if (path.contains(HTTP)) {
						
						getHTMLContent(path, imageName, true);
					} else {
						getHTMLContent(currentHtmlDir+path, imageName, true);
					} 
				}
			}
		}
	}

	/**
	 * @param host
	 * @param port
	 * this method is written by Rahul
	 * this is to initialize the socket connection by providing host and port.
	 */
	public void listenSocket(String host, int port) {
		try {
			initialize(host, port);
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("No I/O");
			System.exit(1);
		}
	}

	/**
	 * @param imageName
	 * @param isImage
	 * @return htmlContent
	 * @throws IOException
	 * this method is written by Abhimanyu to get html content from input stream, whereas Rahul wrote the section to read image from socket.
	 */
	private String readResponse(String imageName, Boolean isImage)
			throws IOException {
		if (!isImage) {
			StringBuilder builder;
			builder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				builder.append("/n/n");
				builder.append(line);
			}
			return builder.toString();
		} else {
			extractImage(imageName);
			return null;
		}
	}

	/**
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SocketException
	 * 
	 * this method is written by Rahul to initialize the socket connection, input and output stream
	 */
	private void initialize(String host, int port) throws UnknownHostException,IOException, SocketException {
		socket = new Socket(host, port);
		//socket = new Socket("portquiz.net", 8080);
		socket.setKeepAlive(true);
		out = new PrintWriter(socket.getOutputStream(), true);
		inputStream = socket.getInputStream();
		in = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	/**
	 * @param buffer
	 * @param count
	 * @return index of the header seperation
	 * 
	 * this method is written by Rahul to get index of the end of http header so that image data can be retrieved after that.
	 */
	private int getIndex(byte[] buffer, int count){
		String str = new String(buffer, 0, count);
		int indexOfEOH = str.indexOf("\r\n\r\n");
		return indexOfEOH + 4;
	}
	
	
	/**
	 * @param imageName
	 * this method is written by Rahul to extract image from input stream.
	 * This method is responsible to get index of the characters that seperates header info from image data and after that from that
	 * position it reads the image data.
	 */
	public void extractImage(String imageName){
		DataInputStream in;
		try {
			in = new DataInputStream(socket.getInputStream());
			OutputStream dos = new FileOutputStream(imageName);
			int count,start;
			byte[] buffer = new byte[2048];
			byte[] modBuffer = new byte[2048];
			boolean endOfHeaderFound = false;
			while ((count = in.read(buffer)) != -1) {
				if(!endOfHeaderFound){
					start = getIndex(buffer, count);
					endOfHeaderFound = true;
					for (int i = 0; start + i < count; i++) {
						modBuffer[i] = buffer[start + i];
					}
					dos.write(modBuffer, 0, count - start);
					dos.flush();
				}else{					
					for (int i = 0; i < buffer.length; i++) {
						modBuffer[i] = buffer[i];
					}
					dos.write(modBuffer, 0, count);
					dos.flush();
				}
			}
			dos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
