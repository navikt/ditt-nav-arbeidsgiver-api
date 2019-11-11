package no.nav.tag.dittNavArbeidsgiver.controller;


import no.finn.unleash.Unleash;
import no.nav.tag.dittNavArbeidsgiver.models.NarmesteLedertilgang;
import no.nav.tag.dittNavArbeidsgiver.services.digisyfo.DigisyfoService;
import no.nav.security.oidc.api.Protected;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

import static no.nav.tag.dittNavArbeidsgiver.utils.FnrExtractor.extract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Protected
@RestController
public class DigisyfoController {

    private final OIDCRequestContextHolder requestContextHolder;
    private final DigisyfoService digisyfoService;
    private final Unleash unleash;
    @Value("${digisyfo.digisyfoUrl}")
    private String digisyfoUrl;

    @Autowired
    public DigisyfoController(OIDCRequestContextHolder requestContextHolder, DigisyfoService digisyfoService, Unleash unleash) {
        this.requestContextHolder = requestContextHolder;
        this.digisyfoService = digisyfoService;
        this.unleash = unleash;

    }

    @GetMapping(value = "/api/narmesteleder")
    public ResponseEntity<NarmesteLedertilgang> sjekkNarmestelederTilgang() {
        NarmesteLedertilgang response = new NarmesteLedertilgang();
        response.tilgang = digisyfoService.getNarmesteledere(extract(requestContextHolder)).getNarmesteLedere().length > 0;
        return ResponseEntity.ok(response);

    }

    @GetMapping(value = "/api/sykemeldinger")
    public String hentAntallSykemeldinger(@CookieValue("nav-esso") String navesso) {
        return unleash.isEnabled("dna.digisyfo.hentSykemeldinger") ? digisyfoService.hentSykemeldingerFraSyfo(navesso) : "[]";
    }
    
    @GetMapping(value = "/api/syfooppgaver")
    public String hentSyfoOppgaver(@CookieValue("nav-esso") String navesso) {
        return digisyfoService.hentSyfoOppgaver(navesso);
    }

}

