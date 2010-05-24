package org.uriplay.beans.html;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import junit.framework.TestCase;

import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Sets;

public class HtmlTranslatorTest extends TestCase {

	public void testWritesSingleItem() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Item item = new Item();
		item.setTitle("Andrew Marr");
		item.setDescription("Sunday morning interview show");
		item.setCanonicalUri("http://www.bbc.co.uk/tomorrowsworld");
		item.setGenres(Sets.newHashSet("http://uriplay.org/genres/comedy"));
		item.setPublisher("bbc.co.uk");
		
		Version version = new Version();
		item.addVersion(version);
		version.setDuration(999);
		
		Version version2 = new Version();
		item.addVersion(version2);
		version2.setDuration(185);
		
		Encoding encoding = new Encoding();
		version.addManifestedAs(encoding);
		
		Location location = new Location();
		encoding.addAvailableAt(location);
		
		location.setUri("http://www.bbc.co.uk/iplayer/episode/b00lkyfb/The_Andrew_Marr_Show_05_07_2009/");
		
		
		Set<Object> graph = Sets.newHashSet();
		graph.add(item);
		
		new HtmlTranslator().writeTo(graph, baos );
		
		System.out.println(baos.toString());
	}
	
	public void testWritesOutPlaylist() throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Item item1 = new Item();
		item1.setTitle("Andrew Marr");
		item1.setDescription("Sunday morning interview show");
		item1.setCanonicalUri("http://www.bbc.co.uk/tomorrowsworld");
		
		Version version = new Version();
		item1.addVersion(version);
		version.setDuration(999);
		
		Version version2 = new Version();
		item1.addVersion(version2);
		version2.setDuration(185);
		
		Encoding encoding = new Encoding();
		version.addManifestedAs(encoding);
		
		Location location = new Location();
		encoding.addAvailableAt(location);
		
		location.setUri("http://www.bbc.co.uk/iplayer/episode/b00lkyfb/The_Andrew_Marr_Show_05_07_2009/");
		
		Item item2 = new Item();
		item2.setTitle("Keith Floyd");
		item2.setDescription("Wino chef");
		item2.setCanonicalUri("http://www.bbc.co.uk/floyd");
		
		Playlist playlist = new Playlist();
		playlist.setCanonicalUri("http://www.bbc.co.uk/somebrand");
		
		playlist.addItems(item1, item2);
		
		Set<Object> graph = Sets.newHashSet();
		graph.add(playlist);
		
		new HtmlTranslator().writeTo(graph, baos );
	}
}
