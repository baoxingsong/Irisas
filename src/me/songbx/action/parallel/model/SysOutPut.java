package me.songbx.action.parallel.model;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SysOutPut {
	private PrintWriter outPut;
	public SysOutPut(String file) throws FileNotFoundException{
		outPut = new PrintWriter(file);
	}
	public synchronized void print( String s ){
		outPut.print(s);
		outPut.flush();
	}
	public synchronized void println( String s ){
		outPut.println(s);
		outPut.flush();
	}
	public synchronized void close(){
		outPut.close();
	}
}
