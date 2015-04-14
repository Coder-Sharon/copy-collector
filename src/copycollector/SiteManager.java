package copycollector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SiteManager {
	
	/** Preferences contain saved user-given sites from previous runs */
	private static Preferences prefs;
	
	/** ArrayList of user-given sites that can be edited during runtime */
	public static ArrayList<URL> mySites;
	
	/** Sets up new instance of SiteManager */
	public SiteManager() {
		getMySites();
	}
	
	/** Builds ArrayList with provided URLs from stored preferences
	 * @returns URL list mySites */
	private void getMySites() {
		ArrayList<URL> theseSites = new ArrayList<URL>();
		prefs = Preferences.userNodeForPackage(SiteManager.class.getClass());
		
		if(!prefs.get("MY_SITES_PREF", "").isEmpty()) {
			String myPreferences = prefs.get("MY_SITES_PREF", "");
			List<String> temp = Arrays.asList(myPreferences.split(","));
			for(String thisString : temp)
				try {
					theseSites.add(new URL(thisString));
				} catch (MalformedURLException e) {
					UserInterface.errorPopup("Malformed URL found when building site list."
							+ "\nPlease contact program admin if this occurs.");
				}
		}
		
		mySites = theseSites;
	}
	
	/** Saves ArrayList changes made by user in current run by updating preferences */
	public static void saveChanges() {
		String joinedList = "";
		for(URL thisSite : mySites)
			joinedList = joinedList.concat(thisSite.toString() + ",");
		
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			UserInterface.errorPopup("Error occured when during preference clearing."
					+ "\nPlease contact program admin if this occurs.");
		}
		
		prefs.put("MY_SITES_PREF", joinedList);
		
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			UserInterface.errorPopup("Error occured when during preference syncing."
					+ "\nPlease contact program admin if this occurs.");
		}
	}
}
