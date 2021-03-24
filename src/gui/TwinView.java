package gui;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import files.Document;

/**
 * Twin viewer class
 * 
 * @author Christoph Stamm
 *
 */
public class TwinView extends Composite {
	public MainWindow m_mainWnd;
	
	private Cursor m_standardCursor = null;
	private Cursor m_areaCursor = null;
	private Document m_doc1, m_doc2;
	private View m_view1, m_view2;
	private ColorTableDlg m_colorTable;
	private HistogramDlg m_histogram;
	private LineViewer m_lineViewer;
	private FrequencyEdt m_frequenciesEditor;
	private boolean m_autoZoom = true;
	private boolean m_synchronized = false;
	private boolean m_meanColor = false;
	
	public TwinView(MainWindow mainWnd, Composite parent, int style) {
		super(parent, style);
		
		m_mainWnd = mainWnd;
		m_standardCursor = getShell().getCursor();
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 800;
		data.heightHint = 600;
		setLayoutData(data);
		setLayout(new FillLayout());
		
		m_doc1 = new Document();
		m_view1 = new View(this);
		
		try {
			ImageData image = new ImageData(getShell().getClass().getClassLoader().getResource("images/areaHS.png").openStream());
			m_areaCursor = new Cursor(getShell().getDisplay(), image, image.width/2, image.height/2);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	// dispose all dialogs
	@Override
	public void dispose() {
		m_view1.setImageData(null);
		if (m_view2 != null) m_view2.setImageData(null);
		closeColorTable();
		closeHistogram();
		closeFrequencies();
		m_areaCursor.dispose();
	}
	
	public boolean isEmpty() {
		return m_view1.getImageData() == null;
	}
	
	public View getView(boolean first) {
		return (first) ? m_view1 : m_view2;
	}

	public Document getDocument(View view) {
		return (view == m_view1) ? m_doc1 : m_doc2;
	}
	
	public Document getDocument(boolean first) {
		return (first) ? m_doc1 : m_doc2;
	}
	
	public void load(String filename, int fileType) throws Exception {
		m_view1.setImageData(m_doc1.load(filename, fileType));
		layout();
		refresh(false);
	}
	
	public void close(boolean first) {
		if (first) {
			assert m_view2 == null : "view2 has to be closed first";
			m_view1.setImageData(null);
			if (m_colorTable != null) {
				m_colorTable.close();
				m_colorTable = null;
			}
			if (m_histogram != null) {
				m_histogram.close();
				m_histogram = null;
			}
			if (m_lineViewer != null) {
				m_lineViewer.close();
				m_lineViewer = null;
			}
			if (m_frequenciesEditor != null) {
				m_frequenciesEditor.close();
				m_frequenciesEditor = null;
			}
		} else if (m_view2 != null) {
			if (m_colorTable != null) {
				m_colorTable.update(false, this);
			}
			if (m_histogram != null) {
				m_histogram.update(false, this);
			}
			if (m_lineViewer != null) {
				m_lineViewer.update(false, this);
			}
			split();
		}
	}
	
	public void save(boolean first, String filename, int fileType) throws Exception {
		if (first) {
			assert m_doc1 != null : "m_doc1 is null";
			m_doc1.save(m_view1.getImageData(), m_view1.getImageType(), filename, fileType);
		} else {
			assert m_doc2 != null : "m_doc2 is null";
			m_doc2.save(m_view2.getImageData(), m_view2.getImageType(), filename, fileType);
		}
	}
	
	public boolean useMeanColor() {
		return m_meanColor;
	}
	
	public boolean hasAutoZoom() {
		return m_autoZoom;
	}
	
	public boolean hasColorTable() {
		return m_colorTable != null;
	}
	
	public boolean hasHistogram() {
		return m_histogram != null;
	}
	
	public boolean hasLineViewer() {
		return m_lineViewer != null;
	}
	
	public boolean hasWaves() {
		return m_frequenciesEditor != null;
	}

	public boolean hasSecondView() {
		return m_view2 != null;
	}
	
	public boolean isSynchronized() {
		return m_synchronized;
	}
	
	public ImageData getImage(boolean first) {
		return (first) ? m_view1.getImageData() : m_view2.getImageData();
	}
	
	public int getImageType(boolean first) {
		return (first) ? m_view1.getImageType() : m_view2.getImageType();
	}
	
	public float getZoomFactor(boolean first) {
		return (first) ? m_view1.getZoom() : m_view2.getZoom();
	}
	
	public void swapImages() {
		assert hasSecondView() : "m_view2 is null";
		
		// swap images
		ImageData tmp = m_view1.getImageData();
		m_view1.setImageData(m_view2.getImageData());
		m_view2.setImageData(tmp);
		// swap documents
		Document t = m_doc1; m_doc1 = m_doc2; m_doc2 = t;
		
		// update dialogs
		refresh(false);
	}
	
	public void showImageInFirstView(ImageData imageData, String fileName) {
		if (imageData == null) return;
		
		m_view1.setImageData(imageData);
		m_doc1.setFileName(fileName);
		layout();
		refresh(false);
		m_mainWnd.notifyAllMenus();
	}
	
	public void showImageInSecondView(ImageData imageData) {
		if (imageData == null) return;
		
		if (!hasSecondView()) split();
		if (hasSecondView()) {
			m_view2.setImageData(imageData);
			m_doc2.setFileName(null);
			layout();
			refresh(false);
			m_mainWnd.notifyAllMenus();
		}
	}
	
	public void split() {
		if (hasSecondView()) {
			// destroy second view
			m_view2.setImageData(null);
			m_view2.dispose();
			m_view2 = null;
			m_doc2 = null;
		} else {
			// create second view
			m_doc2 = new Document();
			m_view2 = new View(this);
			m_view2.setImageData(m_view1.getImageData());
		}
		layout();		
	}
	
	@Override
	public void layout() {
		if (!isEmpty()) {
			Layout l = getLayout();
			assert l instanceof FillLayout : "wrong layout";
			FillLayout fillLayout = (FillLayout)l;
			fillLayout.type = (m_view1.isPortrait()) ? SWT.HORIZONTAL : SWT.VERTICAL;
		}
		super.layout();
	}
	
	public void refresh(boolean resize) {
		if (m_autoZoom) setAutoZoom(true);
		else synchronizeZoomAndScrollPos(m_view1);
		if (!resize) {
			if (m_colorTable != null) m_colorTable.update(hasSecondView(), this); 
			if (m_histogram != null) m_histogram.update(hasSecondView(), this); 
			if (m_lineViewer != null) m_lineViewer.update(hasSecondView(), this); 
			if (m_frequenciesEditor != null) {
		    	Shell shell = getShell();
		    	Cursor cursor = shell.getCursor();
		    	
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));   	
				m_frequenciesEditor.update(this);
		    	shell.setCursor(cursor);			
			}
		}
		//System.out.println("Refresh");
	}
	
