package me.songbx.util.exception;

public class codingNotThree extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6923363648028754302L;
	public codingNotThree() {
		super("The length of DNA genetic is not coding ");
	}
	public codingNotThree(String message) {
		super(message);
	}
}
