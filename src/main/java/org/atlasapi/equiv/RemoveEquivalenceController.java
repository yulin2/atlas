package org.atlasapi.equiv;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RemoveEquivalenceController {

    private final EquivalenceBreaker equivalenceBreaker;
    
    public RemoveEquivalenceController(EquivalenceBreaker equivalenceBreaker) {
        this.equivalenceBreaker = checkNotNull(equivalenceBreaker);
    }
    
    @RequestMapping(value="/system/equivalence/remove", method=RequestMethod.POST)
    public void removeEquivalence(
            @RequestParam(value="from", required=true) String sourceUri, 
            @RequestParam(value="to", required=true) String targetUriToRemove) {
        equivalenceBreaker.removeFromSet(sourceUri, targetUriToRemove);
    }
}
