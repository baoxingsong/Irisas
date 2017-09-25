package me.songbx.model.Gene;

public class Gene implements Comparable<Gene>{
	String sequence;
	String name;
	int start;
	int end;
	String chromesomeName;
	
	public Gene(int start, int end, String chromesomeName){
		super();
		this.start = start;
		this.end = end;
		this.chromesomeName = chromesomeName;
	}
	public Gene(Gene g){
		this.sequence=g.getSequence();
		this.name=g.getName();
		this.start=g.getStart();
		this.end=g.getEnd();
		this.chromesomeName=g.getChromesomeName();
	}
	public synchronized String getSequence() {
		return sequence;
	}
	public synchronized void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public synchronized String getName() {
		return name;
	}
	public synchronized void setName(String name) {
		this.name = name;
	}
	public synchronized int getStart() {
		return start;
	}
	public synchronized void setStart(int start) {
		this.start = start;
	}
	public synchronized int getEnd() {
		return end;
	}
	public synchronized void setEnd(int end) {
		this.end = end;
	}
	public synchronized String getChromesomeName() {
		return chromesomeName;
	}
	public synchronized void setChromesomeName(String chromesomeName) {
		this.chromesomeName = chromesomeName;
	}
	@Override
	public int compareTo(Gene arg0) {
		Gene e0 = this;
		Gene e1 = arg0;
		if(e0.getStart() == e1.getStart() && e0.getEnd() == e1.getEnd()){
			return 0;
		}else if(e0.getStart() > e1.getStart() || (e0.getStart() == e1.getStart() && e0.getEnd() > e1.getEnd())){
			return 1;
		}else{
			return -1;
		}
	}	
}
