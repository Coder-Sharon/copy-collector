package copycollector;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.net.URL;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The CopyCollector class drives the functionality of the program. The methods in this class obtain user-
 * specified web sites as a resource, and iterate through these sites to collect copy. The functions create
 * a file structure to store the results of the run, and integrate Selenium WebDriver and Crawler4j crawler
 * to automate the browser and crawl every web page on each specified site. The class instantiates other class
 * objects that function to format the resulting copy file, and save it to the correct folder.
 * @author Sharon Tartarone
 */
public class CopyCollector {
	
	/** Selenium Firefox driver for automation */
	private WebDriver driver;
	
	/** Crawler4j crawler for URL scraping */
	private CrawlerController crawler;
	
	public static final String COPY_FOLDER = UserInterface.directory 
			+ File.separator + "myCopy";					//storage folder name
	private static final String TEXT_FILE_TYPE = ".docx";	//text file type
	private static final String CSS_SELECTOR = "body *";	//CSS selector to find
	private static final int NUM_OF_CRAWLERS = 1;			//number of crawlers
	
	/** Sets up new instance of CopyCollector */
	public CopyCollector() {
		setUpBrowser();
		collectCopy(SiteManager.mySites);
		shutDownBrowser();
	}
	
	/** Instantiates FirefoxDriver */
	private void setUpBrowser() {
		driver = new FirefoxDriver();
	}
	
	/** Shuts down FirefoxDriver and closes browser */
	private void shutDownBrowser() {
		driver.quit();
	}
	
	/** Calls methods to create all storage directories; iterates through ArrayList of URLs
	 * @param ArrayList<URL> user-given sites */
	private void collectCopy(ArrayList<URL> mySites) {
		String copyPath = makeCopyDirectory();
		String sitePath;
		
		for (URL thisSite : mySites) {
			sitePath = makeSiteDirectory(copyPath, thisSite);
			collectCopyFrom(thisSite, sitePath);
		}
	}	
	
	/** Creates copy storage directory with current date as folder name
	 * @return String path name of copy directory */
	private String makeCopyDirectory() {
		String date = new Date().toString().replace(":", ".");
		String copyPath = COPY_FOLDER + File.separator + date;
		new File(copyPath).mkdirs();
		
		return copyPath;
	}
	
	/** Creates site storage directory with name of URL host as folder name
	 * @param Sting copyPath the path name of copy directory
	 * @param URL thisSite the URL of site to name site directory
	 * @return String path name of site directory */
	private String makeSiteDirectory(String copyPath, URL thisSite) {
		String sitePath = copyPath + File.separator + thisSite.getHost();
		new File(sitePath).mkdirs();
		
		return sitePath;
	}
	
	/** Instantiates crawler to collect URLS; calls method that captures images and text for each URL
	 * @param URL thisSite the site to navigate
	 * @param String thisPath the path to store image/text files */
	private void collectCopyFrom(URL thisSite, String sitePath) {
		ArrayList<String> pageList;
		crawler = new CrawlerController(thisSite.toString());
		
		driver.navigate().to(thisSite);
		MyCrawler.pageList.clear();
		startCrawler(thisSite);
		pageList = MyCrawler.pageList;
		stopCrawler();
		
		String copyFile = sitePath + File.separator + thisSite.getHost().toString() + TEXT_FILE_TYPE;
		saveCopy(pageList, copyFile);
	}
	
	/** Instantiates new Word document, and calls functions to collect, format, and insert copy into
	 * the file. Word document is then saved to correct location. Handles errors associated with Word 
	 * document creation and saving.
	 * @param ArrayList pageList each page on web site to collect copy from
	 * @param String copyFile the location to store the Word document */
	private void saveCopy(ArrayList<String> pageList, String copyFile) {
		WordprocessingMLPackage wordDoc = null;
		try {
			wordDoc = WordprocessingMLPackage.createPackage();
		} catch (InvalidFormatException e) {
			UserInterface.errorPopup("Unable to create Word document."
					+ "\nPlease contact program admin if this occurs.");
		}
		
        for (String thisPage : pageList){
        	driver.navigate().to(thisPage);
        	String pageTitle = driver.getTitle();
        	byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        	ArrayList<WebElement> webElements = (ArrayList<WebElement>) driver.findElements(By.cssSelector(CSS_SELECTOR));
    		new WordParser(pageTitle, screenshot, webElements, wordDoc);
        }
        
    	try {
			wordDoc.save(new java.io.File(copyFile));
		} catch (Docx4JException e) {
			UserInterface.errorPopup("Critical error occured while saving Word document."
					+ "\nPlease contact program admin if this occurs.");
		}
	}
	
	/** Adds current site as seed to crawler and starts crawler
	 * @param URL thisSite the site to add as seed and then crawl */
	private void startCrawler(URL thisSite) {
		crawler.getCrawler().addSeed(thisSite.toString());
		crawler.getCrawler().start(MyCrawler.class, NUM_OF_CRAWLERS);
	}
	
	/** Stops crawler for current site */
	private void stopCrawler() {
		 crawler.getCrawler().shutdown();
		 crawler.getCrawler().waitUntilFinish();
	}
}
