package gui;

import main.Picsi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tracker;

/**
 * Rectangle tracker for input view
 * 
 * @author Christoph Stamm
 *
 */
public class RectTracker {
	private Tracker m_t1, m_t2;
	
	public RectTracker() {
		TwinView twins = Picsi.getTwinView();
		assert twins != null : "twin view is null";

		View view = twins.getView(true); // input view
		m_t1 = new Tracker(view, SWT.LEFT | SWT.RIGHT | SWT.UP | SWT.DOWN);
		m_t2 = new Tracker(view, SWT.RIGHT | SWT.DOWN | SWT.RESIZE);
	}
	
	public Rectangle track(int x, int y, int pw, int ph) {
		TwinView twins = Picsi.getTwinView();
		assert twins != null : "twin view is null";
		
		View view = twins.getView(true); // input view
		final int w = view.getImageWidth();
		final int h = view.getImageHeight();
		
		m_t1.setRectangles(new Rectangle[] { new Rectangle(view.image2Client(x), view.image2Client(y), view.image2Client(pw), view.image2Client(ph)) });
		m_t1.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent event) {
				Rectangle r = m_t1.getRectangles()[0];
				Point pnt = new Point(
						Math.max(0, Math.min(w - 1, view.client2ImageX(r.x))), 
						Math.max(0, Math.min(h - 1, view.client2ImageY(r.y))));
				twins.m_mainWnd.showImagePosition(pnt);
			}
		});
		m_t1.open();
		
		m_t2.setRectangles(m_t1.getRectangles());
		m_t2.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				Rectangle r = m_t2.getRectangles()[0];
				int x = Math.max(0, Math.min(w - 1, view.client2ImageX(r.x))); 
				int y = Math.max(0, Math.min(h - 1, view.client2ImageY(r.y))); 
				Point pnt = new Point(
						Math.max(1, Math.min(w - x, view.client2Image(r.width))), 
						Math.max(1, Math.min(h - y, view.client2Image(r.height))));
				twins.m_mainWnd.showImagePosition(pnt);
			}
		});
		m_t2.open();
		
		Rectangle r = m_t2.getRectangles()[0];
		r.x = Math.max(0, Math.min(w - 1, view.client2ImageX(r.x))); 
		r.y = Math.max(0, Math.min(h - 1, view.client2ImageY(r.y))); 
		r.width = Math.max(1, Math.min(w - r.x, view.client2Image(r.width))); 
		r.height = Math.max(1, Math.min(h - r.y, view.client2Image(r.height)));
		
		return r;
	}
}
