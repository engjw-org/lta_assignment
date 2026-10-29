package sg.lta.vrls2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.lta.vrls2.controller.VehicleController;
import sg.lta.vrls2.model.Vehicle;
import sg.lta.vrls2.service.VehicleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleDetailLookupTest {
    private static final String UUID = "00000000-0000-4000-8000-000000000001";
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        VehicleService service = mock(VehicleService.class);
        when(service.all()).thenReturn(IntStream.range(0, 61).mapToObj(i -> new Vehicle(
                i == 60 ? "00000000-0000-4000-8000-000000000002" : UUID,
                i % 2 == 0 ? "REGISTERED" : "DEREGISTERED",
                LocalDate.of(2026, 4, 8), LocalDate.of(2032, 11, 1), LocalTime.of(9, 5, i % 60),
                "11111111-1111-4111-8111-111111111111", "New Brand", "5-seat", "personal"
        )).toList());
        mvc = MockMvcBuilders.standaloneSetup(new VehicleController(service)).build();
    }

    @Test
    void detailLookupReturnsAllMatchingEventsAndFieldsWithoutPageTruncation() throws Exception {
        mvc.perform(get("/api/vehicles").param("page", "0").param("uuid", UUID))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(60)))
                .andExpect(jsonPath("$[0].uuid").value(UUID))
                .andExpect(jsonPath("$[0].brand").value("New Brand"))
                .andExpect(jsonPath("$[0].userUuid").value("11111111-1111-4111-8111-111111111111"))
                .andExpect(jsonPath("$[59].status").value("DEREGISTERED"));
    }

    @Test
    void unknownVehicleReturnsNoEventsInsteadOfOtherVehicles() throws Exception {
        mvc.perform(get("/api/vehicles").param("uuid", "00000000-0000-4000-8000-000000000099"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void invalidUuidIsRejected() throws Exception {
        mvc.perform(get("/api/vehicles").param("uuid", "<script>"))
                .andExpect(status().isBadRequest());
    }
}
