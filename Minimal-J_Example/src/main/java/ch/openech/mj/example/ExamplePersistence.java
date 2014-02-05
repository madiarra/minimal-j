package ch.openech.mj.example;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.ImmutableTable;
import ch.openech.mj.db.Table;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.BookIdentification;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.CustomerIdentification;
import ch.openech.mj.example.model.Lend;

public class ExamplePersistence extends DbPersistence {

	public final ImmutableTable<BookIdentification> bookIdentification;
	public final Table<Book> book;
	
	public final ImmutableTable<CustomerIdentification> customerIdentification;
	public final Table<Customer> customer;
	
	public final Table<Lend> lend;
	
	public ExamplePersistence() {
		super(DbPersistence.embeddedDataSource());
		
		bookIdentification = addImmutableClass(BookIdentification.class);
		book = addClass(Book.class);

		customerIdentification = addImmutableClass(CustomerIdentification.class);
		customer = addClass(Customer.class);

		lend = addClass(Lend.class);
	}
	
}