	public void setAutoZoom(boolean b) {
		m_autoZoom = b;
		if (m_autoZoom) {
			// compute minimal zoom factor
			m_view1.computeBestZoomFactor();
			if (hasSecondView()) m_view2.computeBestZoomFactor();
			synchronizeZoomAndScrollPos(m_view1);
		}
	}
	
	public void zoom100() {
		setAutoZoom(false);
		m_view1.setZoom(1.0f);
		if (hasSecondView()) m_view2.setZoom(1.0f);
		synchronizeZoomAndScrollPos(m_view1);
	}
	
	/**
	 * synchronize zoom factor and scroll position
	 * @param source
	 */
	public void synchronizeZoomAndScrollPos(View source) {
		boolean v2 = hasSecondView();
		
		if (m_synchronized) {
			View target = (source == m_view1) ? m_view2 : m_view1;
			if (source != null && target != null) {
				target.setZoom(source.getZoom());
				target.scroll(source.getScrollPosX(), source.getScrollPosY());
			}
		}
		m_mainWnd.showZoomFactor(m_view1.getZoom(), (v2) ? m_view2.getZoom() : 0);
		
		m_view1.updateScrollBars(true);
		if (v2) m_view2.updateScrollBars(true);
	}
	
	public void synchronize() {
		if (m_synchronized) {
			m_synchronized = false;
		} else {
			if (!hasSecondView()) split();
			m_synchronized = true;
		}
		synchronizeZoomAndScrollPos(m_view1);
	}
	
	public void synchronizeHorizontally(View view, int x) {
		if (m_synchronized) {
			if (m_view1 == view) {
				if (hasSecondView()) m_view2.scrollHorizontally(x);
			} else {
				m_view1.scrollHorizontally(x);
			}
		}
	}
	
	public void synchronizeVertically(View view, int y) {
		if (m_synchronized) {
			if (m_view1 == view) {
				if (hasSecondView()) m_view2.scrollVertically(y);
			} else {
				m_view1.scrollVertically(y);
			}
		}
	}
	
	public void toggleMeanAreaColor() {
		m_meanColor = !m_meanColor;
    	Shell shell = getShell();
    	
		shell.setCursor(m_meanColor ? m_areaCursor : m_standardCursor);   	
	}
	
	public void toggleColorTable() {
		if (m_colorTable != null) {
			// close color table
			m_colorTable.close();
			m_colorTable = null;
		} else {
			// open color table
			m_colorTable = new ColorTableDlg(getShell());
			m_colorTable.open(this);
		}
	}
	
	public void closeColorTable() {
		if (m_colorTable != null) {
			m_colorTable.close();
			m_colorTable = null;
		}		
	}
	
	public void toggleHistogram() {
		if (m_histogram != null) {
			// close histogram
			m_histogram.close();
			m_histogram = null;
		} else {
			// open histogram
			m_histogram = new HistogramDlg(getShell());
			m_histogram.open(this);
		}
	}
	
	public void closeHistogram() {
		if (m_histogram != null) {
			m_histogram.close();
			m_histogram = null;
		}		
	}

	public void toggleLineViewer() {
		if (m_lineViewer != null) {
			// close line viewer
			m_lineViewer.close();
			m_lineViewer = null;
		} else {
			// open histogram
			m_lineViewer = new LineViewer(getShell());
			m_lineViewer.open(this);
		}
	}
	
	public void closeLineViewer() {
		if (m_lineViewer != null) {
			m_lineViewer.close();
			m_lineViewer = null;
		}		
	}

	public void toggleFrequencies() {
		if (m_frequenciesEditor != null) {
			// close waves
			m_frequenciesEditor.close();
			m_frequenciesEditor = null;
		} else {
			Shell shell = getShell();
			// open waves
			m_frequenciesEditor = new FrequencyEdt(getShell());
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));   	
			m_frequenciesEditor.open(this);
	    	if (!shell.isDisposed()) shell.setCursor(m_standardCursor);			
		}
	}
	
	public void closeFrequencies() {
		if (m_frequenciesEditor != null) {
			m_frequenciesEditor.close();
			m_frequenciesEditor = null;
		}		
	}
}
