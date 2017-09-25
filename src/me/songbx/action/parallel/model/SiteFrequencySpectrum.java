package me.songbx.action.parallel.model;

import java.util.HashMap;

public class SiteFrequencySpectrum {
	private HashMap<Integer, Integer> siteFrequencySpectrum = new HashMap<Integer, Integer>();

	public synchronized HashMap<Integer, Integer> getSiteFrequencySpectrum() {
		return siteFrequencySpectrum;
	}

	public synchronized void setSiteFrequencySpectrum(
			HashMap<Integer, Integer> siteFrequencySpectrum) {
		this.siteFrequencySpectrum = siteFrequencySpectrum;
	}
	public synchronized void put(Integer k, Integer v){
		siteFrequencySpectrum.put(k, v);
	}
	public synchronized boolean containsKey(Integer k){
		return siteFrequencySpectrum.containsKey(k);
	}
	public synchronized Integer get(Integer k){
		return siteFrequencySpectrum.get(k);
	}
}
