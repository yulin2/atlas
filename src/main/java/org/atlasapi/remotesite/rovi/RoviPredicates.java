package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import javax.annotation.Nullable;

import org.atlasapi.remotesite.rovi.program.RoviProgramLine;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;


public class RoviPredicates {

    public final static Predicate<RoviProgramLine> IS_BRAND = new Predicate<RoviProgramLine>() {
        @Override
        public boolean apply(@Nullable RoviProgramLine input) {
            return input.getShowType().equals(RoviShowType.SM);
        }
    };
    
    public final static Predicate<RoviProgramLine> HAS_PARENT = new Predicate<RoviProgramLine>() {
        @Override
        public boolean apply(@Nullable RoviProgramLine input) {
            Optional<String> titleParentId = input.getTitleParentId();
            return titleParentId.isPresent() && !titleParentId.get().equals(input.getProgramId());
        }
    };
    
    public final static Predicate<RoviProgramLine> IS_BRAND_NO_PARENT = and(IS_BRAND, not(HAS_PARENT));
    
}
