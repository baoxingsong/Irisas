package me.songbx.action.parallel.model;

import java.util.HashMap;

import me.songbx.service.AnnotationReadService;

public class AnnotationReadReferences {
	HashMap<String, AnnotationReadService> annotationReadReferences = new HashMap<String, AnnotationReadService>();

	public synchronized HashMap<String, AnnotationReadService> getAnnotationReadReferences() {
		return annotationReadReferences;
	}

	public synchronized void setAnnotationReadReferences(
			HashMap<String, AnnotationReadService> annotationReadReferences) {
		this.annotationReadReferences = annotationReadReferences;
	}
	public synchronized void put(String s, AnnotationReadService a){
		annotationReadReferences.put(s, a);
	}
	public synchronized AnnotationReadService get(String s){
		return annotationReadReferences.get(s);
	}
	public synchronized boolean containsKey(String name){
		return annotationReadReferences.containsKey(name);
	}
}
