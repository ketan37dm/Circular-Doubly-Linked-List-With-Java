package com;

public class CDLFine<T> extends CDLList<T>{
	// To avoid deadlock in the list we are introducing a pseudo tail element
	// This element is not actually part of the list but helps avoid deadlock situations
	public Element psuedo_head;
	
	CDLFine(T v){
		// call to super constructor will create HEAD element of the list
		super(v);
		// create a pseudo head
		this.psuedo_head = new Element();
		psuedo_head.next = this.head();
		psuedo_head.previous = this.head();
		this.head().previous = psuedo_head;
		this.head().next = psuedo_head;
	}
	
	public class Cursor extends CDLList<T>.Cursor{
		public Element current(){
			synchronized(current_element){
				return this.current_element;
			}
		}
		
		public void next(){
			Element next_element = new Element();
			synchronized(current_element){
				if (current_element.next == psuedo_head){
					next_element = current_element.next.next;
				}
				else{
					next_element = current_element.next;
				}
				synchronized(next_element){
					this.current_element = next_element;
				}
			}
		}
		
		public void previous(){
			Element previous_element = new Element();
			if (current_element == head()){
				previous_element = current_element.previous.previous;
			}
			else{
				previous_element = current_element.previous;
			}
			synchronized(previous_element){
				synchronized(current_element){
				this.current_element = previous_element;
				}
			}
		}
		
		public Writer writer(){
			synchronized(current_element){
				Writer writer_object = new Writer();
				writer_object.current_element = this.current_element;
				return writer_object;
			}
		}
	}// Cursor class ends
	
	public class Writer extends CDLList<T>.Writer{
		public boolean insertBefore(T value){
			synchronized(current_element.previous){
				synchronized(current_element){
					// to avoid deadlock
					if (current_element == head()){
						current_element = psuedo_head;
					}
					
					// create a new node 
					Element new_element = new Element();
					new_element.data = value;
					
					// update "next" of the current_element's previous
					current_element.previous.next = new_element;
					new_element.next = current_element;
					new_element.previous = current_element.previous;
					// update "previous" of the current_element
					current_element.previous = new_element;
					return true;
				}
			}
		}// insertBefore ends
		
		public boolean insertAfter(T value){
			synchronized(current_element){
				synchronized(current_element.next){
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
		}// insertAfter ends
	}// Writer class ends
	
	public Cursor reader(Element node){
		synchronized(node){
			Cursor c = new Cursor();
			c.current_element = node;
			return c;
		}
	}
}