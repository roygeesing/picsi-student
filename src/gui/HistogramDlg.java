package gui;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import imageprocessing.ImageProcessing;
import main.Picsi;

/**
 * Histogram dialog
 * 
 * @author Christoph Stamm
 *
 */
public class HistogramDlg extends Dialog {
	private Shell m_shell;
	private Label m_statLbl;
	private ImageData m_imageData;
	private Button m_inputBtn, m_outputBtn;
	private Canvas m_canvas;
	private Composite m_channelsComp;
	private int m_imageType;
	private int m_selectedChannel;
	private int[] m_hist;
	private IntSummaryStatistics m_stat;
	
    public HistogramDlg(Shell parent, int style) {
        super(parent, style);
    }

	public HistogramDlg(Shell parent) {
		super(parent);
	}

    public Object open(TwinView views) {
    	final int colorHeight = 20;
        Shell parent = getParent();
        Color[] rgb = new Color[] {
        	Display.getCurrent().getSystemColor(SWT.COLOR_RED),		
        	Display.getCurrent().getSystemColor(SWT.COLOR_GREEN),		
        	Display.getCurrent().getSystemColor(SWT.COLOR_BLUE)		
        };
        
        // create shell
        m_shell = new Shell(parent, SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MODELESS);
        m_shell.setAlpha(220);
        m_shell.setText("Histogram");
        {
			GridLayout gl = new GridLayout();
			gl.horizontalSpacing = 7;
			gl.verticalSpacing = 7;
			gl.marginHeight = 7;
			gl.marginWidth = 7;
			m_shell.setLayout(gl);
        }

		// set label text
		m_statLbl = new Label(m_shell, SWT.NONE);
		m_statLbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        	
		// create canvas
		m_canvas = new Canvas(m_shell, SWT.RESIZE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 4*colorHeight;
		data.minimumWidth = 256 + 2*m_canvas.getBorderWidth();
		m_canvas.setLayoutData(data);
		
		// create buttons
		Composite buttonsComp = new Composite(m_shell, SWT.NONE);
		{
			RowLayout rl = new RowLayout();
			rl.marginTop = 0;
			rl.marginRight = 0;
			rl.marginLeft = 0;
			rl.marginBottom = 0;
			rl.spacing = 15;
			buttonsComp.setLayout(rl);
		}

		Button logBtn = new Button(buttonsComp, SWT.CHECK);
		logBtn.setText("Logarithmic");
		logBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				m_canvas.redraw();	// redraws histogram;
			}
		});
		Composite ioComp = new Composite(buttonsComp, SWT.NONE);
		{
			RowLayout rl = new RowLayout();
			rl.marginRight = 0;
			rl.marginLeft = 0;
			rl.marginTop = 0;
			rl.marginBottom = 0;
			ioComp.setLayout(rl);
		}
		m_inputBtn = new Button(ioComp, SWT.RADIO);
		m_inputBtn.setText("Input");
		m_inputBtn.setSelection(true);
		m_inputBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// radio buttons are also unselected
				if (((Button)event.widget).getSelection()) update(views.hasSecondView(), views);
			}
		});
		m_outputBtn = new Button(ioComp, SWT.RADIO);
		m_outputBtn.setText("Output");
		m_outputBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// radio buttons are also unselected
				if (((Button)event.widget).getSelection()) update(views.hasSecondView(), views);
			}
		});
		m_channelsComp = new Composite(buttonsComp, SWT.NONE);
		{
			RowLayout rl = new RowLayout();
			rl.marginRight = 0;
			rl.marginLeft = 0;
			rl.marginTop = 0;
			rl.marginBottom = 0;
			m_channelsComp.setLayout(rl);
		}
		Button redBtn = new Button(m_channelsComp, SWT.RADIO);
		redBtn.setText("Red");
		redBtn.setSelection(true);
		redBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// radio buttons are also unselected
				if (((Button)event.widget).getSelection()) update(views.hasSecondView(), views);
			}
		});
		Button greenBtn = new Button(m_channelsComp, SWT.RADIO);
		greenBtn.setText("Green");
		greenBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// radio buttons are also unselected
				if (((Button)event.widget).getSelection()) update(views.hasSecondView(), views);
			}
		});
		Button blueBtn = new Button(m_channelsComp, SWT.RADIO);
		blueBtn.setText("Blue");
		blueBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// radio buttons are also unselected
				if (((Button)event.widget).getSelection()) update(views.hasSecondView(), views);
			}
		});
		
		// update data
        update(views.hasSecondView(), views);
        
        // draw histogram
        m_canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				Rectangle rect = m_canvas.getClientArea();
				GC gc = new GC(m_canvas);
				int w, x, pixel, offset;
				final int h = rect.height - colorHeight;
				final int max = Math.max(1, (logBtn.getSelection()) ? (int)Math.round(Math.log(m_stat.getMax())) : m_stat.getMax());
				final RGB[] colors = m_imageData.palette.colors;
				
				// draw histogram
				switch(m_imageType) {
				case Picsi.IMAGE_TYPE_BINARY:
					w = rect.width/2;
					offset = (rect.width - 2*w)/2;
					{
						// draw bar
						final int v = (logBtn.getSelection()) ? (int)Math.round(Math.log(m_hist[0])) : m_hist[0];
						final int height = h*v/max;
						gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
						gc.fillRectangle(offset + w/2, h - height, 2, height);
						// draw color
						Color c = rgb2color(colors[0]);
						gc.setBackground(c);
						gc.fillRectangle(offset, h, w, colorHeight);
						c.dispose();
					}
					{
						// draw bar
						final int v = (logBtn.getSelection()) ? (int)Math.round(Math.log(m_hist[1])) : m_hist[1];
						final int height = h*v/max;
						gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
						gc.fillRectangle(offset + w + w/2, h - height, 2, height);
						// draw color
						Color c = rgb2color(colors[1]);
						gc.setBackground(c);
						gc.fillRectangle(offset + w, h, w, colorHeight);
						c.dispose();
					}
					break;
				case Picsi.IMAGE_TYPE_GRAY:
				case Picsi.IMAGE_TYPE_INDEXED:
					w = rect.width/m_hist.length;
					offset = (rect.width - m_hist.length*w)/2;
					x = offset;
					pixel = 0;
					
					for(int v: m_hist) {
						// draw bar
						if (logBtn.getSelection()) v = (int)Math.round(Math.log(v));
						final int height = h*v/max;
						gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
						gc.fillRectangle(x, h - height, w, height);
						// draw color
						Color c = (colors != null) ? rgb2color(colors[pixel]) : gray2color(pixel);
						gc.setBackground(c);
						gc.fillRectangle(x, h, w, colorHeight);
						c.dispose();
						x += w;
						pixel++;
					}
					break;
				case Picsi.IMAGE_TYPE_RGB:
					w = rect.width/m_hist.length;
					offset = (rect.width - m_hist.length*w)/2;
					x = offset;
					pixel = 0;
					
					for(int v: m_hist) {
						// draw bar
						if (logBtn.getSelection()) v = (int)Math.round(Math.log(v));
						final int height = h*v/max;
						gc.setBackground(rgb[m_selectedChannel]);
						gc.fillRectangle(x, h - height, w, height);
						// draw color
						Color c = new Color(m_shell.getDisplay(), (m_selectedChannel == 0) ? pixel : 0, (m_selectedChannel == 1) ? pixel : 0, (m_selectedChannel == 2) ? pixel : 0);
						gc.setBackground(c);
						gc.fillRectangle(x, h, w, colorHeight);
						c.dispose();
						x += w;
						pixel++;
					}
					break;
				}
				gc.dispose();
			}
		});
		
		m_shell.pack();
        m_shell.open();
        Display display = parent.getDisplay();
        while (!m_shell.isDisposed()) {
        	if (!display.readAndDispatch()) display.sleep();
        }
        for(Color c: rgb) c.dispose();
        views.closeHistogram();
        return null;
    }

    public void close() {
    	if (m_shell != null && !m_shell.isDisposed()) m_shell.dispose();
    }
    
    public void update(boolean hasOutput, TwinView views) {
    	// enable input/output buttons
    	if (!hasOutput) {
    		m_inputBtn.setSelection(true);
    		m_outputBtn.setSelection(false);
    	}
		m_outputBtn.setEnabled(hasOutput);
    	if (m_outputBtn.getSelection()) {
    		m_imageData = views.getSecondImage();
    		m_imageType = views.getSecondImageType();
    	} else {
	    	m_imageData = views.getFirstImage();
	    	m_imageType = views.getFirstImageType();
    	}
    	
		// enable channel buttons and get selected channel
		boolean b = m_imageType == Picsi.IMAGE_TYPE_RGB;
		Control[] children = m_channelsComp.getChildren();
		
		for(int i=0; i < children.length; i++) {
			final Control ctrl = children[i];
			ctrl.setEnabled(b);
			if (ctrl instanceof Button) {
				final Button btn = (Button)ctrl;
				if (btn.getSelection()) {
					m_selectedChannel = i;
				}
			}
		}
		
    	// update histogram and statistic
    	if (m_imageType == Picsi.IMAGE_TYPE_RGB) {
    		m_hist = ImageProcessing.histogramRGB(m_imageData, m_selectedChannel);    		
    	} else {
    		m_hist = ImageProcessing.histogram(m_imageData, 1 << Math.min(8, m_imageData.depth));
    	}
    	m_stat = Arrays.stream(m_hist).summaryStatistics();
    	
    	// update statistic label
		m_statLbl.setText(Picsi.createMsg("Min = {0}   Max = {1}", 
			new Object[] { m_stat.getMin(), m_stat.getMax()}));

		m_shell.layout();	// necessary to update m_statLbl
		m_canvas.redraw();	// redraws histogram
    }
    
    private Color rgb2color(RGB rgb) {
    	return new Color(m_shell.getDisplay(), rgb);
    }

    private Color gray2color(int p) {
    	return new Color(m_shell.getDisplay(), p, p, p);
    }
}
