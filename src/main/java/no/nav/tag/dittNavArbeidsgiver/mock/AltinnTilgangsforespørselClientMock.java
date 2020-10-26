package no.nav.tag.dittNavArbeidsgiver.mock;

import no.nav.tag.dittNavArbeidsgiver.clients.altinn.AltinnTilgangsforespørselClient;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangsforespørsel;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile({"local", "labs"})
public class AltinnTilgangsforespørselClientMock implements AltinnTilgangsforespørselClient {
    private final List<AltinnTilgangsforespørsel> forespørsler = new ArrayList<>();
    private final Clock clock;

    public AltinnTilgangsforespørselClientMock(Clock clock) {
        this.clock = clock;
    }

    public void setStatus(String guid, String newStatus) {
        for (var forespørsel : forespørsler) {
            if (forespørsel.getId().equals(guid)) {
                forespørsel.setStatus(newStatus);
                forespørsel.setLastChangedDateTime(now());
            }
        }
    }

    private String now() {
        return LocalDateTime.now(clock).toString();
    }

    @Override
    public List<AltinnTilgangsforespørsel> hentAlleSøknader() {
        return forespørsler
                .stream()
                .map(fores -> fores.toBuilder().build())
                .collect(Collectors.toList());
    }

    @Override
    public AltinnTilgangsforespørsel sendSøknad(AltinnTilgangssøknadsskjema søknadsskjema) {
        var forespørsel = new AltinnTilgangsforespørsel();

        forespørsel.setId(UUID.randomUUID().toString());
        forespørsel.setCreatedDateTime(now());
        forespørsel.setLastChangedDateTime(now());
        forespørsel.setFnr(søknadsskjema.fnr);
        forespørsel.setOrgnr(søknadsskjema.orgnr);
        forespørsel.setServiceCode(søknadsskjema.serviceCode);
        forespørsel.setServiceEdition(søknadsskjema.serviceEdition);
        forespørsel.setStatus("Created");
        forespørsel.setSubmitUrl(søknadsskjema.redirectUrl);

        forespørsler.add(forespørsel);
        return forespørsel;
    }

    @Override
    public void delete(String guid) {
        forespørsler.removeIf(forespørse -> forespørse.getId().equals(guid));
    }
}
