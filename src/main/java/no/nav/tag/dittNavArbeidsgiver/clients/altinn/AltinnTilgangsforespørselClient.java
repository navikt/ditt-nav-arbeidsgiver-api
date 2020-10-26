package no.nav.tag.dittNavArbeidsgiver.clients.altinn;

import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangsforespørsel;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;

import java.util.List;

public interface AltinnTilgangsforespørselClient {
    List<AltinnTilgangsforespørsel> hentAlleSøknader();

    AltinnTilgangsforespørsel sendSøknad(AltinnTilgangssøknadsskjema søknadsskjema);

    void delete(String guid);
}
