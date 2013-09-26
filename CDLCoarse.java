package com;

public class CDLCoarse<T> extends CDLList<T>{
	// Following object is the LOCK Object. Using this object only we implement the 
	// concept of one lock per list.
	Object lock = new Object();
	
	public CDLCoarse(T v) {
		super(v);
	}
	
	public class Cursor extends CDLList<T>.Cursor{
		// returns the current element held by the cursor
		public Element current(){
			synchronized (lock) {
				return current_element;
			}
		}
		
		// move the cursor to next element
		public void next(){
			// update the current element in the cursor to its immediate next element
			synchronized(lock){
				this.current_element = current_element.next;
			}
		}
		
		// move the cursor to the previous element
		public void previous(){
			// update the current element in the cursor to its immediate previous element
			synchronized(lock){
				this.current_element = current_element.previous;
			}
		}
		
		public Writer writer(){
			// Return a writer for the current element pointed by the cursor
			synchronized(lock){
				Writer writer_object = new Writer();
				writer_object.current_element = this.current_element;
				return writer_object;
			}
		}
	}
	
	public class Writer extends CDLList<T>.Writer{
		public boolean insertAfter(T value){
			// insert a new element in the list after the current element pointed by the writer
			synchronized (lock) {
				// create a new node 
				Element new_element = new Element();
				new_element.data = value;
				
				// update "previous" of 3rd element
				current_element.next.previous = new_element;
				new_element.next = current_element.next;
				new_element.previous = current_element;
				// update "next" of 1st element
				current_element.next = new_element;
				return true;
			}
		}
		
		public boolean insertBefore(T value){
			// insert a new element in the list before the current element pointed by the writer
			synchronized(lock){
				// create a new node 
				Element new_element = new Element();
				new_element.data = value;
				
				// update "next" of the current_element's previous
				current_element.previous.next = new_element;
				new_element.next = current_element;
				new_element.previous = current_element.previous;
				// update "previous" of the current_element
				current_element.previous = new_element;
				//System.out.println("Previous element : " + current_element.data + "New Element : " + new_element.data );
				return true;
			}
		}
	}
	
	public Cursor reader(Element node){
		// Return a cursor for the corresponding element object in the list. 
		synchronized(lock){
			Cursor c = new Cursor();
			c.current_element = node;
			return c;
		}
	}
}