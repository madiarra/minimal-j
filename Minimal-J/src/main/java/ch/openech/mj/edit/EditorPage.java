package ch.openech.mj.edit;

import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public class EditorPage extends Page implements EditorFinishedListener {

	private final Editor<?> editor;
	private final FormVisual<?> form;
	private final IComponent layout;
	
	public EditorPage(PageContext context, String[] editorClassAndArguments) {
		this(context, createEditor(editorClassAndArguments));
	}
	
	public EditorPage(PageContext context, String editorClass) {
		this(context, createEditor(editorClass));
	}
	
	static Editor<?> createEditor(String... editorClassAndArguments) {
		try {
			Class<?> clazz = Class.forName(editorClassAndArguments[0]);
			if (editorClassAndArguments.length > 1) {
				Class<?>[] argumentClasses = new Class[editorClassAndArguments.length - 1];
				Object[] arguments = new Object[editorClassAndArguments.length - 1];
				for (int i = 0; i<argumentClasses.length; i++) {
					argumentClasses[i] = String.class;
					arguments[i] = editorClassAndArguments[i + 1];
				}
				return (Editor<?>) clazz.getConstructor(argumentClasses).newInstance(arguments);
			} else {
				return (Editor<?>) clazz.newInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException("EditorPage Erstellung fehlgeschlagen", x);
		}
	}
	
	protected EditorPage(PageContext context, Editor<?> editor) {
		super(context);
		this.editor = editor;
		form = editor.startEditor();
		layout = ClientToolkit.getToolkit().createEditorLayout(editor.getInformation(), form, editor.getActions());

		setTitle(editor.getTitle());
		editor.setEditorFinishedListener(this);
	}
	
	@Override
	public boolean isExclusive() {
		return true;
	}

	@Override
	public IComponent getPanel() {
		return layout;
	}

	@Override
	public void finished() {
		close();
	}
	
	public void checkedClose() {
		editor.checkedClose();
	}
	
	protected FormVisual getFormVisual() {
		return form;
	}

}
