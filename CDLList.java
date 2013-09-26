package com;

//import com.CDLList.Element;

public class CDLList<T> {
	 public Element head;
	
	// In this constructor, we create HEAD element of the CDLL.
	public CDLList(T v){
		if (head == null){
			this.head = new Element();
			this.head.data = v;
			this.head.next = this.head;
			this.head.previous = this.head;
		}
	}
	
	// Each node will look like this
	public class Element {
		public T data;
		public Element next;
		public Element previous;
		
		public T value(){
			return this.data;
		}
	}
	
	// Cursor is used for traversing the list
	public class Cursor {
		protected Element current_element;
		
		// returns the current element held by the cursor
		public Element current(){
			return current_element;
		}
		
		// move the cursor to next element
		public void next(){
			// update the current element in the cursor to its immediate next element
			this.current_element = current_element.next;
		}
		
		// move the cursor to the previous element
		public void previous(){
			this.current_element = current_element.previous;
		}
		
		public Writer writer(){
			// return the writer object for the current element's cursor
			// put cursor's current_element into Writer
			Writer writer_object = new Writer();
			writer_object.current_element = this.current_element;
			return writer_object;
		}
	}
	
	public class Writer {
		// Writer object has a current_element stored within it. 
		protected Element current_element;
		
		public boolean insertAfter(T value){
			// create a new node 
			Element new_element = new Element();
			new_element.data = value;
			
			// update "previous" of 3rd element
			current_element.next.previous = new_element;
			new_element.next = current_element.next;
			new_element.previous = current_element;
			// update "next" of 1st element
			current_element.next = new_element;
			traverse();
			return true;
		}
		
		public boolean insertBefore(T value){
			// create a new node 
			Element new_element = new Element();
			new_element.data = value;
			
			// update "next" of the current_element's previous
			current_element.previous.next = new_element;
			new_element.next = current_element;
			new_element.previous = current_element.previous;
			// update "previous" of the current_element
			current_element.previous = new_element;
			traverse();
			return true;
		}
	}
	
	// return cursor for the element
	// I am expecting that the node received by the function below 
	// is going to be a head always
	public Cursor reader(Element node){
		Cursor c = new Cursor();
		c.current_element = node;
		return c;
	}
	
	// return head element of the list
	public Element head(){
		if(this.head==null)
			System.out.println("Head data is null");
		return this.head;
	}
	
	// traverse the list
	public void traverse(){
		// get the head node and traverse the list till its end
		Cursor traverser = this.reader(this.head);
		int i=1;
		System.out.println("*******************************************************************************");
		// display head element first
		System.out.println( i + " : " +  traverser.current_element.value());
		i++;
		traverser.next();
		do{
			System.out.println( i + " : " +  traverser.current_element.value());
			traverser.next();
			i++;
		}while(traverser.current_element != this.head);
	}
}