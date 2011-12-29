package org.atlasapi.output;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;

public abstract class TransformingModelWriter<I, O> implements AtlasModelWriter<I> {

    public static final <I,O> AtlasModelWriter<I> transform(AtlasModelWriter<O> delegate, final Function<? super I, O> transformer) {
        return new TransformingModelWriter<I, O>(delegate) {
            @Override
            protected O transform(I model) {
                return transformer.apply(model);
            }
        };
    }
    
    private final AtlasModelWriter<O> delegate;

    public TransformingModelWriter(AtlasModelWriter<O> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void writeTo(HttpServletRequest request, HttpServletResponse response, I model) throws IOException {
        delegate.writeTo(request, response, transform(model));
    }

    protected abstract O transform(I model);

    @Override
    public void writeError(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary exception) throws IOException {
        delegate.writeError(request, response, exception);
    }

}
