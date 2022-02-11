package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

import imageprocessing.colors.Inverter;

/**
 * Image processing module magb
 * @author Christoph Stamm
 *
 */
public class MAGB extends ImageMenu {
	/**
	 * Registration of image operations
	 * @param item menu item
	 * @param views twin view
	 * @param mru MRU
	 */
	public MAGB(MenuItem item, TwinView views, MRU mru) {
		super(item, views, mru);
		
		add("&Invert\tF1", SWT.F1, new Inverter());
		// TODO add here further image processing entries (they are inserted into the MAGB menu)
	}
}
