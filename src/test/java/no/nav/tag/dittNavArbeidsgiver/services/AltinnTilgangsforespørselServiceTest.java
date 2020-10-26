package no.nav.tag.dittNavArbeidsgiver.services;

import no.nav.tag.dittNavArbeidsgiver.mock.AltinnTilgangsforespørselClientMock;
import no.nav.tag.dittNavArbeidsgiver.mock.ClockMock;
import no.nav.tag.dittNavArbeidsgiver.models.AltinnTilgangssøknadsskjema;
import no.nav.tag.dittNavArbeidsgiver.services.altinn.AltinnTilgangsforespørselService;
import org.junit.Test;

import java.time.*;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AltinnTilgangsforespørselServiceTest {
    final private ClockMock clock = new ClockMock(Instant.ofEpochSecond(60 * 60 * 24 * 365));

    final private AltinnTilgangsforespørselClientMock client =
            new AltinnTilgangsforespørselClientMock(clock);

    final private AltinnTilgangsforespørselService service = new AltinnTilgangsforespørselService(client, clock);

    private static final String fnr0 = "1234";
    private static final String fnr1 = fnr0 + "1";
    private static final String orgnr0 = "321";
    private static final String orgnr1 = orgnr0 + "1";
    private static final String redirectUrl0 = "https://redirect";
    private static final String tjenestekode0 = "456";
    private static final Integer tjenesteversjon0 = 2;

    private static final AltinnTilgangssøknadsskjema skjema0 = AltinnTilgangssøknadsskjema
            .builder()
            .fnr(fnr0)
            .orgnr(orgnr0)
            .serviceCode(tjenestekode0)
            .serviceEdition(tjenesteversjon0)
            .redirectUrl(redirectUrl0)
            .build();

    private static final AltinnTilgangssøknadsskjema skjema1 = AltinnTilgangssøknadsskjema
            .builder()
            .fnr(fnr1)
            .orgnr(orgnr0)
            .serviceCode(tjenestekode0)
            .serviceEdition(tjenesteversjon0)
            .redirectUrl(redirectUrl0)
            .build();

    private static final AltinnTilgangssøknadsskjema skjema2 = AltinnTilgangssøknadsskjema
            .builder()
            .fnr(fnr0)
            .orgnr(orgnr1)
            .serviceCode(tjenestekode0)
            .serviceEdition(tjenesteversjon0)
            .redirectUrl(redirectUrl0)
            .build();

    @Test
    public void enkeltOppslagFungerer() {
        service.sendForespørsel(skjema0);
        service.slettForeldedeForespørsler();
        assertEquals(1, service.hentForespørsler(fnr0).size());
    }

    @Test
    public void duplisererIkkeSkjema() {
        service.sendForespørsel(skjema0);
        service.sendForespørsel(skjema0);
        service.slettForeldedeForespørsler();
        assertEquals(1, service.hentForespørsler(fnr0).size());
    }

    @Test
    public void skillerFnrFraHverandre() {
        service.sendForespørsel(skjema0);
        service.sendForespørsel(skjema1);
        service.slettForeldedeForespørsler();
        assertEquals(1, service.hentForespørsler(fnr0).size());
        assertEquals(1, service.hentForespørsler(fnr1).size());
    }

    @Test
    public void skillerForespørslerTilForskjelligeOrgnr() {
        service.sendForespørsel(skjema0);
        service.sendForespørsel(skjema2);
        service.slettForeldedeForespørsler();
        assertEquals(2, service.hentForespørsler(fnr0).size());
    }

    @Test
    public void kasterUtForeldeteForespørsler() {
        var forespørsel = service.sendForespørsel(skjema0);
        client.setStatus(forespørsel.getId(), "Accepted");

        clock.forward(Period.ofDays(1));

        service.slettForeldedeForespørsler();
        assertTrue(service.hentForespørsler(fnr0).isEmpty());
    }

    @Test
    public void kasterIkkeUtNyeGodkjenteForespørsler() {
        var forespørsel = service.sendForespørsel(skjema0);
        client.setStatus(forespørsel.getId(), "Accepted");

        clock.forward(Duration.ofMinutes(20));

        service.slettForeldedeForespørsler();
        assertEquals(1, service.hentForespørsler(fnr0).size());
    }

    @Test
    public void kasterIkkeUtGamleOpprettedeForespørsler() {
        service.sendForespørsel(skjema0);

        clock.forward(Period.ofDays(1));

        service.slettForeldedeForespørsler();
        assertEquals(1, service.hentForespørsler(fnr0).size());
    }

    @Test
    public void hentingRettEtterInnsendingFinnerForespørselen() {
        var førsteForespørsel = service.sendForespørsel(skjema0);
        var forespørsler = service.hentForespørsler(fnr0);

        /* Merk at service.oppdaterForespørsler IKKE kalles. */

        assertEquals(1, forespørsler.size());
        var andreForespørsel = new ArrayList<>(forespørsler).get(0);
        assertEquals(førsteForespørsel, andreForespørsel);
    }
}
