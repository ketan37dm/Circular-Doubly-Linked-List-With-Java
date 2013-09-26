package com;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

//import com.CDLListFineRW.Cursor;
import com.CDLCoarseRW.Cursor;
import com.CDLList.Element;

public class tests {

	@Test
    public void test1(){
//        USE THIS FOR COARSELIST AND FINELIST WITH MODIFICATIONS
		CDLCoarseRW<String> list = new CDLCoarseRW<String>("hi");
        Element head = (Element) list.head();
        com.CDLCoarseRW.Cursor c = list.reader(list.head());
        
        for(int i = 74; i >= 65; i--) {
            char val = (char) i;
            c.writer().insertAfter("" + val);
        }
        
        List<Thread> threadList = new ArrayList<Thread>();
        for (int i = 0; i < 25; i++) {
            NormalThread nt = new NormalThread(list, i);
            threadList.add(nt);
        }
            
	RandomThread rt = new RandomThread(list);
	threadList.add(rt);
	
        try {
            for(Thread t : threadList){
            	t.start();
            }
            for (Thread t : threadList) {
            	t.join();
            }
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
//    YOU MAY WANT TO INCLUDE A PRINT METHOD TO VIEW ALL THE ELEMENTS
//        list.print();
        
        list.traverse();
    }
    
    
    
}



