package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.common.Id;

import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class IdStringCoercer extends AbstractAttributeCoercer<String, Id> {

    private final NumberToShortStringCodec idCodec;

    public IdStringCoercer(NumberToShortStringCodec idCodec) {
        this.idCodec = checkNotNull(idCodec);
    }
    
    @Override
    protected Id coerce(String input) {
        return Id.valueOf(idCodec.decode(input));
    }

}
