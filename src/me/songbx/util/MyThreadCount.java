package me.songbx.util;

public class MyThreadCount {
	private int count;  
    public MyThreadCount(int count){  
        this.count = count;  
    }  
    public synchronized void countDown(){  
        count--;
    }
    public synchronized void plusOne(){  
        count++;
    }
    public synchronized boolean hasNext(){
        return (count > 0);
    }
    public synchronized int getCount() {
        return count;
    }
    public synchronized void setCount(int count) {  
        this.count = count;  
    }
}
