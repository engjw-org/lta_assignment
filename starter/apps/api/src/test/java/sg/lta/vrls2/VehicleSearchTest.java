package sg.lta.vrls2;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.lta.vrls2.controller.VehicleController;
import sg.lta.vrls2.model.Vehicle;
import sg.lta.vrls2.service.VehicleCsvLoader;
import sg.lta.vrls2.service.VehicleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Issue #5: real controller/service; only the CSV boundary supplies fixture events. */
class VehicleSearchTest {
    private VehicleCsvLoader loader;
    private VehicleService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() throws Exception {
        loader = mock(VehicleCsvLoader.class);
        service = new VehicleService(loader);
        ReflectionTestUtils.setField(service, "dataDir", ".");
        givenEvents(List.of(event(1, "Toyota", "5-seat", "2026-04-08", "2032-11-01")));
        // Match Spring Boot's ISO JSON date/time serialization, not Jackson's raw array default.
        var json = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        mvc = MockMvcBuilders.standaloneSetup(new VehicleController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json)).build();
    }

    private void givenEvents(List<Vehicle> events) throws Exception {
        when(loader.loadAll(any())).thenReturn(events);
        service.init();
    }

    private static String uuid(int number) {
        return "00000000-0000-4000-8000-%012d".formatted(number);
    }

    private static Vehicle event(int id, String brand, String type, String start, String end) {
        return new Vehicle(uuid(id), "REGISTERED", LocalDate.parse(start), LocalDate.parse(end),
                LocalTime.of(9, 5, 7), "11111111-1111-4111-8111-111111111111", brand, type, "personal");
    }

    private ResultActions search(String payload) throws Exception {
        return mvc.perform(post("/api/vehicles/search").contentType(MediaType.APPLICATION_JSON).content(payload));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 1999, 2000, 2001, 2005})
    void blankSearchSortsBeforeCappingAndReportsTruncationAtExactlyTwoThousand(int count) throws Exception {
        // Reverse input makes limiting before sorting return the wrong records.
        List<Vehicle> source = IntStream.range(0, count)
                .mapToObj(i -> event(count - i, "Toyota", "5-seat", "2026-04-08", "2032-11-01")).toList();
        givenEvents(source);
        ResultActions result = search("{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(Math.min(count, 2000))))
                .andExpect(jsonPath("$.totalMatches").value(count))
                .andExpect(jsonPath("$.truncated").value(count > 2000));
        if (count > 0) {
            result.andExpect(jsonPath("$.records[0].uuid").value(uuid(1)))
                    .andExpect(jsonPath("$.records[" + (Math.min(count, 2000) - 1) + "].uuid")
                            .value(uuid(Math.min(count, 2000))));
        }
        assertThat(service.all()).containsExactlyElementsOf(source);
    }

    @Test
    void omittedEmptyAndWhitespaceFieldsAreAllOptional() throws Exception {
        search("""
                {"query":"  ","startDate":"","endDate":null,"type":""}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.totalMatches").value(1)).andExpect(jsonPath("$.truncated").value(false));
    }

    @Test
    void matchesUuidPrefixOrBrandSubstringCaseInsensitively() throws Exception {
        Vehicle prefix = new Vehicle("abcdef00-0000-4000-8000-000000000001", "REGISTERED",
                LocalDate.of(2026, 4, 8), LocalDate.of(2032, 11, 1), LocalTime.NOON,
                uuid(9), "Ford", "7-seat", "personal");
        givenEvents(List.of(prefix, event(2, "The ABCDEF Motors", "5-seat", "2026-04-08", "2032-11-01"),
                event(3, "Toyota", "5-seat", "2026-04-08", "2032-11-01")));
        search("{\"query\":\"  ABCDEF  \"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(2)))
                .andExpect(jsonPath("$.records[0].brand").value("The ABCDEF Motors"))
                .andExpect(jsonPath("$.records[1].uuid").value(prefix.uuid()));
        search("{\"query\":\"4000-8000\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(0))); // UUID interior is not a prefix.
    }

    @Test
    void filtersAcrossAllEventsBeforeApplyingTheCap() throws Exception {
        List<Vehicle> source = IntStream.rangeClosed(1, 2001).mapToObj(i -> event(i,
                i == 2001 ? "Rare Brand" : "Toyota", "5-seat", "2026-04-08", "2032-11-01")).toList();
        givenEvents(source);
        search("{\"query\":\"rare\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.records[0].uuid").value(uuid(2001)))
                .andExpect(jsonPath("$.totalMatches").value(1)).andExpect(jsonPath("$.truncated").value(false));
    }

    @Test
    void matchesSeatTypeExactly() throws Exception {
        givenEvents(List.of(event(1, "Toyota", "5-seat", "2026-04-08", "2032-11-01"),
                event(2, "Toyota", "15-seat", "2026-04-08", "2032-11-01"),
                event(3, "Toyota", "7-seat", "2026-04-08", "2032-11-01")));
        search("{\"type\":\"5-seat\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(1))).andExpect(jsonPath("$.records[0].uuid").value(uuid(1)));
    }

    @Test
    void startDateIsAnInclusiveLowerBoundOnItsOwnField() throws Exception {
        givenEvents(List.of(event(1, "Before", "5-seat", "2026-04-07", "2040-01-01"),
                event(2, "At bound", "5-seat", "2026-04-08", "2039-01-01"),
                event(3, "After", "5-seat", "2026-04-09", "2038-01-01")));
        search("{\"startDate\":\"2026-04-08\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(2)))
                .andExpect(jsonPath("$.records[0].brand").value("At bound"))
                .andExpect(jsonPath("$.records[1].brand").value("After"));
    }

    @Test
    void endDateIsAnInclusiveUpperBoundOnItsOwnField() throws Exception {
        givenEvents(List.of(event(1, "Before", "5-seat", "2020-01-01", "2032-10-31"),
                event(2, "At bound", "5-seat", "2020-01-01", "2032-11-01"),
                event(3, "After", "5-seat", "2020-01-01", "2032-11-02")));
        search("{\"endDate\":\"2032-11-01\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(2)))
                .andExpect(jsonPath("$.records[0].brand").value("Before"))
                .andExpect(jsonPath("$.records[1].brand").value("At bound"));
    }

    @Test
    void combinesAllSuppliedCriteriaWithAnd() throws Exception {
        givenEvents(List.of(event(1, "Toyota", "5-seat", "2026-04-08", "2032-11-01"),
                event(2, "Ford", "5-seat", "2026-04-08", "2032-11-01"),
                event(3, "Toyota", "7-seat", "2026-04-08", "2032-11-01"),
                event(4, "Toyota", "5-seat", "2026-04-07", "2032-11-01"),
                event(5, "Toyota", "5-seat", "2026-04-08", "2032-11-02")));
        search("""
                {"query":"toyota","type":"5-seat","startDate":"2026-04-08","endDate":"2032-11-01"}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.records[0].uuid").value(uuid(1)));
    }

    @Test
    void preservesRepeatedUuidEventsAndAllFieldsWithDateDescendingInsideUuid() throws Exception {
        Vehicle old = event(1, "Old", "5-seat", "2025-12-31", "2039-01-01");
        Vehicle recent = new Vehicle(uuid(1), "DEREGISTERED", LocalDate.of(2026, 2, 1),
                LocalDate.of(2030, 1, 1), LocalTime.of(23, 59, 59), uuid(99), "Recent", "7-seat", "commercial");
        Vehicle middle = event(1, "Middle", "5-seat", "2026-01-31", "2035-01-01");
        List<Vehicle> source = List.of(event(2, "Later UUID", "5-seat", "2027-01-01", "2040-01-01"), old, recent, middle);
        givenEvents(source);
        search("{}").andExpect(status().isOk()).andExpect(jsonPath("$.records", hasSize(4)))
                .andExpect(jsonPath("$.records[0].brand").value("Recent"))
                .andExpect(jsonPath("$.records[1].brand").value("Middle"))
                .andExpect(jsonPath("$.records[2].brand").value("Old"))
                .andExpect(jsonPath("$.records[3].uuid").value(uuid(2)))
                .andExpect(jsonPath("$.records[0].status").value("DEREGISTERED"))
                .andExpect(jsonPath("$.records[0].startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.records[0].endDate").value("2030-01-01"))
                .andExpect(jsonPath("$.records[0].time").value("23:59:59"))
                .andExpect(jsonPath("$.records[0].userUuid").value(uuid(99)))
                .andExpect(jsonPath("$.records[0].type").value("7-seat"))
                .andExpect(jsonPath("$.records[0].category").value("commercial"));
        assertThat(service.all()).containsExactlyElementsOf(source);
    }

    @Test
    void nonMatchingSearchReturnsAnEmptyCompleteResult() throws Exception {
        search("{\"query\":\"not-present\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(0)))
                .andExpect(jsonPath("$.totalMatches").value(0)).andExpect(jsonPath("$.truncated").value(false));
    }

    static Stream<String> malformedCriteria() {
        return Stream.of("{\"startDate\":\"08/04/2026\"}", "{\"endDate\":\"2026-02-30\"}",
                "{\"startDate\":\"2026-04-08T09:00:00\"}", "{\"query\":\"" + "a".repeat(101) + "\"}",
                "{\"type\":\"" + "a".repeat(51) + "\"}", "{\"query\":[\"Toyota\"]}");
    }

    @ParameterizedTest
    @MethodSource("malformedCriteria")
    void rejectsMalformedOrOversizedCriteria(String payload) throws Exception {
        search(payload).andExpect(status().isBadRequest());
    }
}
