package com;

import com.CDLCoarseRW.Cursor;

public class NormalThread extends Thread {
    
	CDLCoarseRW<String> cdl;
    int id;
    CDLCoarseRW<String>.Cursor cursor;
    public NormalThread(CDLCoarseRW<String> list, int id) {
        this.id = id;
        this.cdl = list;
        cursor = (Cursor) list.reader(list.head());
    }

    @Override
    public void run() {

        int offset = id * 2;
        for(int i = 0; i < offset; i++) {
            cursor.next();
        }
        
        cursor.writer().insertBefore("IB - " + id);
        cursor.writer().insertAfter("IA - " + id);
    }

}
