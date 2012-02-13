package ch.openech.mj.application;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.ObjectPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.toolkit.ClientToolkit;

public abstract class ObjectViewPage<T> extends Page implements RefreshablePage, ObjectPage<T> {

	private T actualObject;
	private FormVisual<T> objectPanel;
	private Object alignLayout;
	
	public ObjectViewPage() {
		super();
	}

	protected abstract T loadObject();

	protected abstract FormVisual<T> createForm();
	
	@Override
	public Object getPanel() {
		if (alignLayout == null) {
			objectPanel = createForm();
			alignLayout = ClientToolkit.getToolkit().createFormAlignLayout(objectPanel.getComponent());
		}
		refresh();
		return alignLayout;
	}
	
	@Override
	public T getObject() {
		return actualObject;
	}

	protected void showObject(T object) {
		objectPanel.setObject(object);
	}
	
	@Override
	public void refresh() {
		showObject(loadObject());
		
//		if (isWorking()) return;
//		
//			execute(new SwingWorker<T, Object>() {
//				@Override
//				protected T doInBackground() throws Exception {
//					return loadObject();
//				}
//				
//				@Override
//				protected void done() {
//					try {
//						actualObject = get();
//						showObject(actualObject);
//					} catch (CancellationException x) {
//						// nothing special, user cancelled operation
//					} catch (InterruptedException ex) {
//						ex.printStackTrace();
//					} catch (ExecutionException ex) {
//						ex.printStackTrace();
//					}
//				}
//			}, "loadObject");
	}
	
}
