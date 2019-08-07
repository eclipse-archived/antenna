package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360ReleaseTest {

    @Test
    public void testReleaseMergeWithNonDominantNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();
        sw360Release1.setCpeid(null);
        sw360Release2.setCpeid("cpe:ishere");

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeid()).isEqualTo(sw360Release2.getCpeid());
    }

    @Test
    public void testReleaseMergeWithBothNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();

        sw360Release1.setCpeid(null);
        sw360Release2.setCpeid(null);

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeid()).isEqualTo(sw360Release2.getCpeid());
    }

    @Test
    public void testReleaseMergeWithDominantNull() {
        SW360Release sw360Release1 = new SW360Release();
        SW360Release sw360Release2 = new SW360Release();

        sw360Release1.setCpeid("cpe:ishere");
        sw360Release2.setCpeid(null);

        sw360Release1.mergeWith(sw360Release2);

        assertThat(sw360Release1.getCpeid()).isNotEqualTo(sw360Release2.getCpeid());
    }
}
