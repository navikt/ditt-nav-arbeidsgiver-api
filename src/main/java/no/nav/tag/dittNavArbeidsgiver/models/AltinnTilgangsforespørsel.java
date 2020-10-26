package no.nav.tag.dittNavArbeidsgiver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AltinnTilgangsforespørsel {
    String id;
    String orgnr;
    String fnr;
    String serviceCode;
    Integer serviceEdition;
    String status;
    String createdDateTime;
    String lastChangedDateTime;
    String submitUrl;


    public boolean erForeldet(Clock clock) {
        try {
            return status != null &&
                    (status.equals("Accepted") || status.equals("Rejected")) &&
                    lastChangedDateTime != null &&
                    LocalDateTime.parse(lastChangedDateTime).isBefore(
                            LocalDateTime.now(clock).minus(3, ChronoUnit.HOURS)
                    );
        } catch (DateTimeParseException e) {
            log.error("Parsing av lastChangedDateTime='{}' feilet.", lastChangedDateTime, e);
            return false;
        }
    }

    public boolean isValid() {
        return nonNull(
                id,
                orgnr,
                fnr,
                serviceCode,
                serviceEdition,
                status,
                createdDateTime,
                lastChangedDateTime
        );
    }

    private boolean nonNull(Object ... objects)  {
        for (var o : objects) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }
}
