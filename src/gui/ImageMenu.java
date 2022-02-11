package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

import imageprocessing.Cropping;
import imageprocessing.colors.ChannelRGB;
import imageprocessing.colors.Inverter;

/**
 * Image processing menu
 * @author Christoph Stamm
 *
 */
public class ImageMenu extends UserMenu {	
	/**
	 * Registration of image operations
	 * @param item menu item
	 * @param views twin view
	 * @param mru MRU
	 */
	public ImageMenu(MenuItem item, TwinView views, MRU mru) {
		super(item, views, mru);

		add("C&ropping\tCtrl+R", 								SWT.CTRL | 'R', new Cropping());
		add("&Invert\tF1", 										SWT.F1, 		new Inverter());

		UserMenu channels = addMenu("Channel");		
		channels.add("R\tCtrl+1", 								SWT.CTRL | '1', new ChannelRGB(0));
		channels.add("G\tCtrl+2", 								SWT.CTRL | '2', new ChannelRGB(1));
		channels.add("B\tCtrl+3", 								SWT.CTRL | '3', new ChannelRGB(2));
		
		// TODO add here further image processing entries (they are inserted into the Image menu)
	}	
}
