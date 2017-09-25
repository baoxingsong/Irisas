package me.songbx.action.parallel.model;

import java.util.HashMap;

import me.songbx.service.AnnotationReadIncludeOrfLostService;

public class AnnotationIncludeOrfLostReadReferences {
	HashMap<String, AnnotationReadIncludeOrfLostService> annotationReadReferences = new HashMap<String, AnnotationReadIncludeOrfLostService>();

	public synchronized HashMap<String, AnnotationReadIncludeOrfLostService> getAnnotationReadReferences() {
		return annotationReadReferences;
	}

	public synchronized void setAnnotationReadReferences(
			HashMap<String, AnnotationReadIncludeOrfLostService> annotationReadReferences) {
		this.annotationReadReferences = annotationReadReferences;
	}
	public synchronized void put(String s, AnnotationReadIncludeOrfLostService a){
		annotationReadReferences.put(s, a);
	}
	public synchronized AnnotationReadIncludeOrfLostService get(String s){
		return annotationReadReferences.get(s);
	}
	public synchronized boolean containsKey(String name){
		return annotationReadReferences.containsKey(name);
	}
}
