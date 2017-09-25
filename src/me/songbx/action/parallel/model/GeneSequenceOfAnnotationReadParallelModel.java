package me.songbx.action.parallel.model;

import java.util.HashMap;
import me.songbx.service.GeneSequenceOfAnnotationReadService;

public class GeneSequenceOfAnnotationReadParallelModel {
	HashMap<String, GeneSequenceOfAnnotationReadService> annotationReadReferences = new HashMap<String, GeneSequenceOfAnnotationReadService>();

	public synchronized HashMap<String, GeneSequenceOfAnnotationReadService> getAnnotationReadReferences() {
		return annotationReadReferences;
	}

	public synchronized void setAnnotationReadReferences(
			HashMap<String, GeneSequenceOfAnnotationReadService> annotationReadReferences) {
		this.annotationReadReferences = annotationReadReferences;
	}
	
	public synchronized void put(String s, GeneSequenceOfAnnotationReadService a){
		annotationReadReferences.put(s, a);
	}
	public synchronized GeneSequenceOfAnnotationReadService get(String s){
		return annotationReadReferences.get(s);
	}
	public synchronized boolean containsKey(String name){
		return annotationReadReferences.containsKey(name);
	}
	
}
