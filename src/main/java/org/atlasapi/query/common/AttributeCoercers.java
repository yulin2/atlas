package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.common.Id;

import com.metabroadcast.common.ids.NumberToShortStringCodec;


public final class AttributeCoercers {

    private AttributeCoercers() { }
    
    public static final AttributeCoercer<String, Id> idCoercer(NumberToShortStringCodec idCodec) {
        return new IdStringCoercer(idCodec);
    }
    
    private static class IdStringCoercer extends AbstractAttributeCoercer<String, Id> {

        private final NumberToShortStringCodec idCodec;

        public IdStringCoercer(NumberToShortStringCodec idCodec) {
            this.idCodec = checkNotNull(idCodec);
        }
        
        @Override
        protected Id coerce(String input) {
            return Id.valueOf(idCodec.decode(input));
        }

    }
}
