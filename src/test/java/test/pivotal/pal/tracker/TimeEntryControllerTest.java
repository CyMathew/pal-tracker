package test.pivotal.pal.tracker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.pivotal.pal.tracker.TimeEntry;
import io.pivotal.pal.tracker.TimeEntryController;
import io.pivotal.pal.tracker.TimeEntryRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.rmi.NoSuchObjectException;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TimeEntryControllerTest {
    private TimeEntryRepository timeEntryRepository;
    private DistributionSummary timeEntrySummary;
    private Counter actionCounter;
    private TimeEntryController controller;

//    @Before
//    public void setUp() {
//        timeEntryRepository = mock(TimeEntryRepository.class);
//        timeEntrySummary = mock(DistributionSummary.class);
//        actionCounter = mock(Counter.class);
//        controller = new TimeEntryController(timeEntryRepository, meterRegistry);
//    }

    @Before
    public void setUp() throws Exception {
        timeEntryRepository = mock(TimeEntryRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        timeEntrySummary = mock(DistributionSummary.class);
        doReturn(timeEntrySummary)
                .when(meterRegistry)
                .summary("timeEntry.summary");

        actionCounter = mock(Counter.class);
        doReturn(actionCounter)
                .when(meterRegistry)
                .counter("timeEntry.actionCounter");

        controller = new TimeEntryController(timeEntryRepository, meterRegistry);
    }
    // done
    @Test
    public void testCreate() {
        long projectId = 123L;
        long userId = 456L;
        TimeEntry timeEntryToCreate = new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8);
//        doReturn(new List<>(timeEntryRepository) {}.when()



        long timeEntryId = 1L;
        TimeEntry expectedResult = new TimeEntry(timeEntryId, projectId, userId, LocalDate.parse("2017-01-08"), 8);
        doReturn(expectedResult)
            .when(timeEntryRepository)
            .create(any(TimeEntry.class));


        ResponseEntity response = controller.create(timeEntryToCreate);


        verify(timeEntrySummary, times(1)).record(anyDouble());
        verify(actionCounter, times(1)).increment();
        verify(timeEntryRepository).create(timeEntryToCreate);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResult);
    }

    @Test
    public void testRead() {
        long timeEntryId = 1L;
        long projectId = 123L;
        long userId = 456L;
        TimeEntry expected = new TimeEntry(timeEntryId, projectId, userId, LocalDate.parse("2017-01-08"), 8);
        doReturn(expected)
            .when(timeEntryRepository)
            .find(timeEntryId);

        ResponseEntity<TimeEntry> response = controller.read(timeEntryId);

        verify(actionCounter, times(1)).increment();
        verify(timeEntryRepository).find(timeEntryId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testRead_NotFound() {
        long nonExistentTimeEntryId = 1L;
        doReturn(null)
            .when(timeEntryRepository)
            .find(nonExistentTimeEntryId);

        verify(actionCounter, times(0)).increment();
        ResponseEntity<TimeEntry> response = controller.read(nonExistentTimeEntryId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testList() {
        List<TimeEntry> expected = asList(
            new TimeEntry(1L, 123L, 456L, LocalDate.parse("2017-01-08"), 8),
            new TimeEntry(2L, 789L, 321L, LocalDate.parse("2017-01-07"), 4)
        );
        doReturn(expected).when(timeEntryRepository).list();

        ResponseEntity<List<TimeEntry>> response = controller.list();

        verify(actionCounter, times(1)).increment();
        verify(timeEntryRepository).list();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testUpdate() throws NoSuchObjectException {
        long timeEntryId = 1L;
        long projectId = 987L;
        long userId = 654L;
        TimeEntry expected = new TimeEntry(timeEntryId, projectId, userId, LocalDate.parse("2017-01-07"), 4);
        doReturn(expected)
            .when(timeEntryRepository)
            .update(eq(timeEntryId), any(TimeEntry.class));

        ResponseEntity response = controller.update(timeEntryId, expected);

        verify(actionCounter, times(1)).increment();
        verify(timeEntryRepository).update(timeEntryId, expected);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testUpdate_NotFound() {
        long nonExistentTimeEntryId = 1L;
        doReturn(null)
            .when(timeEntryRepository)
            .update(eq(nonExistentTimeEntryId), any(TimeEntry.class));

        ResponseEntity response = controller.update(nonExistentTimeEntryId, new TimeEntry());

        verify(actionCounter, times(0)).increment();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDelete() {
        long timeEntryId = 1L;

        ResponseEntity response = controller.delete(timeEntryId);

        verify(actionCounter, times(1)).increment();
        verify(timeEntrySummary, times(1)).record(anyDouble());
        verify(timeEntryRepository).delete(timeEntryId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
