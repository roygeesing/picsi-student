package gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Abstract base class for image processing menus
 * @author Christoph Stamm
 *
 */
public abstract class ImageMenu {
	private TwinView m_views;
	private MRU m_mru;
	private ArrayList<ImageMenuItem> m_menuItems = new ArrayList<ImageMenuItem>();

	protected ImageMenu(TwinView views, MRU mru) {
		assert views != null : "views are null";
		m_views = views;
		m_mru = mru;
	}
	
	public void add(ImageMenuItem item) {
		m_menuItems.add(item);
	}
	
	public void createMenuItems(Menu menu) {
		for(final ImageMenuItem item : m_menuItems) {
			MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(item.m_text);
			mi.setAccelerator(item.m_accelerator);
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					run(item);
					m_mru.setLastOperation(item.m_text);
				}
			});
		}
	}
	
	public boolean isEnabled(int index) {
		return m_menuItems.get(index).m_process.isEnabled(m_views.getFirstImageType());
	}

	public boolean findAndRun(String text) {
		for(ImageMenuItem mi: m_menuItems) {
			if (mi.m_text.equals(text)) {
				if (mi.m_process.isEnabled(m_views.getFirstImageType())) {
					run(mi);
				}
				return true;
			}
		}
		return false;
	}
	
	private void run(ImageMenuItem item) {
		ImageData output = null;
		try {
			output = item.m_process.run(m_views.getFirstImage(), m_views.getFirstImageType());
		} catch(Throwable e) {
			int last = item.m_text.indexOf('\t');
			if (last == -1) last = item.m_text.length();
			String location = item.m_text.substring(0, last).replace("&", "");
			m_views.m_mainWnd.showErrorDialog("ImageProcessing", location, e);
		}						
		if (output != null) {
			m_views.showImageInSecondView(output);
		}		
	}
}
