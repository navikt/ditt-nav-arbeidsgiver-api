package no.nav.tag.dittNavArbeidsgiver.controller;

import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.tag.dittNavArbeidsgiver.models.NarmesteLedertilgang;
import no.nav.tag.dittNavArbeidsgiver.services.aktor.AktorClient;
import no.nav.tag.dittNavArbeidsgiver.services.digisyfo.DigisyfoService;
import no.nav.tag.dittNavArbeidsgiver.utils.FnrExtractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Protected
@RestController
public class DigisyfoController {

    private final TokenValidationContextHolder requestContextHolder;
    private final DigisyfoService digisyfoService;
    private final AktorClient aktorClient;

    @Autowired
    public DigisyfoController(TokenValidationContextHolder requestContextHolder, DigisyfoService digisyfoService, AktorClient aktorClient) {
        this.requestContextHolder = requestContextHolder;
        this.digisyfoService = digisyfoService;
        this.aktorClient = aktorClient;
    }

    @GetMapping(value = "/api/narmesteleder")
    public ResponseEntity<NarmesteLedertilgang> sjekkNarmestelederTilgang() {
        String fnr = FnrExtractor.extract(requestContextHolder);
        String aktørId = aktorClient.getAktorId(fnr);

        NarmesteLedertilgang response = new NarmesteLedertilgang();
        response.tilgang = digisyfoService.getNarmesteledere(aktørId).getNarmesteLedere().length > 0;
        return ResponseEntity.ok(response);
    }
}

