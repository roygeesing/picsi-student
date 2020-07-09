package imageprocessing;

import org.eclipse.swt.SWT;

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
	public MAGB(TwinView views) {
		super(views);
		
		add(new ImageMenuItem("&Invert\tF1", 									SWT.F1, new Inverter()));
		// TODO add here further image processing entries (they are inserted into the MAGB menu)
	}
}
