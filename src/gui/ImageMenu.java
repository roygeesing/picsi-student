package gui;

import imageprocessing.colors.*;
import imageprocessing.transform.Rotate;
import imageprocessing.transform.RotateAndScale;
import imageprocessing.transform.Scale;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

import imageprocessing.Cropping;

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
		add("MyInvert", 0, new MyInverter());
		add("Grayscale", 0, new Grayscale());
		add("Dither", 0, new Dither());

		UserMenu channels = addMenu("Channel");		
		channels.add("R\tCtrl+1", 								SWT.CTRL | '1', new ChannelRGB(0));
		channels.add("G\tCtrl+2", 								SWT.CTRL | '2', new ChannelRGB(1));
		channels.add("B\tCtrl+3", 								SWT.CTRL | '3', new ChannelRGB(2));

		UserMenu transform = addMenu("Transform");
		transform.add("Rotate", 0, new Rotate());
		transform.add("Rotate and scale", 0, new RotateAndScale());
		transform.add("Scale", 0, new Scale());
		
		// TODO add here further image processing entries (they are inserted into the Image menu)
	}	
}
