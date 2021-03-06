package org.minimalj.example.notes;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class NewNoteEditor extends NewObjectEditor<Note> {

	@Override
	protected Form<Note> createForm() {
		return new NoteForm();
	}

	@Override
	protected Note save(Note object) {
		return Backend.save(object);
	}
	
	@Override
	protected void finished(Note newNote) {
		Frontend.show(new NoteTablePage());
	}

	private static class NoteForm extends Form<Note> {
		
		public NoteForm() {
			line(Note.$.date);
			line(Note.$.text);
		}
	}
}
