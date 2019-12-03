package io.pivotal.pal.tracker;

import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {
    private Map<Long, TimeEntry> table = new LinkedHashMap<>();
    private AtomicLong counter = new AtomicLong(0);

    public TimeEntry create(TimeEntry timeEntry) {
        if (timeEntry == null)
            throw new NullPointerException("TimeEntry is null");
        if (timeEntry.getId() == null) {
            timeEntry.setId(counter.addAndGet(1L));
        }

        table.put(timeEntry.getId(), timeEntry);

        return timeEntry;
    }

    public TimeEntry find(long timeEntryId) {
        return table.get(timeEntryId);
    }

    public List<TimeEntry> list() {
        return table.values().stream()
                .collect(Collectors.toList());
    }

    public TimeEntry update(Long id, TimeEntry timeEntry) {
        if (timeEntry == null)
            throw new NullPointerException("TimeEntry is null");
        if (id == null)
            throw new NullPointerException("TimeEntry ID is null");

        TimeEntry old = table.get(id);
        if (table.containsKey(id)) {
            timeEntry.setId(id);
            table.put(id, timeEntry);
            old = timeEntry;
        }

        return old;
    }

    public void delete(Long id) {
        table.remove(id);
    }
}
