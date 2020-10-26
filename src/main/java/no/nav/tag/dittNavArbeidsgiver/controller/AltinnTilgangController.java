package no.nav.tag.dittNavArbeidsgiver.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.Pair;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangsforespørsel;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;
import no.nav.tag.dittNavArbeidsgiver.clients.altinn.AltinnClient;
import no.nav.tag.dittNavArbeidsgiver.services.altinn.AltinnTilgangsforespørselService;
import no.nav.tag.dittNavArbeidsgiver.utils.FnrExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Protected
@Slf4j
@RestController
@RequestMapping("/api/altinn-tilgangssoknad")
public class AltinnTilgangController {
    private static final Set<Pair<String, String>> våreTjenester = Set.of(
            Pair.of("5216",  "1"),
            Pair.of("5212",  "1"),
            Pair.of("5384",  "1"),
            Pair.of("5159", "1"),
            Pair.of("4936",  "1"),
            Pair.of("5332", "2"),
            Pair.of("5332", "1"),
            Pair.of("5441",  "1"),
            Pair.of("5516", "1"),
            Pair.of("5516", "2"),
            Pair.of("3403", "2"),
            Pair.of("5078",  "1"),
            Pair.of("5278", "1")
    );

    private final AltinnTilgangsforespørselService altinnTilgangsforespørselService;
    private final AltinnClient altinnClient;
    private final TokenValidationContextHolder requestContextHolder;

    @Autowired
    public AltinnTilgangController(
            AltinnTilgangsforespørselService altinnTilgangsforespørselService,
            AltinnClient altinnClient,
            TokenValidationContextHolder requestContextHolder
    ) {
        this.altinnTilgangsforespørselService = altinnTilgangsforespørselService;
        this.altinnClient = altinnClient;
        this.requestContextHolder = requestContextHolder;
    }

    @GetMapping()
    public ResponseEntity<List<AltinnTilgangsforespørsel>> mineSøknaderOmTilgang() {
        String fødselsnummer = FnrExtractor.extract(requestContextHolder);
        return ResponseEntity.ok(List.copyOf(altinnTilgangsforespørselService.hentForespørsler(fødselsnummer)));
    }

    @PostMapping()
    public ResponseEntity<AltinnTilgangsforespørsel> sendSøknadOmTilgang(@RequestBody AltinnTilgangssøknadsskjema søknadsskjema) {
        validateNotBlank("orgnr", søknadsskjema.orgnr);
        validateNotBlank("service code", søknadsskjema.serviceCode);
        validateNotBlank("redirect url", søknadsskjema.redirectUrl);
        Objects.requireNonNull(søknadsskjema.serviceEdition, "serviceEdition");


        var fødselsnummer= FnrExtractor.extract(requestContextHolder);

        var brukerErIOrg = altinnClient.hentOrganisasjoner(fødselsnummer)
                .stream()
                .anyMatch(org -> org.getOrganizationNumber().equals(søknadsskjema.orgnr));

        if (!brukerErIOrg) {
            log.warn("Bruker forsøker å be om tilgang til org de ikke er med i.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Pair<String, String> tjeneste = Pair.of(søknadsskjema.serviceCode, søknadsskjema.serviceEdition.toString());

        if (!våreTjenester.contains(tjeneste)) {
            log.warn("Bruker forsøker å be om tilgang til tjeneste ({}, {})) vi ikke støtter.", søknadsskjema.serviceCode, søknadsskjema.serviceEdition);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        søknadsskjema.fnr = fødselsnummer;
        return ResponseEntity.ok(altinnTilgangsforespørselService.sendForespørsel(søknadsskjema));
    }

    private static void validateNotBlank(String whatItIs, String shouldNotBeBlank) {
        Objects.requireNonNull(shouldNotBeBlank, whatItIs);
        if (shouldNotBeBlank.matches("\\s*")) {
            throw new IllegalArgumentException("Should not be blank: " + whatItIs);
        }
    }
}
