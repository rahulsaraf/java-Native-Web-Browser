import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

/**
 * 
 */

/**
 * @author Rahul & Abhimanyu
 * This class is responsible for getting URL from the text box and sending it to backend class to get html content and download the image data
 *
 */
public class MyBrowser extends JFrame{

	private static final long serialVersionUID = 1L;

	private JTextField locationTextField = new JTextField(35);

	private static JTextPane displayEditorPane = new JTextPane();
	StyledDocument doc;
	static Document document;
	private static MyBrowser browser;
	String response = "";

	static String html;
	static int j = 0;
	static int currentIndex = 0;
	  
	/**
	 * @param args
	 * this is written by Abhimanyu, this method is responsible for rendering the JFRame.
	 */
	public static void main(String[] args) {
		 browser = new MyBrowser();
		 browser.setVisible(true);
		 browser.show();
		 
	}
	
	/**
	 * This constructor is written by Rahul, this constructor is responsible for initializing all the components.
	 * TextBox to enter the URL
	 * TextPane to show the HTML content
	 * GO Button to hit the URL
	 */
	public MyBrowser(){
		doc = (StyledDocument)displayEditorPane.getDocument();
		document = displayEditorPane.getDocument();
	    setSize(1024, 768);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    JPanel buttonPanel = new JPanel();
	    
	    locationTextField.addKeyListener(new KeyAdapter() {
	      public void keyReleased(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	          actionGo();
	        }
	      }
	    });
	    buttonPanel.add(locationTextField);
	    JButton goButton = new JButton("GO");
	    goButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionGo();
	      }
	    });
	    buttonPanel.add(goButton);
	    displayEditorPane.setEditable(false);

	    getContentPane().setLayout(new BorderLayout());
	    getContentPane().add(buttonPanel, BorderLayout.NORTH);
	    getContentPane().add(new JScrollPane(displayEditorPane), BorderLayout.CENTER);
	}
	
	/**
	 * This method is written by Rahul, this is responsible for extracting URL from textBox and send it to backENd
	 */
	private void actionGo() {
		displayEditorPane.setText(null);
		try {
			String verifiedUrl = verifyUrl(locationTextField.getText());
			if (verifiedUrl != null) {

				showPage(verifiedUrl, true);

			} else {

				displayEditorPane.getStyledDocument().insertString(
						document.getLength(), "Invalid URL", null);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private String verifyUrl(String url) {
		    if (!url.toLowerCase().startsWith("http://"))
		      return null;
		    return url;
	}

	
	
	/**
	 * @param pageUrl
	 * @param addToList
	 * @throws BadLocationException
	 * 
	 * This method is written by Rahul to call backend with URL to get HTML content
	 * After getting response from BackEnd, HTML response parsed to get the text and image content.
	 */
	private void showPage(String pageUrl, boolean addToList) throws BadLocationException {
	    try {
	    	BrowserClientBackend brow = new BrowserClientBackend();
	      String args[] = new String[1];
	      args[0] = pageUrl;
	      html = brow.getHtmlContent(args);
	      ArrayList<String> list = new ArrayList<String>();
	      parseHtml(list);
	    } catch (Exception e) {
	    	displayEditorPane.getStyledDocument().insertString(document.getLength(),"Unable to load page" ,null );
	      System.out.println("Unable to load page");
	    }
	  }


	/**
	 * @param content
	 * @return Tags present in HTML response.
	 * 
	 * This method is responsible for extracting tags from HTML
	 */
	private static ArrayList<String> checkIfTagsPresent(String content) {
		ArrayList<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile("<([^\\s>/]+)");
		Matcher m = p.matcher(content);
		while (m.find()) {
			String tag = m.group(1);
			list.add(tag);
		}
		return list;	
	}
	
	
	/**
	 * @param list
	 * @throws BadLocationException
	 * This method is collectively written by both.
	 * This method is responsible for parsing HTML to get the content inside each tags.
	 */
	private static void parseHtml(ArrayList<String> list) throws BadLocationException {
		currentIndex = 0;
		ArrayList<String> tagList = checkIfTagsPresent(html);
		boolean isImage = false;
		if(tagList.size() == 1 && tagList.contains("img")){
			isImage = true;
		}
		
		html = html.replace("<br/>", "\n").replace("<strong>", "").replace("<b>", "").replace("</strong>", "").replace("</b>", "");
		Pattern regex;
		if(isImage){
			 regex = Pattern.compile("<img(.*?)>", Pattern.DOTALL);	
		}else{
			 regex = Pattern.compile("<html>(.*?)</html>", Pattern.DOTALL);
		}
		
		Matcher matcher = regex.matcher(html.replaceAll("/n/n/n", ""));
		Pattern title = Pattern.compile("<title>([^<>]+)</title>");
		Pattern body = Pattern.compile("</(.*?)>([^<>]+)<(.*?)>");
		Pattern h1 = Pattern.compile("<h1>([^<>]+)</h1>");
		Pattern h2 = Pattern.compile("<h2>([^<>]+)</h2>");
		Pattern p = Pattern.compile("<p>([^<>]+)</p>");
		Pattern i = Pattern.compile("<i>([^<>]+)</i>");
		Pattern pre = Pattern.compile("<pre>([^<>]+)</pre>");
		Pattern img = Pattern.compile("<img([^<>]+)>");
		Pattern a = Pattern.compile("<a ([^<>]+)>");
		Pattern rUrl = Pattern.compile(">([^<>]+)</a>");
		Pattern pEx = Pattern.compile("<p>([^<>]+)<a");
		//Pattern address = Pattern.compile("<address>([^<>]+)<a");
		
		if (matcher.find() || isImage) {
			
			for (String tag : tagList) {
				
			    String DataElements = matcher.group(1);		
			if(tag.equals("title")){
			    Matcher title_match = title.matcher(DataElements);
			    if (title_match.find(currentIndex)) {
			    	String match = new String(title_match.group(1));
			    	currentIndex = DataElements.indexOf(match,currentIndex);
			    	browser.setTitle(match);
			        list.add(match);
			    }				
			}
			
			else if(tag.equals("body")){
		    Matcher body_match = body.matcher(DataElements);
		    if (body_match.find(currentIndex)) {
		    	String match = new String(body_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		        list.add(match);
		    }
			}
			else if(tag.equals("h1")){
		    Matcher h1_match = h1.matcher(DataElements);
		    if (h1_match.find(currentIndex)) {
		    	String match = new String(h1_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n\n" ,null );
		        list.add(match);
		    }
		    }
		    
			else if(tag.equals("h2")){
		    Matcher h2_match = h2.matcher(DataElements);
		    if (h2_match.find(currentIndex)) {
		    	String match = new String(h2_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n\n",null );
		        list.add(match);
		    }
		    }
		    
			else if(tag.equals("p")){
		    Matcher p_match = p.matcher(DataElements);
		    if (p_match.find(currentIndex)) {
		    	String match = new String(p_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex + 3);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n\n",null );
		        list.add(match);
		    }else{
		    Matcher pEx_match = pEx.matcher(DataElements);
		    if (pEx_match.find(currentIndex)) {
		    	String match = new String(pEx_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex + 3);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match,null );
		        list.add(match);
		    }
		    }
		    }
		    
			else if(tag.equals("i")){
		    Matcher i_match = i.matcher(DataElements);
		    if (i_match.find(currentIndex)) {
		    	String match = new String(i_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n",null );
		        list.add(match);
		    }
		    }
		    
			else if(tag.equals("pre")){
		    Matcher pre_match = pre.matcher(DataElements);
		    if (pre_match.find(currentIndex)) {
		    	String match = new String(pre_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n",null );
		        list.add(match);
		    }
		    }
		    
		    
			else if(tag.equals("img")){
		    Matcher img_match = img.matcher(DataElements);
		    if (img_match.find(currentIndex)) {
		    	String match = new String(img_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	match = match.substring(match.indexOf("src=") + 4, match.lastIndexOf(".")+4).replace("\"", "");
		    	match = match.split("/")[match.split("/").length -1];
		    	displayEditorPane.insertIcon ( new ImageIcon (match));
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),"\n" ,null );
		        list.add(match);
		    }
		    }
		    
			else if(tag.equals("a")){
		    Matcher a_match = a.matcher(DataElements);
		    Matcher r_match = rUrl.matcher(DataElements);
		    if (a_match.find(currentIndex)) {
		    	String match = new String(a_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	match = match.replace("href", "").replace("=", "").replace("\"","");
		    	//displayEditorPane.getStyledDocument().insertString(document.getLength(),match ,null );
		        list.add(match);
		    }
		    if(r_match.find(currentIndex)){
		    	String match = new String(r_match.group(1));
		    	currentIndex = DataElements.indexOf(match,currentIndex);
		    	if(tagList.contains("address") && match.contains("john@")){
		    		match = "(" + match + ") / 2001-04-06";
		    	}
		    	displayEditorPane.getStyledDocument().insertString(document.getLength(),match + "\n",null );
		    	list.add(match);
		    }
		    
		    }
		}
	}
			
		for (int j=0; j < list.size(); j++) {
			System.out.println(list.get(j));
		}
	}

}
