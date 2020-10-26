package no.nav.tag.dittNavArbeidsgiver.services.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.dittNavArbeidsgiver.clients.altinn.AltinnTilgangsforespørselClient;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangsforespørsel;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AltinnTilgangsforespørselService {
    private final AltinnTilgangsforespørselClient altinnTilgangsforespørselClient;
    private final Clock clock;

    @Autowired
    public AltinnTilgangsforespørselService(AltinnTilgangsforespørselClient altinnTilgangsforespørselClient) {
        this(altinnTilgangsforespørselClient, Clock.systemDefaultZone());
    }
    public AltinnTilgangsforespørselService(AltinnTilgangsforespørselClient altinnTilgangsforespørselClient, Clock clock) {
        this.altinnTilgangsforespørselClient = altinnTilgangsforespørselClient;
        this.clock = clock;
    }

    public Collection<AltinnTilgangsforespørsel> hentForespørsler(String fnr) {
        return altinnTilgangsforespørselClient
                .hentAlleSøknader()
                .stream()
                .filter(forespørsel -> fnr.equals(forespørsel.getFnr()))
                .collect(Collectors.toList());
    }

    public AltinnTilgangsforespørsel sendForespørsel(AltinnTilgangssøknadsskjema skjema) {
        return hentForespørsler(skjema.fnr)
                .stream()
                .filter(forespørsel ->
                        forespørsel.getOrgnr().equals(skjema.orgnr) &&
                        forespørsel.getServiceCode().equals(skjema.serviceCode) &&
                                forespørsel.getServiceEdition().equals(skjema.serviceEdition)
                )
                .findAny()
                .orElseGet(() -> altinnTilgangsforespørselClient.sendSøknad(skjema));
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000) /* i millisekund */
    public void slettForeldedeForespørsler() {
        var antallSlettet = new AtomicInteger(0);
        altinnTilgangsforespørselClient
                .hentAlleSøknader()
                .stream()
                .filter(søknad -> søknad.erForeldet(clock))
                .forEach(foreldetSøknad -> {
                    try {
                        altinnTilgangsforespørselClient.delete(foreldetSøknad.getId());
                        antallSlettet.getAndIncrement();
                    } catch (Exception e) {
                        log.error("Sletting av tilgangsforespørsel {} feilet.", foreldetSøknad.getId(), e);
                    }
                });
        log.info("Slettet {} foreldede forespørsler", antallSlettet.get());
    }
}
