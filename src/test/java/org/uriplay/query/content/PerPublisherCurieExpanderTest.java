package org.uriplay.query.content;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import static org.uriplay.query.content.PerPublisherCurieExpander.CurieAlgorithm.*;

import junit.framework.TestCase;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class PerPublisherCurieExpanderTest extends TestCase {

	CurieExpander expander = new PerPublisherCurieExpander();
	
	public void testExpandsBbcCuries() throws Exception {
		
		assertThat(expander.expand("bbc:b006mk25"), is("http://www.bbc.co.uk/programmes/b006mk25"));
	}
	
	public void testProducesBbcCuries() throws Exception {
		
		assertThat(BBC.compact("http://www.bbc.co.uk/programmes/b006mk25"), is("bbc:b006mk25"));
	}
	
	public void testExpandsC4Curies() throws Exception {
		assertThat(expander.expand("c4:grand-designs"), is("http://www.channel4.com/programmes/grand-designs/4od"));
		assertThat(expander.expand("c4:grand-designs_2921795"), is("http://www.channel4.com/programmes/grand-designs/4od#2921795"));
	}
	
	public void testProducesC4Curies() throws Exception {
		assertThat(C4.compact("http://www.channel4.com/programmes/grand-designs/4od"), is("c4:grand-designs"));
		assertThat(C4.compact("http://www.channel4.com/programmes/grand-designs/4od#2921795"), is("c4:grand-designs_2921795"));
	}
	
	public void testExpandsItvCuries() throws Exception {
		assertThat(expander.expand("itv:1-2773"), is("http://www.itv.com/ITVPlayer/Programmes/default.html?ViewType=1&Filter=2773"));
		assertThat(expander.expand("itv:5-100109"), is("http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=100109"));
	}
	
	public void testProducesItvCuries() throws Exception {
		assertThat(ITV.compact("http://www.itv.com/ITVPlayer/Programmes/default.html?ViewType=1&Filter=2773"), is("itv:1-2773"));
		assertThat(ITV.compact("http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=100109"), is("itv:5-100109"));
	}
	
	public void testExpandsYoutubeCuries() throws Exception {
		
		assertThat(expander.expand("yt:4QQkMVddwx0"), is("http://www.youtube.com/watch?v=4QQkMVddwx0"));
	}
	
	public void testProducesYoutubeCuries() throws Exception {
		
		assertThat(YT.compact("http://www.youtube.com/watch?v=4QQkMVddwx0"), is("yt:4QQkMVddwx0"));
	}
	
	public void testExpandsBlipTvCuries() throws Exception {
		
		assertThat(expander.expand("blip:12345"), is("http://blip.tv/file/12345"));
	}
	
	public void testProducesBlipTvCuries() throws Exception {
		
		assertThat(BLIP.compact("http://blip.tv/file/12345"), is("blip:12345"));
	}
	
	public void testExpandsVimeoCuries() throws Exception {
		
		assertThat(expander.expand("vim:12345"), is("http://vimeo.com/12345"));
	}
	
	public void testProducesVimeoCuries() throws Exception {
		
		assertThat(VIM.compact("http://vimeo.com/12345"), is("vim:12345"));
	}
	
	public void testExpandsDailyMotionCuries() throws Exception {
		
		assertThat(expander.expand("dm:xbqomc_dont-do-anything_fun"), is("http://www.dailymotion.com/video/xbqomc_dont-do-anything_fun"));
	}
	
	public void testProducesDailyMotionCuries() throws Exception {
		
		assertThat(DM.compact("http://www.dailymotion.com/video/xbqomc_dont-do-anything_fun"), is("dm:xbqomc_dont-do-anything_fun"));
	}
	
	public void testExpandsHuluCuries() throws Exception {
		
		assertThat(expander.expand("hulu:78417"), is("http://www.hulu.com/watch/78417"));
	}
	
	public void testProducesHuluCuries() throws Exception {
		
		assertThat(HULU.compact("http://www.hulu.com/watch/78417/the-daily-show-with-jon-stewart-wed-jun-17-2009"), is("hulu:78417"));
	}
	
	public void testExpandsTedCuries() throws Exception {
		
		assertThat(expander.expand("ted:elizabeth_gilbert_on_genius"), is("http://www.ted.com/talks/elizabeth_gilbert_on_genius.html"));
	}
	
	public void testProducesTedCuries() throws Exception {
		
		assertThat(TED.compact("http://www.ted.com/talks/elizabeth_gilbert_on_genius.html"), is("ted:elizabeth_gilbert_on_genius"));
	}
	
}
