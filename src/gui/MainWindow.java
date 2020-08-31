package gui;

import java.io.IOException;
import javax.swing.JTextArea;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.widgets.*;

import main.Picsi;
import files.Document;
import files.ImageFiles;
import imageprocessing.BVER;
import imageprocessing.ImageProcessing;
import imageprocessing.MAGB;

/**
 * Picsi SWT main window
 * 
 * @author Christoph Stamm
 *
 */
public class MainWindow {
	public TwinView m_views;
	
	private final MRU m_mru = new MRU(this);

	private Shell m_shell;		// subclassing of Shell is not allowed, therefore containing
	private Display m_display;
	private Editor m_editor;
	private String m_lastPath; // used to seed the file dialog
	private Label m_statusLabel, m_zoomLabel;
	private MenuItem m_editMenuItem;
	private MAGB m_magb;
	private BVER m_bver;

	/////////////////////////////////////////////////////////////////////////////////////////////////////7
	// public methods section

	/**
	 * @wbp.parser.entryPoint
	 */
	public Shell open(Display dpy) {
		// create a window and set its title.
		m_display = dpy;
		m_shell = new Shell(m_display);
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.marginLeft = 5;
			gridLayout.marginWidth = 0;
			gridLayout.verticalSpacing = 0;
			gridLayout.marginTop = 5;
			gridLayout.marginHeight = 0;
			m_shell.setLayout(gridLayout);
			m_shell.setCursor(m_display.getSystemCursor(SWT.CURSOR_CROSS)); // a wait-cursor can be used only if the cross-cursor is set for the shell instead of the view
			
			// Hook listeners.
			m_shell.addShellListener(new ShellAdapter() {
				@Override
				public void shellClosed(ShellEvent e) {
					e.doit = true;
				}
			});
			m_shell.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					// Clean up.
					if (m_views != null) m_views.clean();;
					if (m_editor != null) m_editor.dispose();
				}
			});
	
			// set icon
			try {
				m_shell.setImage(new Image(m_display, getClass().getClassLoader().getResource("images/picsi.png").openStream()));			
			} catch(IOException e) {
			}
			
			// set title
			m_shell.setText(Picsi.APP_NAME);
		}
		
		// create twin view: must be done before createMenuBar, because of dynamic image processing menu items
		m_views = new TwinView(this, m_shell, SWT.NONE);
		m_magb = new MAGB(m_views, m_mru);
		m_bver = new BVER(m_views, m_mru);
		
		// create
		createMenuBar();
		
		// create status bar
		{
			int dpiY = m_shell.getDisplay().getDPI().y;
			Composite compo = new Composite(m_shell, SWT.NONE);
			GridData data = new GridData (SWT.FILL, SWT.BOTTOM, true, false);
			Font font = m_shell.getFont();
			FontData[] fd = font.getFontData();
			data.heightHint = 2*fd[0].getHeight()*dpiY/96; //data.heightHint = 15;
			
			compo.setLayoutData(data);
			compo.setCursor(m_display.getSystemCursor(SWT.CURSOR_ARROW));
			
			GridLayout gridLayout = new GridLayout();
			gridLayout.marginRight = 5;
			gridLayout.numColumns = 2;
			gridLayout.horizontalSpacing = 10;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			compo.setLayout(gridLayout);
			
			// Label to show status and cursor location in image.
			m_statusLabel = new Label(compo, SWT.NONE);
			data = new GridData(SWT.FILL, SWT.FILL, true, true);
			m_statusLabel.setLayoutData(data);
			
			// Label to show zoom value
			m_zoomLabel = new Label(compo, SWT.RIGHT);
			data = new GridData(SWT.RIGHT, SWT.FILL, false, true);
			data.widthHint = 200;
			m_zoomLabel.setLayoutData(data);
		}
		
		// Open the window
		m_shell.pack();
		m_shell.open();
		return m_shell;
	}
	
	public void displayTextOfBinaryImage(Document doc, ImageData imageData, JTextArea text) {
		doc.displayTextOfBinaryImage(imageData, text);
	}
	
	/*
	 * Set the status label to show color information
	 * for the specified pixel in the image.
	 */
	public void showColorForPixel(Object[] args) {
		if (args == null) {
			m_statusLabel.setText("");
		} else {
			m_statusLabel.setText(Picsi.createMsg("Image color at ({0}, {1}) - pixel {2} [0x{3}] - is {4} [{5}] {6}", args));
		}
	}

	public void showImagePosition(Point pnt) {
		if (pnt == null) {
			m_statusLabel.setText("");
		} else {
			m_statusLabel.setText("(" + pnt.x + ',' + pnt.y + ')');
		}
	}
	
	public void showZoomFactor(float zoom1, float zoom2) {
		View view1 = m_views.getView(true);
		String s = "(" + view1.getImageWidth() + ',' + view1.getImageHeight() + ") " + Math.round(zoom1*100) + '%';
		
		if (m_views.isSynchronized() || !m_views.hasSecondView()) {
			m_zoomLabel.setText(s);
		} else {
			View view2 = m_views.getView(false);
			s += " | (" + view2.getImageWidth() + ',' + view2.getImageHeight() + ") " + Math.round(zoom2*100) + '%';
			m_zoomLabel.setText(s);
		}
	}
	
	/**
	 * Shows a modal error dialog and prints the stack trace to console window
	 * @param operation
	 * @param filename
	 * @param e exception
	 */
	public void showErrorDialog(String operation, String filename, Throwable e) {
		MessageBox box = new MessageBox(m_shell, SWT.ICON_ERROR);
		String message = Picsi.createMsg("Error {0}\nin {1}\n\n", new String[] {operation, filename});
		String errorMessage = "";
		if (e != null) {
			if (e instanceof SWTException) {
				SWTException swte = (SWTException)e;
				errorMessage = swte.getMessage();
				if (swte.throwable != null) {
					errorMessage += ":\n" + swte.throwable.toString();
				}
			} else if (e instanceof SWTError) {
				SWTError swte = (SWTError)e;
				errorMessage = swte.getMessage();
				if (swte.throwable != null) {
					errorMessage += ":\n" + swte.throwable.toString();
				}
			} else {
				errorMessage = e.toString();
			}
			e.printStackTrace();
		}
		box.setText("Error");
		box.setMessage(message + errorMessage);
		box.open();
	}
	
	public static class FileInfo {
		public String filename;
		public int fileType;
		
		public FileInfo(String name, int type) {
			filename = name;
			fileType = type;
		}
	}
	
	/***
	 * Get the user to choose a file name and type to save.
	 * @param imageType type of image (e.g. binary, grayscale, RGB, ...)
	 * @return
	 */
	public FileInfo chooseFileName(int imageType) {
		FileDialog fileChooser = new FileDialog(m_shell, SWT.SAVE);
		String[] saveFilterExts = ImageFiles.saveFilterExtensions(imageType);
		fileChooser.setFilterPath(m_lastPath);
		fileChooser.setFilterExtensions(saveFilterExts);
		fileChooser.setFilterNames(ImageFiles.saveFilterNames(imageType));
		
		String filename = null;
		
		if (m_views.hasSecondView()) {
			Document doc = m_views.getDocument(false);
			filename = doc.getFileName();			
		}
		if (filename != null) {
			fileChooser.setFileName(filename);
			fileChooser.setFilterIndex(ImageFiles.determineSaveFilterIndex(saveFilterExts, filename));
		}
		filename = fileChooser.open();
		m_lastPath = fileChooser.getFilterPath();
		if (filename == null)
			return null;

		// Figure out what file type the user wants.
		//fileChooser.getFilterIndex();
		int fileType = ImageFiles.determinefileType(filename);
		if (fileType == SWT.IMAGE_UNDEFINED) {
			MessageBox box = new MessageBox(m_shell, SWT.ICON_ERROR);
			box.setMessage(Picsi.createMsg("Unknown file extension: {0}\nPlease use bmp, gif, ico, jfif, jpeg, jpg, png, tif, or tiff.", 
				filename.substring(filename.lastIndexOf('.') + 1)));
			box.open();
			return null;
		}
		
		if (new java.io.File(filename).exists()) {
			MessageBox box = new MessageBox(m_shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			box.setMessage(Picsi.createMsg("Overwrite {0}?", filename));
			if (box.open() == SWT.CANCEL)
				return null;
		}
		
		return new FileInfo(filename, fileType);		
	}
	
	/***
	 * Get the user to choose a file to save.
	 * @param fileType type of file (e.g. JPEG, BMP, ...)
	 * @param imageType type of image (e.g. binary, grayscale, RGB, ...)
	 * @return
	 */
	public FileInfo chooseFileName(int fileType, int imageType) {
		if (fileType == SWT.IMAGE_UNDEFINED) {
			MessageBox box = new MessageBox(m_shell, SWT.ICON_ERROR);
			box.setMessage(Picsi.createMsg("Unknown file extension: {0}\nPlease use bmp, gif, ico, jfif, jpeg, jpg, png, tif, or tiff.", ""));
			box.open();
			return null;
		}
		
		String[] saveFilterExts = ImageFiles.saveFilterExtensions(imageType);
		int filterIndex = ImageFiles.determineSaveFilterIndex(saveFilterExts, ImageFiles.fileTypeString(fileType));
		FileDialog fileChooser = new FileDialog(m_shell, SWT.SAVE);
		fileChooser.setFilterPath(m_lastPath);		
		fileChooser.setFilterExtensions(new String[]{saveFilterExts[filterIndex]});
		fileChooser.setFilterNames(new String[]{ImageFiles.saveFilterNames(imageType)[filterIndex]});
				
		String filename = fileChooser.open();
		m_lastPath = fileChooser.getFilterPath();
		if (filename == null)
			return null;

		if (new java.io.File(filename).exists()) {
			MessageBox box = new MessageBox(m_shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			box.setMessage(Picsi.createMsg("Overwrite {0}?", filename));
			if (box.open() == SWT.CANCEL)
				return null;
		}
		
		return new FileInfo(filename, fileType);		
	}

	public boolean updateFile(String filename) {
		boolean retValue = true;
		m_shell.setCursor(m_display.getSystemCursor(SWT.CURSOR_WAIT));

		int fileType = ImageFiles.determinefileType(filename);
		
		try {
			m_views.load(filename, fileType);
			setTitle(filename, fileType);

			// notify all menus about the opened file
			notifyAllMenus();
		} catch (Throwable e) {
			showErrorDialog("loading", filename, e);
			retValue = false;
		} finally {
			m_shell.setCursor(m_display.getSystemCursor(SWT.CURSOR_CROSS));			
		}
		
		return retValue;
	}

	/**
	 * Used to disable the menu during line tracking
	 * @param enabled
	 */
	public void setEnabledMenu(boolean enabled) {
		Menu menuBar = m_shell.getMenuBar();
		if (menuBar != null) menuBar.setEnabled(enabled);
	}

	/**
	 * Notifies all menus about input/output changes
	 */
	public void notifyAllMenus() {
		Menu menuBar = m_shell.getMenuBar();
		for(MenuItem item: menuBar.getItems()) {
			item.getMenu().notifyListeners(SWT.Show, new Event());
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////7
	// private methods section
	
	private Menu createMenuBar() {
		// Menu bar.
		Menu menuBar = new Menu(m_shell, SWT.BAR);
		m_shell.setMenuBar(menuBar);
		createFileMenu(menuBar);
		createMagbMenu(menuBar);
		createBverMenu(menuBar);
		createWindowMenu(menuBar);
		createHelpMenu(menuBar);
		return menuBar;
	}
	
	// File menu
	private void createFileMenu(Menu menuBar) {	
		final int OPENRUNLAST = 2;
		final int CLOSEINPUT = 5;
		final int CLOSEOUTPUT = 6;
		final int CLOSEBOTH = 7;
		final int SAVE = 9;
		final int SAVEAS = 10;
		final int SAVEINPAS = 11;
		final int EDIT = 13;
		final int PRINT = 15;
		final int SWAP = 17;
		
		// File menu
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&File");
		final Menu fileMenu = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(fileMenu);
		fileMenu.addListener(SWT.Show,  new Listener() {
			@Override
			public void handleEvent(Event e) {
				MenuItem[] menuItems = fileMenu.getItems();
				menuItems[OPENRUNLAST].setEnabled(m_mru.getLastOperation() != null);
				menuItems[CLOSEINPUT].setEnabled(!m_views.isEmpty());
				menuItems[CLOSEOUTPUT].setEnabled(m_views.hasSecondView());
				menuItems[CLOSEBOTH].setEnabled(m_views.hasSecondView());
				menuItems[SAVE].setEnabled(m_views.hasSecondView());
				menuItems[SAVEAS].setEnabled(m_views.hasSecondView());
				menuItems[SAVEINPAS].setEnabled(!m_views.isEmpty());
				menuItems[EDIT].setEnabled(!m_views.isEmpty());
				menuItems[PRINT].setEnabled(!m_views.isEmpty());
				menuItems[SWAP].setEnabled(m_views.hasSecondView());
			}
		});
		
		// File -> New...
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("&New...\tCtrl+N");
		item.setAccelerator(SWT.MOD1 + 'N');
		setIcon(item, "images/newHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				editFile(null, null, null);
			}
		});
		
		// File -> Open...
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("&Open...\tCtrl+O");
		item.setAccelerator(SWT.MOD1 + 'O');
		setIcon(item, "images/openHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// Get the user to choose an image file.
				FileDialog fileChooser = new FileDialog(m_shell, SWT.OPEN);
				if (m_lastPath != null)
					fileChooser.setFilterPath(m_lastPath);
				fileChooser.setFilterExtensions(ImageFiles.openFilterExtensions());
				fileChooser.setFilterNames(ImageFiles.openFilterNames());
				String filename = fileChooser.open();
				if (filename == null)
					return;
				m_lastPath = fileChooser.getFilterPath();

				m_mru.moveFileNameToTop(-1, filename);
				updateFile(filename);
			}
		});
		
		// File -> Open and Run Last operation
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Open and &Run Last\tCtrl+R");
		item.setAccelerator(SWT.MOD1 + 'R');
		//setIcon(item, "images/newHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String filename = m_mru.getTop();
				if (filename != null) {
					updateFile(filename);
					String lastOperation = m_mru.getLastOperation();
					if (lastOperation != null) {
						if (!m_magb.findAndRun(lastOperation)) {
							m_bver.findAndRun(lastOperation);
						}
					}
				}
			}
		});
		
		// File -> Open Recent ->
		item = new MenuItem(fileMenu, SWT.CASCADE);
		item.setText("Open Recent");
		final Menu recent = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(recent);
		// add most recently used files
		m_mru.addRecentFiles(recent);
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// File -> Close Input
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Close Input");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (m_views.hasSecondView()) {
					// swap images
					swapViews();

					// close output view
					m_views.close(false);
				} else {
					// update title
					m_shell.setText(Picsi.APP_NAME);

					// close input view
					m_views.close(true);
				}
				
				// notify all menus about the closed input
				notifyAllMenus();
			}
		});

		// File -> Close Output
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Close Output");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// close output view
				m_views.close(false);
				// notify all menus about the closed output
				notifyAllMenus();
			}
		});

		// File -> Close Both
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Close Both");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// close output view
				m_views.close(false);
				if (!m_views.isEmpty()) {
					// update title
					m_shell.setText(Picsi.APP_NAME);
					
					// close input view
					m_views.close(true);
				}
				// notify all menus about the closed input and output
				notifyAllMenus();
			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		// File -> Save
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("&Save Output\tCtrl+S");
		item.setAccelerator(SWT.MOD1 + 'S');
		setIcon(item, "images/saveHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (m_views.hasSecondView()) saveFile(false, false);
			}
		});
		
		// File -> Save As...
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Save Output As...");
		setIcon(item, "images/saveAsHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (m_views.hasSecondView()) saveFile(false, true);
			}
		});
		
		// File -> Save Input As...
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Save Input As...");
		setIcon(item, "images/saveAsHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) saveFile(true, true);
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// File -> Edit...
		m_editMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		m_editMenuItem.setText("&Edit...\tCtrl+E");
		m_editMenuItem.setAccelerator(SWT.MOD1 + 'E');
		setIcon(m_editMenuItem, "images/editHS.png");
		m_editMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					Document doc = m_views.getDocument(true);
					View view = m_views.getView(true);
					
					if (m_views.hasSecondView()) {
						// ask the user to specify the image to print
						Object[] filterTypes = { "Input", "Output" };
						int o = OptionPane.showOptionDialog("Choose the image to edit", SWT.ICON_INFORMATION, filterTypes, 0);
						if (o < 0) return;
						if (o > 0) {
							view = m_views.getView(false);
							doc = m_views.getDocument(false);
							String filename = doc.getFileName();
							if (filename == null) {
								// must be saved before
								if (!saveFile(false, true)) return;
							}
						}
					}
					
					String filename = doc.getFileName();
					if (filename != null) {
						editFile(doc, view.getImageData(), filename);
					}
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// File -> Print
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("&Print...\tCtrl+P");
		item.setAccelerator(SWT.MOD1 + 'P');
		setIcon(item, "images/printHS.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					View view = m_views.getView(true);
					Document doc = m_views.getDocument(true);
				
					if (m_views.hasSecondView()) {
						// ask the user to specify the image to print
						Object[] filterTypes = { "Input", "Output" };
						int o = OptionPane.showOptionDialog("Choose the image to print", SWT.ICON_INFORMATION, filterTypes, 0);
						if (o < 0) return;
						if (o > 0) {
							view = m_views.getView(false);
							doc = m_views.getDocument(false);
						}
					}
					
					// Ask the user to specify the printer.
					PrintDialog dialog = new PrintDialog(m_shell, SWT.NONE);
					PrinterData printerData = view.getPrinterData();
					if (printerData != null) dialog.setPrinterData(printerData);
					printerData = dialog.open();
					if (printerData == null) return;
					
					Throwable ex = view.print(m_display);
					if (ex != null) {
						showErrorDialog("printing", doc.getFileName(), ex);					
					}
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);

		// File -> Swap I/O
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("Swap &Images\tCtrl+I");
		item.setAccelerator(SWT.MOD1 + 'I');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (m_views.hasSecondView()) {
					// swap images
					swapViews();
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// File -> Exit
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("E&xit");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				m_shell.close();
			}
		});
	}
	
	// MAGB menu
	private void createMagbMenu(Menu menuBar) {
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&MAGB");
		final Menu imageMenu = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(imageMenu);
		imageMenu.addListener(SWT.Show,  new Listener() {
			@Override
			public void handleEvent(Event e) {
				MenuItem[] menuItems = imageMenu.getItems();
				for (int i=0; i < menuItems.length; i++) {
					menuItems[i].setEnabled(!m_views.isEmpty() && m_magb.isEnabled(i));
				}
			}
		});

		// user defined image menu items
		m_magb.createMenuItems(imageMenu);
	}
	
	// BVER menu
	private void createBverMenu(Menu menuBar) {
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&BVER");
		final Menu imageMenu = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(imageMenu);
		imageMenu.addListener(SWT.Show,  new Listener() {
			@Override
			public void handleEvent(Event e) {
				MenuItem[] menuItems = imageMenu.getItems();
				for (int i=0; i < menuItems.length; i++) {
					menuItems[i].setEnabled(!m_views.isEmpty() && m_bver.isEnabled(i));
				}
			}
		});

		// user defined image menu items
		m_bver.createMenuItems(imageMenu);
	}
	
	// Window menu
	private void createWindowMenu(Menu menuBar) {
		final int AUTO_ZOOM = 0;
		final int ORIGINAL_SIZE = 1;
		final int SYNCHRONIZE = 2;
		final int SHOWOUTPUT = 4;
		final int SHOWCOLORTABLE = 6;
		final int SHOWHISTOGRAM = 7;
		final int SHOWLINE = 8;
		final int SHOWPSNR = 9;
		final int SHOWWAVES = 10;
		
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&Window");
		final Menu windowMenu = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(windowMenu);
		windowMenu.addListener(SWT.Show,  new Listener() {
			@Override
			public void handleEvent(Event e) {
				MenuItem[] menuItems = windowMenu.getItems();
				menuItems[AUTO_ZOOM].setEnabled(!m_views.isEmpty());
				menuItems[AUTO_ZOOM].setSelection(m_views.hasAutoZoom());
				menuItems[ORIGINAL_SIZE].setEnabled(!m_views.isEmpty());
				menuItems[SYNCHRONIZE].setEnabled(!m_views.isEmpty());
				menuItems[SYNCHRONIZE].setSelection(m_views.isSynchronized());
				menuItems[SHOWOUTPUT].setEnabled(!m_views.isEmpty());
				menuItems[SHOWOUTPUT].setSelection(m_views.hasSecondView());
				menuItems[SHOWCOLORTABLE].setEnabled(!m_views.isEmpty() && (m_views.getFirstImageType() != Picsi.IMAGE_TYPE_RGB ||
						(m_views.hasSecondView() && (m_views.getSecondImageType() != Picsi.IMAGE_TYPE_RGB))
				));
				menuItems[SHOWCOLORTABLE].setSelection(m_views.hasColorTable());		
				menuItems[SHOWHISTOGRAM].setEnabled(!m_views.isEmpty());
				menuItems[SHOWHISTOGRAM].setSelection(m_views.hasHistogram());		
				menuItems[SHOWLINE].setEnabled(!m_views.isEmpty());
				menuItems[SHOWLINE].setSelection(m_views.hasLineViewer());						
				menuItems[SHOWPSNR].setEnabled(!m_views.isEmpty() && m_views.hasSecondView() && m_views.getFirstImageType() == m_views.getSecondImageType() 
						&& m_views.getView(true).getImageHeight() == m_views.getView(false).getImageHeight()
						&& m_views.getView(true).getImageWidth() == m_views.getView(false).getImageWidth()
				);
				menuItems[SHOWWAVES].setEnabled(!m_views.isEmpty() && m_views.getFirstImageType() == Picsi.IMAGE_TYPE_GRAY);
				menuItems[SHOWWAVES].setSelection(m_views.hasWaves());		
			}
		});

		// Window -> Auto Zoom
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("&Auto Zoom\tCtrl+A");
		item.setAccelerator(SWT.MOD1 + 'A');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					MenuItem item = (MenuItem)event.widget;
					m_views.setAutoZoom(item.getSelection());
				}
			}
		});

		// Window -> Original Size
		item = new MenuItem(windowMenu, SWT.PUSH);
		item.setText("Original Si&ze\tCtrl+Z");
		item.setAccelerator(SWT.MOD1 + 'Z');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) m_views.zoom100();
			}
		});

		// Window -> Synchronize
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("&Synchronize");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) m_views.synchronize();
			}
		});

		new MenuItem(windowMenu, SWT.SEPARATOR);

		// Window -> Show Output
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("Sho&w Output\tCtrl+W");
		item.setAccelerator(SWT.MOD1 + 'W');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) m_views.split();
			}
		});

		new MenuItem(windowMenu, SWT.SEPARATOR);

		// Window -> Show Color Table
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("Show &Color Table...\tCtrl+C");
		item.setAccelerator(SWT.MOD1 + 'C');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					m_views.toggleColorTable();
				}
			}
		});

		// Window -> Show Histogram
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("Show &Histogram...\tCtrl+H");
		item.setAccelerator(SWT.MOD1 + 'H');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					m_views.toggleHistogram();
				}
			}
		});

		// Window -> Show Line Viewer
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("Show &Line...\tCtrl+L");
		item.setAccelerator(SWT.MOD1 + 'L');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					m_views.toggleLineViewer();
				}
			}
		});

		// Window -> Show PSNR
		item = new MenuItem(windowMenu, SWT.PUSH);
		item.setText("Show &PSNR...\tCtrl+Q");
		item.setAccelerator(SWT.MOD1 + 'Q');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty()) {
					ImageData imageData1 = m_views.getFirstImage();
					ImageData imageData2 = m_views.getSecondImage();
					int imageType = m_views.getFirstImageType(); assert imageType == m_views.getSecondImageType();
					double[] psnr = ImageProcessing.psnr(imageData1, imageData2, imageType);
					MessageBox box = new MessageBox(Picsi.s_shell, SWT.OK);
					
					box.setText("PSNR");
					if (psnr != null) {
						if (imageType == Picsi.IMAGE_TYPE_INDEXED || imageType == Picsi.IMAGE_TYPE_RGB) {
							box.setMessage(Picsi.createMsg("Red: {0}, Green: {1}, Blue: {2}", new Object[] { psnr[0], psnr[1], psnr[2] }));
						} else {
							box.setMessage("PSNR: " + psnr[0]);
						}
					}
					box.open();
				}
			}
		});

		// Window -> Show Waves Editor
		item = new MenuItem(windowMenu, SWT.CHECK);
		item.setText("Show Wa&ves...\tCtrl+V");
		item.setAccelerator(SWT.MOD1 + 'V');
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!m_views.isEmpty() && m_views.getFirstImageType() == Picsi.IMAGE_TYPE_GRAY) {
					m_views.toggleWaves();
				}
			}
		});
	}
	
	// Help menu
	private void createHelpMenu(Menu menuBar) {
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&Help");
		final Menu helpMenu = new Menu(m_shell, SWT.DROP_DOWN);
		item.setMenu(helpMenu);

		// Help -> About
		item = new MenuItem(helpMenu, SWT.PUSH);
		item.setText("About...");
		//item.setID(SWT.ID_ABOUT);
		setIcon(item, "images/picsi.png");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				MessageBox box = new MessageBox(m_shell, SWT.OK);
				box.setText("About " + Picsi.APP_NAME);
				box.setMessage(Picsi.APP_COPYRIGHT + "\n\nVersion: " + Picsi.APP_VERSION + "\n\nWeb: " + Picsi.APP_URL);
				box.open();
			}
		});
	}
	
	private boolean saveFile(boolean first, boolean saveAs) {
		final int imageType = m_views.getView(first).getImageType();
		FileInfo si = null;
		
		if (saveAs || m_views.getDocument(first).getFileName() == null) {
			si = chooseFileName(imageType);
			if (si == null) return false;
		}
		
		m_shell.setCursor(m_display.getSystemCursor(SWT.CURSOR_WAIT));
		
		try {
			if (si != null) {
				m_views.save(first, si.filename, si.fileType);
				setTitle(si.filename, si.fileType);
			} else {
				assert m_views.getDocument(first).getFileName() != null : "doc has no filename";
				m_views.save(first, null, -1);
			}
			return true;
		} catch (Throwable e) {
			showErrorDialog("saving", (si != null) ? si.filename : m_views.getDocument(first).getFileName(), e);
			return false;
		} finally {
			m_shell.setCursor(m_display.getSystemCursor(SWT.CURSOR_CROSS));
			m_views.refresh(false);
		}
	}
	
	private void setTitle(String filename, int fileType) {
		m_shell.setText(Picsi.createMsg(Picsi.APP_NAME + " - {0} ({1} {2})", new Object[]{filename, Picsi.imageTypeString(m_views.getFirstImageType()), ImageFiles.fileTypeString(fileType)}));		
	}
	
	private void setIcon(Item item, String resourceName) {
		try {
			item.setImage(new Image(m_display, getClass().getClassLoader().getResource(resourceName).openStream()));			
		} catch(IOException e) {
		}
	}
	
	private void editFile(Document doc, ImageData imageData, String path) {
		if (m_editor == null) {
			m_editor = new Editor(this);
		}
		if (path == null) {
			m_editor.newFile();
		} else {
			if (doc.isBinaryFormat()) {
				m_editor.openBinaryFile(doc, imageData, path);
			} else {
				m_editor.openFile(path);
			}
		}
		m_editor.setVisible(true);
	}
	
	private void swapViews() {
		// current output view will become input view
		// save output
		Document doc = m_views.getDocument(false);
		String filename = doc.getFileName();
		if (filename == null) {
			// must be saved before
			if (!saveFile(false, true)) return;
			filename = doc.getFileName();
		}
		
		// swap images
		m_views.swapImages();
		
		// update title
		setTitle(filename, doc.getfileType());		
	}
}
