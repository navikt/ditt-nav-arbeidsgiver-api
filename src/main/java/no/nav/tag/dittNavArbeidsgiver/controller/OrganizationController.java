package no.nav.tag.dittNavArbeidsgiver.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.dittNavArbeidsgiver.models.Organization;
import no.nav.tag.dittNavArbeidsgiver.services.altinn.AltinnGW;
import no.nav.tag.dittNavArbeidsgiver.utils.FnrExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Protected
@Slf4j
@RestController
public class OrganizationController {

    private final AltinnGW altinnGW;
    private final OIDCRequestContextHolder requestContextHolder;

    @Autowired
    public OrganizationController(AltinnGW altinnGW, OIDCRequestContextHolder requestContextHolder) {
        this.altinnGW = altinnGW;
        this.requestContextHolder = requestContextHolder;
    }

    @GetMapping(value="/api/organisasjoner")
    private ResponseEntity<List<Organization>> getOrganizations() {
        String fnr = FnrExtractor.extract(requestContextHolder);
        List <Organization> result = altinnGW.hentOrganisasjoner(fnr);
        return ResponseEntity.ok(result);
    }

}
