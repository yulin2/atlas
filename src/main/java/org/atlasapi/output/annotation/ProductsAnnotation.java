package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.product.Product;
import org.atlasapi.persistence.media.product.ProductResolver;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ProductsAnnotation extends OutputAnnotation<Content> {

    private final ProductResolver productResolver;

    public ProductsAnnotation(ProductResolver productResolver) {
        super(Content.class);
        this.productResolver = productResolver;
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList(new EntityListWriter<Product>() {

            @Override
            public void write(Product entity, FieldWriter writer, OutputContext ctxt) throws IOException {
                
            }

            @Override
            public String listName() {
                return "products";
            }

            @Override
            public String fieldName() {
                return "product";
            }
            
        }, resolveProductsFor(entity, null), ctxt);
    }
    
    private Iterable<Product> resolveProductsFor(Content content, final ApplicationConfiguration config) {
        return filter(productResolver.productsForContent(content.getCanonicalUri()), config);
    }

    private Iterable<Product> filter(Iterable<Product> productsForContent, final ApplicationConfiguration config) {
        return Iterables.filter(productsForContent, new Predicate<Product>() {

            @Override
            public boolean apply(Product input) {
                return config.isEnabled(input.getPublisher());
            }
        });
    }

}
