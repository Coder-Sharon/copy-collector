package copycollector;

import java.net.URL;
import java.net.MalformedURLException;

import org.junit.Test;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/** The UserInterface class provides a GUI for CopyCollector and SiteManager functions. Users can
 * add or remove web sites to crawl, and start and stop the running of the program. The GUI also
 * provides a menu bar for instructions and information, a progress bar while the program runs, 
 * as well as an option to set and explore the directory of copy. 
 * @author Sharon Tartarone
 */
public class UserInterface {
	
	/** Display of user interface */
	private static Display display = new Display();
	
	/** Shell of user interface */
	private static Shell shell = new Shell(display);
	
	/** Copy Collector thread */
	private CopyCollectorRunnable program;
	
	public static String directory;		//User chosen directory to store copy
	private List mySuite;				//Stores current site suite
	private ProgressBar bar;			//Stores current progress bar
	private Button run;					//Stores current run button
	private int thisSelection;			//Stores current site selected by user
	private boolean modified = false;	//Stores if site list has been modified
	private boolean saved = false;		//Stores if site list changes have been saved
	private boolean running = false;	//Stores if program is running
	private boolean exit = false; 		//Stores if user stopped program
	
	/** Sets up new instance of user interface */
	public UserInterface() {
		new SiteManager();
		buildShell();
		buildMenuBar();
		buildSuiteEditor();
		buildAddButton();
		buildRemoveButton();
		buildRunButton();
		buildProgressBar();
	}
	
