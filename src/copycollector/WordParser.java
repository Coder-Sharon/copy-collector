package copycollector;

import java.util.ArrayList;

import org.openqa.selenium.WebElement;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.STBrType;

/**
 * The WordParser class is called by the CopyCollector to parse the HTML from a web page and format it to
 * produce a readable Word document. The class provides functions to store images in the word file, and
 * style various HTML elements appropriately.
 * @author Sharon Tartarone
 */
public class WordParser {
	
	/** Word document to insert copy of web page and format */
	private WordprocessingMLPackage wordDoc;	
	
	/** Sets up new instance of WordParser 
	 * @param String pageTitle name of web page to use as header name
	 * @param screenshot of web page as bytes
	 * @param ArrayList webElements all HTML elements on web page*/
	public WordParser(String pageTitle, byte[] screenshot, ArrayList<WebElement> webElements,
			WordprocessingMLPackage wordDoc) {
		this.wordDoc = wordDoc;
		setTitle(pageTitle);
		addImage(screenshot);
		parseAll(webElements);
		addPageBreak();
	}
	
	/** Iterates through all HTML elements on page and calls function to style element. After styling
	 * each element, the thread is forced to sleep to prevent socket exhaustion
	 * @param ArrayList webElements all HTML elements on web page */
	private void parseAll(ArrayList<WebElement> webElements) {
		for (WebElement thisElement : webElements) {
			styleElement(thisElement);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				UserInterface.errorPopup("Error occured when thread tried to sleep."
						+ "Please contact program admin if this occurs.");
			}
		}
	}
	
	/** Styles certain HTML elements on page and adds them to the Word document. HTML tags determine the 
	 * appearance of the text in the Word document.
	 * THESE CAN BE MODIFIED AND UPDATED FOR MORE ROBUST HTML TAGS
	 * @param WebElement thisElement the element to style and add to document */
	private void styleElement(WebElement thisElement) {
		String thisTag = thisElement.getTagName();
		String thisText = thisElement.getText();
			
			 if(thisText == "")				{ ; } //do nothing
		else if(thisTag.equals("h1"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
		else if(thisTag.equals("h2"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
		else if(thisTag.equals("h3"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
		else if(thisTag.equals("h4"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
		else if(thisTag.equals("h5"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
		else if(thisTag.equals("h6"))		{ wordDoc.getMainDocumentPart().addStyledParagraphOfText("Subtitle", thisText); }
			 
		else if(thisTag.equals("p"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("a"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }	 
		else if(thisTag.equals("ol"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("ul"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("td"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }	 
		else if(thisTag.equals("button"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }	 
		else if(thisTag.equals("dialog"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("iframe"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }	 
		else if(thisTag.equals("form"))		{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("input"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("label"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("option"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
		else if(thisTag.equals("select"))	{ wordDoc.getMainDocumentPart().addParagraphOfText(thisText); }
			 
			// <a>
			// <br>
			// <div>

	}
	
	/** Sets header of each page in document based on web page title
	 * @param String pageTitle web page title to use as header */
	private void setTitle(String pageTitle){
		wordDoc.getMainDocumentPart().addStyledParagraphOfText("Title", pageTitle);
	}
	
	/** Gets page screenshot as bytes in order to add to Word document
	 * Source: http://blog.iprofs.nl/2012/10/22/adding-images-and-layout-to-your-docx4j-generated-word-documents-part-1/
	 * @param screenshot of web page as bytes
	 * @author lvdpal */
	private void addImage(byte[] screenshot) {
		BinaryPartAbstractImage imagePart = null;
		try {
			imagePart = BinaryPartAbstractImage.createImagePart(wordDoc, screenshot);
		} catch (Exception e) {
			UserInterface.errorPopup("Error occuured when screenshot is made into image."
					+ "Please contact program admin if this occurs.");
		}
	  
		int docPrId = 1, cNvPrId = 2;
		Inline inline = null;
		try {
			inline = imagePart.createImageInline("Filename hint", "Alternative text", docPrId, cNvPrId, false);
		} catch (Exception e) {
			UserInterface.errorPopup("Error occured when image was put into Word document."
					+ "Please contact program admin if this occurs.");
		}
	  
		P paragraph = addInlineImageToParagraph(inline);
	  
	    wordDoc.getMainDocumentPart().addObject(paragraph);
	}
	
	/** Adds image to paragraph in Word document
	 * Source: http://blog.iprofs.nl/2012/10/22/adding-images-and-layout-to-your-docx4j-generated-word-documents-part-1/
	 * @param Inline location to place image in document
	 * @author lvdpal */
	private P addInlineImageToParagraph(Inline inline) {
        P paragraph = new P();
        R run = new R();
        paragraph.getContent().add(run);
        Drawing drawing = new Drawing();
        run.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        return paragraph;
    }
	
	/** Adds page break to Word document
	 * Source: http://blog.iprofs.nl/2012/10/22/adding-images-and-layout-to-your-docx4j-generated-word-documents-part-1/
	 * @author lvdpal */
	private void addPageBreak() {
        MainDocumentPart documentPart = wordDoc.getMainDocumentPart();
 
        Br breakObj = new Br();
        breakObj.setType(STBrType.PAGE);
 
        P paragraph = new P();
        paragraph.getContent().add(breakObj);
        documentPart.getJaxbElement().getBody().getContent().add(paragraph);
    }
}