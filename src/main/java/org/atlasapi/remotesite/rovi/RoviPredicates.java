package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import org.atlasapi.remotesite.rovi.model.ActionLine;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.atlasapi.remotesite.rovi.model.RoviShowType;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;


public class RoviPredicates {

    public final static Predicate<RoviProgramLine> IS_BRAND = new Predicate<RoviProgramLine>() {
        @Override
        public boolean apply(RoviProgramLine input) {
            return RoviShowType.SERIES_MASTER.equals(input.getShowType());
        }
    };
    
    public final static Predicate<RoviProgramLine> HAS_PARENT = new Predicate<RoviProgramLine>() {
        @Override
        public boolean apply(RoviProgramLine input) {
            Optional<String> titleParentId = input.getTitleParentId();
            return titleParentId.isPresent() && !titleParentId.get().equals(input.getProgramId());
        }
    };
    
    public final static Predicate<RoviProgramLine> IS_BRAND_NO_PARENT = and(IS_BRAND, not(HAS_PARENT));
    public final static Predicate<RoviProgramLine> IS_BRAND_WITH_PARENT = and(IS_BRAND, HAS_PARENT);
    public final static Predicate<RoviProgramLine> NO_BRAND_NO_PARENT = and(not(IS_BRAND), not(HAS_PARENT));
    public final static Predicate<RoviProgramLine> NO_BRAND_WITH_PARENT = and(not(IS_BRAND), HAS_PARENT);
    
    public final static Predicate<ActionLine> IS_INSERT = new Predicate<ActionLine>() {
        @Override
        public boolean apply(ActionLine input) {
            return input.getActionType().equals(ActionType.INSERT);
        }
    };    

}
