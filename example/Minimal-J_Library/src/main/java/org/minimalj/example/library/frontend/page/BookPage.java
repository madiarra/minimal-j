package org.minimalj.example.library.frontend.page;

import org.minimalj.example.library.frontend.form.BookForm;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ObjectPage;

public class BookPage extends ObjectPage<Book> {

	public BookPage(Book book) {
		super(book);
	}
	
	@Override
	protected Form<Book> createForm() {
		return new BookForm(false);
	}

	@Override
	public String getTitle() {
		return "Buch " + getObject().title;
	}
}
