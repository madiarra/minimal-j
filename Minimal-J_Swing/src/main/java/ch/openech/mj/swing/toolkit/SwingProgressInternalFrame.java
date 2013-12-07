package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JProgressBar;

import ch.openech.mj.toolkit.ProgressListener;

public class SwingProgressInternalFrame extends JInternalFrame implements ProgressListener {
	private static final long serialVersionUID = 1L;
	
	private final JProgressBar progressBar;
	
	public SwingProgressInternalFrame(String text) {
		super(text);
		pack();
		
		setLayout(new BorderLayout());
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		add(progressBar, BorderLayout.CENTER);
	}
	
	@Override
	public void showProgress(int value, int maximum) {
		if (value == maximum) {
			setVisible(false);
			dispose();
		} else {
			progressBar.setMaximum(maximum);
			progressBar.setValue(value);
		}
	}

	@Override
	public boolean isCanceled() {
		return false;
	}
	
}
