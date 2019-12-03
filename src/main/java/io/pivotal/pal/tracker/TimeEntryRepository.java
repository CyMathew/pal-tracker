package io.pivotal.pal.tracker;

import java.rmi.NoSuchObjectException;
import java.util.List;

public interface TimeEntryRepository {
    public TimeEntry create(TimeEntry timeEntry);

    public TimeEntry find(long timeEntryId);

    public List<TimeEntry> list();

    public TimeEntry update(Long id, TimeEntry timeEntry);

    public void delete(Long timeEntryId);
}
