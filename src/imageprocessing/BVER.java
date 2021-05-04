package imageprocessing;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

import gui.ImageMenu;
import gui.MRU;
import gui.TwinView;

/**
 * Image processing in module bverI
 * @author Christoph Stamm
 *
 */
public class BVER extends ImageMenu {	
	/**
	 * Registration of image operations
	 * @param item menu item
	 * @param views twin view
	 * @param mru MRU
	 */
	public BVER(MenuItem item, TwinView views, MRU mru) {
		super(item, views, mru);

		ImageMenu channels = addMenu("Channel");		
		channels.add("R\tCtrl+1", 								SWT.CTRL | '1', new ChannelRGB(0));
		channels.add("G\tCtrl+2", 								SWT.CTRL | '2', new ChannelRGB(1));
		channels.add("B\tCtrl+3", 								SWT.CTRL | '3', new ChannelRGB(2));
		add("C&ropping\tCtrl+R", 								SWT.CTRL | 'R', new Cropping());
		// TODO add here further image processing entries (they are inserted into the BVER menu)
	}	
}
