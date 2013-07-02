package org.atlasapi.remotesite.btfeatured;

import javax.annotation.Nonnull;

import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.ContentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class BTFeaturedContentUpdater extends ScheduledTask {

    private static final String CONTENT_GROUP_URI = "http://featured.bt.com";

    private static final String XML_SUFFIX = ".xml";

    private final BTFeaturedClient client;
    private final SimpleElementHandler handler;
    private final ContentGroupResolver groupResolver;
    private final ContentGroupWriter groupWriter;

    private ContentResolver contentResolver;

    private ContentWriter contentWriter;
    
    private static final Logger log = LoggerFactory.getLogger(BTFeaturedContentUpdater.class);

    private final String productBaseUri;

    private final String rootDocumentUri;

    public BTFeaturedContentUpdater(BTFeaturedClient client, 
            SimpleElementHandler handler, 
            ContentGroupResolver groupResolver, 
            ContentGroupWriter groupWriter, 
            ContentResolver contentResolver, 
            ContentWriter contentWriter, 
            String productBaseUri, 
            String rootDocumentUri) {
        
        this.client = client;
        this.handler = handler;
        this.groupResolver = groupResolver;
        this.groupWriter = groupWriter;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.productBaseUri = productBaseUri;
        this.rootDocumentUri = rootDocumentUri;
        
    }
    
    @Override
    protected void runTask() {
        try { 
            ContentGroup contentGroup = null;
            if (groupResolver != null && groupWriter != null) {
                
                ResolvedContent resolvedContent = groupResolver.findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI));
                if (resolvedContent.get(CONTENT_GROUP_URI).hasValue()) {
                    contentGroup = (ContentGroup) resolvedContent.get(CONTENT_GROUP_URI).requireValue();
                    contentGroup.setContents(ImmutableList.<ChildRef>of());
                } else {
                    contentGroup = new ContentGroup(CONTENT_GROUP_URI, Publisher.BT_FEATURED_CONTENT);
                }
                
                handler.withContentGroup(contentGroup);
            }
            
            Document rootDocument = client.get(rootDocumentUri);
            // Returns a Page element, containing nested Products
            
            // Add each product to the BT Featured Content content group
            // If a product is a collection (contains a collection element), treat it as a container and ingest each of the products it contains
            BTFeaturedContentProcessor processor = processor();
            
            Element rootElement = rootDocument.getRootElement();
            evaluateProducts(processor, rootElement, Optional.<Container>absent());
                        
            reportStatus(processor.getResult().toString());

            if (contentGroup != null) {
                groupWriter.createOrUpdate(contentGroup);
            }
        } catch (Exception e) {
            reportStatus(e.getMessage());
            Throwables.propagate(e);
        }
    }

    protected void evaluateProducts(BTFeaturedContentProcessor processor, Element rootElement, Optional<Container> parent)
            throws Exception {
        for (int i = 0; i < rootElement.getChildElements().size(); i++) {
            Element product = rootElement.getChildElements().get(i);

            if (!this.shouldContinue()) return;

            Element hydratedProduct = client.get(productBaseUri+product.getAttributeValue("id")+XML_SUFFIX).getRootElement();
            Optional<Content> item = processor.process(hydratedProduct, parent);
            
            if (item.isPresent() && !(item.get() instanceof Item)) {
                log.info("Product has child "+item.get());
                Document childDoc = client.get(urlForProductWithCurie(item.get().getCurie()));
                
                if (childDoc.getRootElement() instanceof BTFeaturedProductElement) {
                    Element container = ((BTFeaturedProductElement)childDoc.getRootElement()).getContainer();
                    evaluateProducts(processor, container, Optional.of((Container)item.get()));
                }
            }
            
        }
    }

    protected String urlForProductWithCurie(String curie) {
        if (!curie.startsWith(BTFeaturedElementHandler.CURIE_PREFIX)) {
            throw new RuntimeException("Not a recognised curie for BT Featured Content: "+curie);
        }
        return productBaseUri+curie.substring(BTFeaturedElementHandler.CURIE_PREFIX.length())+XML_SUFFIX;
    }

    private BTFeaturedContentProcessor processor() {
        return new BTFeaturedContentProcessor() {
            UpdateProgress progress = UpdateProgress.START;

            @Override
            public Optional<Content> process(Element element, @Nonnull Optional<Container> parent) {
                Optional<Content> content = Optional.absent();
                
                try {
                    content = handler.handle(element, parent);
                    
                    if (content.isPresent()) {
                        ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(content.get().getCanonicalUri()));
                        
                        if (!resolved.resolved(content.get().getCanonicalUri())) {
                            if (content.get() instanceof Item) {
                                contentWriter.createOrUpdate((Item)content.get());
                            }
                            else {
                                contentWriter.createOrUpdate((Container)content.get());
                            }
                        }
                        else {
                            Content resolvedContent = (Content)resolved.get(content.get().getCanonicalUri()).requireValue();
                            if (content.get() instanceof Item) {
                                ContentMerger.merge((Item)resolvedContent, (Item)content.get());
                                contentWriter.createOrUpdate((Item)resolvedContent);
                            }
                            else {
                                ContentMerger.merge((Container)resolvedContent, (Container)content.get());
                                contentWriter.createOrUpdate((Container)resolvedContent);
                            }
                        }
                    }
                    
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn(element.getLocalName() , e);
                    progress = progress.reduce(UpdateProgress.FAILURE);
                }
                reportStatus(progress.toString());
                return content;
            }

            @Override
            public UpdateProgress getResult() {
                return progress;
            }
        };
    }
}