	/** Builds shell specifications and adds listeners on GUI close */
	private void buildShell() {
		//Sets up grid layout
		GridLayout gridLayout = new GridLayout();
		
		//Sets visual features of shell
		shell.setLayout(gridLayout);
		shell.setText("Freedom Copy Collector");
		shell.setSize(250, 320);
		shell.setMinimumSize(250, 320);
		//shell.setImage(new Image(display, "icon.png"));
		shell.setBackground(new Color(display, 210, 221, 234));
		
		//Warns user of unsaved changes or program running when user tries to close
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
					userCloses(event);
			}
		});
	}

	/** Builds menu specifications and adds menu bar */
	private void buildMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);
		buildFileMenu(menuBar);
		buildEditMenu(menuBar);
		buildAboutMenu(menuBar);
		shell.setMenuBar(menuBar);
	}
	
	/** Builds file menu options and listeners 
	 * @param Menu menuBar where to add menu */
	private void buildFileMenu(Menu menuBar) {
		//File menu
		Menu fileMenu = new Menu(menuBar);
		MenuItem fileTab = new MenuItem(menuBar, SWT.CASCADE);
		fileTab.setText("File");
		fileTab.setMenu(fileMenu);
		
		//File menu options
		MenuItem save = new MenuItem(fileMenu, SWT.NONE);
		save.setText("Save test suite");
		MenuItem directory = new MenuItem(fileMenu, SWT.NONE);
		directory.setText("Change directory");
		new MenuItem(fileMenu, SWT.SEPARATOR);
		final MenuItem run = new MenuItem(fileMenu, SWT.NONE);
		run.setText("Run collection");
		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem close = new MenuItem(fileMenu, SWT.NONE);
		close.setText("Close");
		
		//File menu listeners
		save.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userSaves();
			}
		});
		directory.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userChoosesDirectory();
			}
		});
		run.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(!running){
					run.setText("Stop collection");
					userRuns();
				}
				else {
					run.setText("Run collection");
				}
			}
		});
		close.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.close();
			    display.dispose();
			}
		});
	}
	
	/** Builds edit menu options and listeners 
	 * @param Menu menuBar where to add menu */
	private void buildEditMenu(Menu menuBar) {
		//Edit menu
		Menu editMenu = new Menu(menuBar);
		MenuItem editTab = new MenuItem(menuBar, SWT.CASCADE);
		editTab.setText("Edit");
		editTab.setMenu(editMenu);
		
		//Edit menu options
		MenuItem add = new MenuItem(editMenu, SWT.NONE);
		add.setText("Add new website");
		MenuItem remove = new MenuItem(editMenu, SWT.NONE);
		remove.setText("Remove selected website");
		
		//Edit menu listeners
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userAdds();
			}
		});
		remove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userRemoves();
			}
		});
	}
	
	/** Builds about menu options and listeners \
	 * @param Menu menuBar where to add menu */
	private void buildAboutMenu(Menu menuBar) {
		//About menu
		Menu aboutMenu = new Menu(menuBar);
		MenuItem aboutTab = new MenuItem(menuBar, SWT.CASCADE);
		aboutTab.setText("About");
		aboutTab.setMenu(aboutMenu);
			
		//About menu options
		MenuItem about = new MenuItem(aboutMenu, SWT.NONE);
		about.setText("About program");
		MenuItem instructions = new MenuItem(aboutMenu, SWT.NONE);
		instructions.setText("How to use");
		MenuItem javadoc = new MenuItem(aboutMenu, SWT.NONE);
		javadoc.setText("Javadoc");
		
		//About menu listeners
		about.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userAbout(e);
			}
		});
		instructions.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userInstructions(e);
			}
		});
		javadoc.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userJavadoc();
			}
		});
	}
	
	/** Builds suite list and suite edit buttons */
	private void buildSuiteEditor() {
		//Suite label
		Label suiteLabel = new Label(shell, SWT.NONE); 
		Font font = new Font(display, "Arial", 10, SWT.ITALIC | SWT.BOLD);
		suiteLabel.setFont(font);
		suiteLabel.setText("Your test suite:");
		suiteLabel.setBackground(new Color(display, 208, 195, 152));

		//Suite list
		mySuite = new List(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		for (URL thisSite : SiteManager.mySites) {
			mySuite.add(thisSite.toString());
		}
		
		//Grid layout data
		GridData data = new GridData();
		data.widthHint = 95;
		suiteLabel.setLayoutData(data);
		data = new GridData();
		data.widthHint = 200;
		data.heightHint = 75;
		mySuite.setLayoutData(data); 
		
		//Suite editor listener
		mySuite.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				thisSelection = mySuite.getSelectionIndex();
			}
		});
	}
	
	/** Builds add site button and listener */
	private void buildAddButton() {
		//Add button
		Button addSite = new Button(shell, SWT.PUSH);
		addSite.setText("Add new site");
		
		//Grid layout data
		GridData data = new GridData(); 
		data.widthHint = 225;
		addSite.setLayoutData(data);
		
		//Add button listener
		addSite.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				userAdds();
			}
		});
	}
	
	/** Builds remove site button and listener */
	private void buildRemoveButton() {
		//Remove button
		Button removeSite = new Button(shell, SWT.PUSH);
		removeSite.setText("Remove selection");
		
		//Grid layout data
		GridData data = new GridData(); 
		data.widthHint = 225;
		removeSite.setLayoutData(data);
		
		//Remove button listener
		removeSite.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) { 
				userRemoves();
		   	}
		});
	}

	/** Builds run program button and listener */
	public void buildRunButton() {
		//Run button
		run = new Button(shell, SWT.PUSH);
		Font font = new Font(display, "Arial", 8, SWT.BOLD);
		run.setText("Run the Copy Collector");
		run.setFont(font);
		
		//Grid layout data
		GridData data = new GridData(); 
		data.widthHint = 225;
		data.heightHint = 50;
		run.setLayoutData(data);
		
		//Run button listener
		run.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(!running)
					userRuns();
				else
					userStops();
			}
	    });
	}
	
	/** Builds progress bar that shows program progress */
	public void buildProgressBar() {
		bar = new ProgressBar(shell, SWT.INDETERMINATE);
		bar.setVisible(false);
		bar.setBounds(10, 10, 200, 32);
		
		GridData data = new GridData(); 
		data.widthHint = 225;
		data.heightHint = 20;
		bar.setLayoutData(data);
	}
	
	/** Called when user saves changes of test suite */
	private void userSaves() {
		SiteManager.saveChanges();
		saved = true;
	}
	
	/** Called when user changes directory to store results */
	private void userChoosesDirectory(){
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setText("Choose a directory");
		dialog.setMessage("Please choose a directory to store your copy and then press \"OK\":");
		String selectedDirectory = dialog.open();
		directory = selectedDirectory;
	}

	/** Called when user adds site to test suite */
	private void userAdds() {
		final UrlValidator urlValidator = new UrlValidator();
		IInputValidator validator = new IInputValidator() {
			public String isValid(String newText) {
				if(urlValidator.isValid(newText))
					return null;
				else
					return "Please enter a valid URL using the following format:\n"
							+ "https://www.google.com/";
	          }
		 };
		 
		InputDialog dialog = new InputDialog(shell, "Add website", "Enter the website that you want to add.", "", validator);
		if(dialog.open() == Window.OK){
			try {
				SiteManager.mySites.add(new URL(dialog.getValue()));
			} catch (MalformedURLException e) {
				errorPopup("Malformed URL detected when adding new site to list."
						+ "\nPlease contact program admin if this occurs.");
			}
			mySuite.add(dialog.getValue());
			modified = true;
			saved = false;
		}
	}
		 	
	/** Called when user removes site from test suite */
	private void userRemoves() {
		SiteManager.mySites.remove(thisSelection);
		mySuite.remove(thisSelection);
		modified = true;
		saved = false;
	}
	
	@Test
	/** Called when user runs program */
	public void userRuns() {
		program = new CopyCollectorRunnable();	
		
		if(SiteManager.mySites.isEmpty())
			errorPopup("Please add websites to your suite.");
		else {
			if(directory == null)
				userChoosesDirectory();	
			
			if (directory != null) {
					running = true;
					run.setText("Stop the Copy Collector");
					bar.setVisible(true);
					Thread thread = new Thread(program);
					thread.start();
			}
		}
	}
	
	public void userStops() {
		exit = true;
		running = false;
		bar.setVisible(false);
	}
	
	public class CopyCollectorRunnable implements Runnable {
	    public void run() {
	    	while(!exit) 
			new CopyCollector();
	    	if(exit) {
	    		return;
	    	}
			running = false;
			run.setText("Run the Copy Collector");
			bar.setVisible(false);
	    }
	}
	
	/** Called when user closes applications 
	 * @param Event event when user clicks an option */
	private void userCloses(Event event) {
		MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_WARNING | SWT.YES | SWT.NO);
		messageBox.setText("Question");
    	  if(running) {
	          messageBox.setMessage("Your program is running, and will be stopped if exited.\n"
	          		+ "Are you sure you want to exit?");
	          if (messageBox.open() == SWT.YES){
	        	  if(modified && !saved)
	        		  event.doit = false;
	        	  else
	        		  event.doit = true;
	          }
	          else
	            event.doit = false;
	      }
    	  if(modified && !saved) {
	          messageBox.setMessage("You modified your test suite, but did not save it.\n"
	          		+ "Do you want to save these changes?");
	          if (messageBox.open() == SWT.YES){
				userSaves();
	            event.doit = true;
	          }
	          else
	            event.doit = true;
	      }
	}
	
	/** Called when user opens about program information 
	 * @param Event event when user clicks an option */
	private void userAbout(Event event) {
		MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("About program");
		messageBox.setMessage("Freedom Copy Collector was developed for Freedom Mortgage to serve the purpose "
		 	+ "of collecting copy from \ncompany websites. The program will assist legal and compliance by "
		 	+ "automatically producing copy decks for review. \n\nThe program collects copy from user-"
		 	+ "specified websites, and outputs the copy and screenshots to a Word document. \nThese Word "
		 	+ "documents can be commented and shared to ensure Freedom Mortgage's websites are always up-"
		 	+ "to-date \nand compliant. For further help, go to \"How to use\" for use instructions.\n\n"
		 	+ "Developed by Sharon Tartarone, Emerging Technologies Intern.");
		if (messageBox.open() == SWT.OK)
	    	event.doit = true;
	}
	
	/** Called when opens about how to use information 
	 * @param Event event when user clicks an option */
	private void userInstructions(Event event) {
		MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("How to use");
		messageBox.setMessage("Freedom Copy Collector is very simple to use. The following are the only options:\n\n"
				+ "1. To start the program: Press \"Run the Copy Collector\" \n\tOR File -> Run collection\n\n"
				+ "2. To stop the program when running: Press \"Run the Copy Collector\" \n\tOR File -> Stop collection\n\n"
				+ "3. To add new site to the suite: Press \"Add new site\" \n\tOR Edit -> Add new website\n\n"
				+ "4. To remove site from suite: Select site on list and press \"Remove selection\" \n\tOR Edit -> Remove selected website\n\n"
				+ "5. To save test suite: Press File -> Save test suite\n\n"
				+ "6. To change storage directory: Press File -> Change directory\n\n"
				+ "7. To close application: Press [x] OR File -> Close");
		if (messageBox.open() == SWT.OK)
	    	event.doit = true;
	}
	
	/** Called when opens about Javadoc information */
	private void userJavadoc() {
		System.out.println("Javadoc");
	}
	
	public static void errorPopup (String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Warning");
		messageBox.setMessage(message);
		messageBox.open();
	}
	
	/** Main launches application */
	public static void main(String args[]) {
		new UserInterface();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) 
				display.sleep();
		} 
	}
}