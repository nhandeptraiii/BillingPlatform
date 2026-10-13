package vn.viettel.khdn.billing_platform.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ServiceClassifierTest {

    @Test
    void matchesRealN1ServiceTypes() {
        for (String k : new String[] { "N1 TTTMDT - GT", "N1 CH - GT", "N1 TT", "N1 TTTC - GT",
                "N1 CH - GT AM", "N1 UNC - AM", "n1 tt", "  N1  ", "N1" }) {
            assertTrue(ServiceClassifier.isFtthN1("cto_gftth_nhunth12", k), k);
        }
    }

    @Test
    void rejectsOtherGroupsAndSubstrings() {
        for (String k : new String[] { "N2 TMDT", "N3 CH", "N2 TT ", "N10 TT", "KN1 TT", "N12", "XN1", "" }) {
            assertFalse(ServiceClassifier.isFtthN1("cto_gftth_nhunth12", k), k);
        }
    }

    @Test
    void requiresFtthInSubscriber() {
        assertFalse(ServiceClassifier.isFtthN1("cto_mobile_abc", "N1 TT"));
        assertTrue(ServiceClassifier.isFtthN1("CTO_GFTTH_ABC", "N1 TT"));
        assertFalse(ServiceClassifier.isFtthN1(null, "N1 TT"));
        assertFalse(ServiceClassifier.isFtthN1("ftth", null));
    }
}
