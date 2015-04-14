package copycollector;

import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * The CrawlerController configures the crawler, and specifies run options. Modified the source
 * code provided by the crawler4j documentation site.
 * Source: https://code.google.com/p/crawler4j/
 * @author Sharon Tartarone
 */
public class CrawlerController {
	
	/** Controller of crawler functions */
	private CrawlController crawler;

	/** Current page URL to crawl */
	public static String pageURL;
	
	private static final int POLITE_DELAY = 1000;		// milliseconds between requests
	private static final int MAX_DEPTH_OF_CRAWL = -1; 	// -1 means unlimited
	private static final int MAX_PAGES_TO_FETCH = -1; 	// -1 means unlimited
	private static final boolean RESUME_CRAWL = false; 	// do not resume previous crawl
	private static final boolean INCLUDE_HTTPS = true;	// allow crawl for HTTPS
	
	/** Sets up new instance of CrawlerController 
	 * @param String thisPage current URL of page to crawl */
	public CrawlerController(String thisPage) {
		setPageURL(thisPage);
		setUpCrawler();
	}
	
	/** Configures the controller for this crawl */
	public void setUpCrawler() {
		CrawlConfig config = new CrawlConfig();
                
       	config.setCrawlStorageFolder(CopyCollector.COPY_FOLDER);
       	config.setPolitenessDelay(POLITE_DELAY);
       	config.setMaxDepthOfCrawling(MAX_DEPTH_OF_CRAWL);
       	config.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
       	config.setResumableCrawling(RESUME_CRAWL);
       	config.setIncludeHttpsPages(INCLUDE_HTTPS);

       	PageFetcher pageFetcher = new PageFetcher(config);
       	RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
       	RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
                
       	try {
       		crawler = new CrawlController(config, pageFetcher, robotstxtServer);
       	} catch(Exception e) {
       		UserInterface.errorPopup("Error occured when new crawler is instantiated."
					+ "Please contact program admin if this occurs.");
       	}
	}
	
	/** Sets URL of current page to crawl 
	 * @param String thisPage current URL of page to crawl */
	private void setPageURL(String thisPage){
		pageURL = thisPage;
	}
	
	/** Allows access to CrawlController and its methods 
	 * @returns CrawlController object to control crawler run */
	public CrawlController getCrawler(){
		return crawler;
	}
}
