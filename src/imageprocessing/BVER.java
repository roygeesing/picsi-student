package imageprocessing;

import org.eclipse.swt.SWT;

import gui.TwinView;

/**
 * Image processing in module bverI
 * @author Christoph Stamm
 *
 */
public class BVER extends ImageMenu {	
	/**
	 * Registration of image operations
	 * @param views
	 */
	public BVER(TwinView views) {
		super(views);

		add(new ImageMenuItem("Channel R\tCtrl+1", 								SWT.CTRL | '1', new ChannelRGB(0)));
		add(new ImageMenuItem("Channel G\tCtrl+2", 								SWT.CTRL | '2', new ChannelRGB(1)));
		add(new ImageMenuItem("Channel B\tCtrl+3", 								SWT.CTRL | '3', new ChannelRGB(2)));
		add(new ImageMenuItem("C&ropping\tCtrl+R", 								SWT.CTRL | 'R', new Cropping()));
		// TODO add here further image processing entries (they are inserted into the BVER menu)
	}	
}
