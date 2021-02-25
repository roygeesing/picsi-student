package imageprocessing;

import org.eclipse.swt.SWT;

import gui.ImageMenu;
import gui.ImageMenuItem;
import gui.MRU;
import gui.TwinView;

/**
 * Image processing module magb
 * @author Christoph Stamm
 *
 */
public class MAGB extends ImageMenu {
	/**
	 * Registration of image operations
	 * @param views
	 */
	public MAGB(TwinView views, MRU mru) {
		super(views, mru);
		
		add(new ImageMenuItem("&Invert\tF1", SWT.F1, new Inverter()));
		// TODO add here further image processing entries (they are inserted into the MAGB menu)
	}
}
