package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.media.common.Id;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.text.MoreStrings;


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

    public static final AttributeCoercer<String, String> stringCoercer() {
        return StringStringCoercer.INSTANCE;
    }
    
    private enum StringStringCoercer implements AttributeCoercer<String, String> {
        INSTANCE;
        
        @Override
        public List<String> apply(Iterable<String> input) {
            return ImmutableList.copyOf(input);
        }
    }
    
    public static final AttributeCoercer<String, Boolean> booleanCoercer() {
        return StringBooleanCoercer.INSTANCE;
    }
    
    private enum StringBooleanCoercer implements AttributeCoercer<String, Boolean> {
        INSTANCE;
        
        private final ImmutableSet<String> validInput = 
                ImmutableSet.copyOf(Lists.transform(
                    ImmutableList.of(Boolean.TRUE, Boolean.FALSE),
                    Functions.compose(MoreStrings.toLower(), Functions.toStringFunction()))
                );
        
        @Override
        public List<Boolean> apply(@Nullable Iterable<String> input)
                throws InvalidAttributeValueException {
            HashSet<Boolean> values = Sets.newHashSet();
            for (String value : input) {
                String lowerCaseValue = value.toLowerCase();
                if (!validInput.contains(lowerCaseValue)) {
                    throw new InvalidAttributeValueException(value);
                }
                values.add(Boolean.valueOf(lowerCaseValue));
            }
            return ImmutableSet.copyOf(values).asList();
        }
    }
    
    public static final AttributeCoercer<String, Float> floatCoercer() {
        return FloatCoercer.INSTANCE;
    }
    
    private enum FloatCoercer implements AttributeCoercer<String, Float> {
        INSTANCE;

        @Override
        public List<Float> apply(Iterable<String> values) throws InvalidAttributeValueException {
            ImmutableList.Builder<Float> floats = ImmutableList.builder();
            for (String value : values) {
                try {
                    floats.add(Float.valueOf(value));
                } catch (NumberFormatException nfe) {
                    throw new InvalidAttributeValueException(value);
                }
            }
            return floats.build();
        }
    }
    
}
