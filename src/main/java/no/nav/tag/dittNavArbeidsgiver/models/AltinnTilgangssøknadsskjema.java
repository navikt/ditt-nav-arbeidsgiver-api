package no.nav.tag.dittNavArbeidsgiver.models;

import lombok.Builder;

@Builder
public class AltinnTilgangssøknadsskjema {
    public String fnr;
    public String orgnr;
    public String redirectUrl;
    public String serviceCode;
    public Integer serviceEdition;
}
