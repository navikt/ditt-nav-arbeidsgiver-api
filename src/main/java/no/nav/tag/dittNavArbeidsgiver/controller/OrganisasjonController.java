package no.nav.tag.dittNavArbeidsgiver.controller;

import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.tag.dittNavArbeidsgiver.models.Organisasjon;
import no.nav.tag.dittNavArbeidsgiver.clients.altinn.AltinnClient;
import no.nav.tag.dittNavArbeidsgiver.utils.FnrExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Protected
@Slf4j
@RestController
public class OrganisasjonController {

    private final AltinnClient altinnClient;
    private final TokenValidationContextHolder requestContextHolder;

    @Autowired
    public OrganisasjonController(AltinnClient altinnClient, TokenValidationContextHolder requestContextHolder) {
        this.altinnClient = altinnClient;
        this.requestContextHolder = requestContextHolder;
    }

    @GetMapping(value="/api/organisasjoner")
    public ResponseEntity<List<Organisasjon>> hentOrganisasjoner() {
        String fnr = FnrExtractor.extract(requestContextHolder);
        List <Organisasjon> result = altinnClient.hentOrganisasjoner(fnr);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value ="/api/rettigheter-til-skjema")
    public ResponseEntity<List<Organisasjon>> hentRettigheter(@RequestParam String serviceKode, @RequestParam String serviceEdition){
        String fnr = FnrExtractor.extract(requestContextHolder);
        List<Organisasjon> result = altinnClient.hentOrganisasjonerBasertPaRettigheter(fnr, serviceKode,serviceEdition);
        return ResponseEntity.ok(result);
    }
}