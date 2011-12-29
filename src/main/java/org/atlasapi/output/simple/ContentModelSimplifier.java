package org.atlasapi.output.simple;

import java.util.List;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.simple.Description;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public abstract class ContentModelSimplifier<F extends Content, T extends Description> extends DescribedModelSimplifier<F,T> {

    
    protected void copyBasicContentAttributes(F content, T simpleDescription) {
        copyBasicDescribedAttributes(content, simpleDescription);
        simpleDescription.setClips(clipToSimple(content.getClips()));
    }
    
    private List<org.atlasapi.media.entity.simple.Item> clipToSimple(List<Clip> clips) {
        return Lists.transform(clips, new Function<Clip, org.atlasapi.media.entity.simple.Item>() {
            @Override
            public org.atlasapi.media.entity.simple.Item apply(Clip clip) {
                return simplify(clip);
            }
        });
    }

    protected abstract org.atlasapi.media.entity.simple.Item simplify(Item item);

}
