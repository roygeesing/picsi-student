package gui;

import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Most recently used files
 * 
 * @author Christoph Stamm
 *
 */
public class MRU {
	private static int MaxMRUitems = 6;	// number of items in the MRU list
	
	private MainWindow m_mainWindow;	// main window
	private Preferences m_prefs;		// system preferences used to store the MRU list
	private Menu m_recentFiles = null;	// menu used to display the MRU list

	/**
	 * Creates a MRU wrapper object
	 * @param mw main window
	 */
	public MRU(MainWindow mw) {
		m_mainWindow = mw;
		Preferences p = Preferences.userRoot();
		if (p != null) {
			m_prefs = p.node("/MRU");
		}
	}
	
	/**
	 * Adds MRU list to given menu
	 * @param recent menu
	 */
	public void addRecentFiles(Menu recent) {
		if (m_prefs == null) return;
		assert recent != null : "recent menu is null";
		m_recentFiles = recent;		
		
		// read filenames from prefs
		updateMenu();
	}
	
	/**
	 * Moves or adds a file name to top of list
	 * @param fileName
	 */
	public void moveFileNameToTop(String fileName) {
		if (m_prefs == null) return;
		assert fileName != null : "filename is null";
		
		int top = m_prefs.getInt("Top", 0);
		
		// find position of fileName
		for (int i=1; i <= MaxMRUitems; i++) {
			String key = String.valueOf(i);
			String fn = m_prefs.get(key, "");
			if (i != top && fileName.equals(fn)) {
				// swap i with top + 1
				top++;
				if (top > MaxMRUitems) top = 1;
				String key2 = String.valueOf(top);
				String fn2 = m_prefs.get(key2, "");
				
				m_prefs.put(key, fn2);
				m_prefs.put(key2, fn);
				m_prefs.putInt("Top", top);
				updateMenu();
				return;
			}
		}
		
		// it's a new file name: add filename to prefs
		top++;
		if (top > MaxMRUitems) top = 1;
		m_prefs.putInt("Top", top);
		
		m_prefs.put(String.valueOf(top), fileName);
		updateMenu();		
	}
	
	/**
	 * Delete current menu items and add new menu items according to the saved MRU list
	 */
	private void updateMenu() {
		// delete current menu items
		while (m_recentFiles.getItemCount() > 0) {
			m_recentFiles.getItem(m_recentFiles.getItemCount() - 1).dispose();
		}
		
		// add menu items
		int top = m_prefs.getInt("Top", 1);
		
		for (int i=0; i < MaxMRUitems; i++) {
			final String fileName = m_prefs.get(String.valueOf(top), "");
			if (!fileName.isEmpty()) {
				MenuItem item = new MenuItem(m_recentFiles, SWT.PUSH);
				item.setText(fileName);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (m_mainWindow.updateFile(fileName)) {
							// move fileName to top position in MRU
							moveFileNameToTop(fileName);
						} else {
							// remove fileName from MRU
							removeFileName(fileName);
						}
					}
				});
			}
			top--;
			if (top == 0) top = MaxMRUitems;
		}
	}

	/**
	 * Removes a file name from MRU list
	 * @param fileName
	 */
	private void removeFileName(String fileName) {
		if (m_prefs == null) return;
		assert fileName != null : "filename is null";
		
		for (int i=1; i <= MaxMRUitems; i++) {
			String key = String.valueOf(i);
			String fn = m_prefs.get(key, "");
			if (fileName.equals(fn)) {
				m_prefs.put(key, "");
				updateMenu();
				return;
			}
		}
	}
	
}
