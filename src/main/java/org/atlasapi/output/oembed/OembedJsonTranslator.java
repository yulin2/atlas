package org.atlasapi.output.oembed;


/**
 * {@link OembedTranslator} that creates an ouput in Json format.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedJsonTranslator extends OembedTranslator {

	public OembedJsonTranslator() {
		super(new JsonOutputFactory());
	}

	private static class JsonOutputFactory implements OutputFactory {

		public OembedOutput createOutput() {
			return new JsonOembedItem();
		}
		
	}

}
