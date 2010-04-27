package org.uriplay.beans;

import java.util.Set;

import org.jherd.beans.PropertyMergeException;
import org.jherd.beans.Representation;
import org.jherd.beans.UriPropertySource;
import org.jherd.naming.ResourceMapping;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.content.MutableContentStore;

import com.hp.hpl.jena.sparql.util.Named;

/**
 * Resource mapping for entities that are {@link Named} by uri.
 * 
 * @author Robert Chatley
 * @author Lee Denison
 */
public class DescriptionResourceMapping implements ResourceMapping, UriPropertySource {

	ResourceMapping reserved;
	
	private MutableContentStore contentStore;
	
	public DescriptionResourceMapping(MutableContentStore contentStore) {
		this.contentStore = contentStore;
	}
	
	public DescriptionResourceMapping() {

	}

	public void setReserved(ResourceMapping reserved) {
		this.reserved = reserved;
	}
	
	public boolean canMatch(String uri) {
		return !isReserved(uri);
	}

	public Object getResource(String uri) {
		if (contentStore == null) {
			return null;
		}
		return contentStore.findByUri(uri);
	}

	public String getUri(Object bean) {
		if (bean instanceof Description) {
			Description description = (Description) bean;
			return description.getCanonicalUri();
		}
		
		return null;
	}

	public Set<String> getUris(Object bean) {
		if (bean instanceof Description) {
			return ((Description) bean).getAllUris();
		}
		
		return null;
	}

	public boolean isReserved(String uri) {
		if (reserved == null) {
			return false;
		} else {
			return reserved.isReserved(uri);
		}
	}

	public void merge(Representation representation, String docId) throws PropertyMergeException {
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("canonicalUri", docId);
		representation.addValues(docId, mpvs);

	}

}