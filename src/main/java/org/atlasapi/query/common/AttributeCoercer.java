package org.atlasapi.query.common;

import java.util.List;

import com.google.common.base.Function;

public interface AttributeCoercer<I, O> extends Function<Iterable<I>, List<O>> {

}
