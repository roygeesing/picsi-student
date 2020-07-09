package files;
import javax.swing.JTextArea;

import org.eclipse.swt.graphics.*;

/**
 * Image document class
 * 
 * @author Christoph Stamm
 *
 */
public class Document {
	private String m_fileName;	// image file name
	private IImageFile m_file;	// image file
	private int m_fileType;		// image file type
	
	/**
	 * Loads an image file
	 * @param filename
	 * @param fileType
	 * @param display
	 * @return imageData
	 * @throws Exception
	 */
	public ImageData load(String filename, int fileType) throws Exception {
		m_fileType = fileType;
		m_file = ImageFiles.createImageFile(m_fileType);
		if (m_file != null) {
			m_fileName = filename;
			return m_file.read(filename);
		}
		return null;
	}
	
	/**
	 * Saves an image file
	 * @param image
	 * @param imageType
	 * @param filename
	 * @param fileType
	 * @throws Exception
	 */
	public void save(ImageData imageData, int imageType, String filename, int fileType) throws Exception {
		if (filename == null) {
			if (m_file != null && m_fileName != null && m_fileType >= 0) {
				// save with existing file name and file type
				filename = m_fileName;
				fileType = m_fileType;
			} else {
				assert filename != null : "filename is null";
				assert fileType >= 0 : "wrong fileType";

				m_file = ImageFiles.createImageFile(fileType);
			}
		} else {
			// save with new file name or new file type
			assert fileType >= 0 : "wrong fileType";
			if (m_file == null || fileType != m_fileType) {
				m_file = ImageFiles.createImageFile(fileType);
			}
		}
		if (m_file != null) {
			m_fileName = filename;
			m_file.save(filename, fileType, imageData, imageType);
		}
	}
	
	/**
	 * Returns true if this image is in binary format
	 * @return
	 */
	public boolean isBinaryFormat() {
		assert m_file != null : "no image file available";
		return m_file.isBinaryFormat();		
	}
	
	/**
	 * Returns the name of this image file
	 * @return
	 */
	public String getFileName() {
		return m_fileName;
	}
	
	/**
	 * Returns the file type of this image file
	 * @return
	 */
	public int getfileType() {
		return m_fileType;
	}
	
	/**
	 * Converts the given image data from binary to ASCII and writes it to the given text area 
	 * @param image image data
	 * @param text text area
	 */
	public void displayTextOfBinaryImage(ImageData imageData, JTextArea text) {
		assert m_file != null : "no image file available";
		
		text.removeAll();
		m_file.displayTextOfBinaryImage(imageData, text);			
	}

}
