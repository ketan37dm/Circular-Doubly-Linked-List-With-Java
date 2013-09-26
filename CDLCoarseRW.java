package com;

import java.util.concurrent.atomic.AtomicInteger;

public class CDLCoarseRW<T> extends CDLList<T> {
	private AtomicInteger total_readers = new AtomicInteger(), total_writers = new AtomicInteger(), wish_writers = new AtomicInteger();
	
	RWLock lock_manager = new RWLock(); 
	
	public CDLCoarseRW(T v) {
		super(v);
	}

	// TODO : WHY DONT WE HAVE SYNCHRONIZED THIS BLOCK ?
	public Cursor reader(Element node) {
		Cursor c = new Cursor();
		c.current_element = node;
		return c;
	}
	
	public class Cursor extends CDLList<T>.Cursor{
		// Return the current element.
		public Element current() {
			Element cursor_current_element = new Element();
			try {
				lock_manager.lockRead();
				cursor_current_element = this.current_element;
				lock_manager.unlockRead();
			} catch (InterruptedException e) {
				//System.out.println("Inside Cursor-Current");
				e.printStackTrace();
			}	
				return cursor_current_element;
		}
		
		// Move to the previous element.
		public void previous() {
			try{
				lock_manager.lockRead(); 
				this.current_element = current_element.previous;
				lock_manager.unlockRead();
			}catch (InterruptedException e){
				//System.out.println("Inside Cursor-Previous");
				e.printStackTrace();
			}
		}

		// Move to the next element
		public void next() {
			try{
				lock_manager.lockRead();
				this.current_element = current_element.next;
				lock_manager.unlockRead();
			}catch (InterruptedException e){
				//System.out.println("Inside Cursor-Next");
				e.printStackTrace();
			}
		}
		
		// TODO : WHY DONT WE HAVE SYNCHRONIZED THIS BLOCK ?
		// Returns a writer at the current element
		public Writer writer() {
			Writer writer_object = new Writer(); 
			writer_object.current_element = this.current_element; 	
			return writer_object;
		}
		
	}
	
	public class Writer extends CDLList<T>.Writer {
		// Add before the current element.
		public boolean insertBefore(T value) {
			try {
				lock_manager.lockWrite();
				Element new_element = new Element();
				new_element.data = value;
				
				// update "next" of the current_element's previous
				current_element.previous.next = new_element;
				new_element.next = current_element;
				new_element.previous = current_element.previous;
				// update "previous" of the current_element
				current_element.previous = new_element;
				
				//System.out.println("Inserted a new element before: "+ current_element.data + " New Element's Data: "+ new_element.data);
				lock_manager.unlockWrite();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		// Add after the current element.
		public boolean insertAfter(T value) {
			try {
				lock_manager.lockWrite();
				Element new_element = new Element(); 	    
		 		new_element.data = value;
							
		 	// update "previous" of 3rd element
				current_element.next.previous = new_element;
				new_element.next = current_element.next;
				new_element.previous = current_element;
				// update "next" of 1st element
				current_element.next = new_element;	
		
				//System.out.println("Inserted a new element after: "+new_element.data+" New Element's Data: "+new_element.data);
				lock_manager.unlockWrite();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
	}
		
	public class RWLock{
		public synchronized boolean lockRead() throws InterruptedException{
			while(total_writers.get() > 0 || wish_writers.get() > 0){
				//System.out.println("Thread wants to read. Wait till there exists a thread which is performing a write.");
				wait();
			}
			total_readers.getAndIncrement();
			return true;
		}

		public synchronized boolean unlockRead(){
			total_readers.getAndDecrement();
			notifyAll();
			return true;
		}

		public synchronized boolean lockWrite() throws InterruptedException{
			wish_writers.getAndIncrement();
			while(total_readers.get() > 0){
				//System.out.println("Thread wants to write. Making it wait till other threads are done with reads and writes.");	
				wait();
			}
			wish_writers.getAndDecrement();
			total_writers.getAndIncrement();
			return true;
		}

		public synchronized boolean unlockWrite() throws InterruptedException{
			//System.out.println("unlockWrite: "+ total_writers);
			total_writers.getAndDecrement();
			notifyAll();
			return true;
		}
	}
}