package com.intesi.docsafey.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.intesi.docsafey.entity.richiestaCons.RichiestaConservazione;
import com.intesi.docsafey.entity.richiestaCons.RichiestaStatus;

public class RichiestaConsSpecifications {
    
    public static Specification<RichiestaConservazione> hasProducerId(Long producerId) {
        return(root, query, cb) -> {
            if (producerId == null)
                return null;

            return cb.equal(root.get("producerId"), producerId);
        };
    }

    public static Specification<RichiestaConservazione> hasStatus(RichiestaStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }

            return cb.equal(root.get("status"), status);
        };
    }
}
