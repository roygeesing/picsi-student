package imageprocessing;

/**
 * Menu items for MAGB and BVER menus
 * 
 * @author Christoph Stamm
 *
 */
public class ImageMenuItem {
	String m_text;
	int m_accelerator;
	IImageProcessor m_process;
	
	public ImageMenuItem(String text, int accelerator, IImageProcessor proc) {
		m_text = text;
		m_accelerator = accelerator;
		m_process = proc;
	}
}
