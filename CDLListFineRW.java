package com;

import java.util.concurrent.atomic.AtomicInteger;

import com.CDLList.Element;

public class CDLListFineRW<T>{
	public Element head;
	public Element psuedo_head;
	RWLock read_write_lock = new RWLock(); 
	
	public class Element{
		public T data;
		public Element next;
		public Element prev; 
		public AtomicInteger total_readers = new AtomicInteger();
		public AtomicInteger total_writers = new AtomicInteger();
		public AtomicInteger wish_writers = new AtomicInteger();
		
		public Element(){
			this.data = null; 
			this.next = this;
			this.prev = this;
		}
		
		public T value() {
			return this.data;
		}
	}
	
	public CDLListFineRW(T v) {
		this.head = new Element();
		this.head.data = v;
		this.head.next = this.head;
		this.head.prev = this.head;
		this.psuedo_head = new Element();
		psuedo_head.next = this.head;
		psuedo_head.prev = this.head;
        this.head.prev = psuedo_head;
        this.head.next = psuedo_head;
	}

	public Element head() {
		return this.head;
	}
	
	public Cursor reader(Element node) {
		//System.out.println( Thread.currentThread().getId() + " Inside Reader");
		Cursor c = new Cursor();
		c.current_element= node;
		return c;
	}

	public class Cursor{
		Element current_element = null; 

		public Element current() {
			return current_element;
		}

		public void previous() {
			Element previous_element = new Element();
			if (current_element == head){
				previous_element = (Element) current_element.prev.prev;
			}
			else{
				previous_element = (Element) current_element.prev;
			}
			try {
				read_write_lock.lockRead(previous_element);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.current_element = previous_element;
			read_write_lock.unlockRead(previous_element);
		}

		public void next() {
			//System.out.println(Thread.currentThread().getId() + "Inside next");
			Element next_element = new Element();
			if (current_element == head){
				next_element = (Element) current_element.next.next;
			}
			else{
				next_element = (Element) current_element.next;
			}
			try {
				read_write_lock.lockRead(next_element);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.current_element = next_element;
			read_write_lock.unlockRead(next_element);
		}

		public Writer writer() {
			Writer writer_object= new Writer();  
			writer_object.current_element = this.current_element; 
			return writer_object;
		}
	}

	public class Writer {
		Element current_element;

		// Add before the current element.
		public boolean insertBefore(T val) {
			//System.out.println(Thread.currentThread().getId() + " Inside insertbefore");
			Element temp_previous =  (Element) current_element.prev;
			if (current_element == head){
				current_element = psuedo_head;
				temp_previous = (Element) current_element.prev;
			}
			try {
				read_write_lock.lockWrite(temp_previous);
				read_write_lock.lockWrite(current_element);
				if(temp_previous == current_element.prev){
					Element new_element = new Element(); 
					new_element.data = val;
					current_element.prev.next = new_element;
					new_element.next = current_element;
					new_element.prev = current_element.prev;
					current_element.prev = new_element;
					//System.out.println(Thread.currentThread().getId() + " Inserted a new element before: "+current_element.data+" New Element's Data: "+new_element.data);
				}
				else{
					//System.out.println("Majja Awanu!! code fatanu! - in previous");
				}
				read_write_lock.unlockWrite(current_element);
				read_write_lock.unlockWrite(temp_previous);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}				
			return true;
		}

		// Add after the current element.
		public boolean insertAfter(T val) {
			//System.out.println(Thread.currentThread().getId() + " Inside insertafter");
			Element temp_next =  (Element) current_element.next;
			try {
				read_write_lock.lockWrite(current_element);
				read_write_lock.lockWrite(temp_next);
				if(temp_next == current_element.next)
				{
					Element new_element = new Element(); 
					new_element.data = val;

					current_element.next.prev = new_element; 
					new_element.prev = current_element;
					new_element.next = current_element.next;
					current_element.next = new_element; 

					//System.out.println(Thread.currentThread().getId() + " Inserted a new element after: "+current_element.data+" New Element's Data: "+new_element.data);
				}
				else
				{
					//System.out.println("Tmp_next: "+temp_next);
					//System.out.println("current_element: "+current_element.next);										
					//System.out.println("Majja Awanu!! code fatanu! - in next");
				}
				read_write_lock.unlockWrite(temp_next);
				read_write_lock.unlockWrite(current_element);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	
	public class RWLock{
		public synchronized boolean lockRead(Element temp) throws InterruptedException{
			//System.out.println(Thread.currentThread().getId()+" wants to read");
			while(temp.total_writers.get() > 0 || temp.wish_writers.get() > 0){
				//System.out.println(Thread.currentThread().getId() + " Thread wants to read. Wait till there exists a thread which is performing a write.");
				wait();
			}
			temp.total_readers.getAndIncrement();
			return true;
		}

		public synchronized boolean unlockRead(Element temp){
			//System.out.println(Thread.currentThread().getId()+" done with reading");
			temp.total_readers.getAndDecrement();
			notifyAll();
			return true;
		}

		public synchronized boolean lockWrite(Element temp) throws InterruptedException{
			//System.out.println(Thread.currentThread().getId()+" wants to write");
			temp.wish_writers.getAndIncrement();
			while(temp.total_readers.get() > 0 || temp.total_writers.get() > 0){
				//System.out.println(Thread.currentThread().getId() + " Thread wants to write. Making it wait till other threads are done with reads and writes.");	
				wait();
			}
			temp.wish_writers.getAndDecrement();
			temp.total_writers.incrementAndGet();
			return true;
		}

		public synchronized boolean unlockWrite(Element temp) throws InterruptedException{
			//System.out.println(Thread.currentThread().getId()+" done with writing");
			//System.out.println("unlockWrite: "+temp.total_writers);
			temp.total_writers.getAndDecrement();
			notifyAll();
			return true;
		}
	}


	public void traverse() {
		//System.out.println("I am here");
		Element temp;
		temp=head;
		int i=1;
		System.out.println("*******************************************************************");
		System.out.println(i+" :"+temp.data);
		temp=temp.next;
		i++;
		while(temp!=head()){
			System.out.println(i+" :"+temp.data);
			temp = temp.next;
			i++;
		}
	}
}
