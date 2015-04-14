package copycollector;

import java.util.ArrayList;
import java.util.regex.Pattern;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * The MyCrawler class extends WebCrawler, and determines which pages should be visited
 * by the WebCrawler. Modified the source code provided by the crawler4j documentation site.
 * Source: https://code.google.com/p/crawler4j/
 * @author Sharon Tartarone
 */
public class MyCrawler extends WebCrawler {
	
	/** ArrayList pageList stores all valid URLs found by crawler */
	public static ArrayList<String> pageList = new ArrayList<String>();
	
	/** Specifies invalid URL extension types */
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|.css?"
			+ "|js|bmp|gif|jpe?g" 
			+ "|png|tiff?|mid|mp2|mp3|mp4|vcf|ico"
			+ "|wav|avi|mov|mpeg|ram|m4v|pdf|jsp" 
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	/** Specifies whether the given URL should be crawled or not (based on crawling logic)
	 * @param WebURL URL to check
	 * @returns if URL should be crawled */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith(CrawlerController.pageURL);
	}

	/** This function is called when a page is fetched and ready to be processed by your program. 
	 * Each valid URL is added to list.
	 * @param Page to visit and add to list */
	@Override
	public void visit(Page page) {          
		String url = page.getWebURL().getURL();
		pageList.add(url);
		System.out.println("URL: " + url);
	}
}