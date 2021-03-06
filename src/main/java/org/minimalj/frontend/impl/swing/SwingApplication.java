package org.minimalj.frontend.impl.swing;

import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

public class SwingApplication implements Runnable {

	private SwingApplication() {
		// private
	}

	/**
	 * Initializes application and opens a new frame
	 * 
	 */
	@Override
	public void run() {
		FrameManager.setSystemLookAndFeel();
		Frontend.setInstance(new SwingFrontend());
		Backend.setInstance(new SwingBackend(Backend.getInstance()));
		
		FrameManager.getInstance().openNavigationFrame(null);
	}

	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);

		SwingUtilities.invokeAndWait(new SwingApplication());
	}
	
}
