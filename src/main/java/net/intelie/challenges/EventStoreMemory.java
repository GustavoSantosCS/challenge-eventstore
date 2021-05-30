package net.intelie.challenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventStoreMemory implements EventStore {

  // I'm using ConcurrentHashMap because the helper segregation the Event by type, for this the key
  // of ConcurrentHashMap is the type of Event. The value of ConcurrentHashMap is ArrayList build
  // using the "helper" of Collections.synchronizedList to get a Thread-safe structure. The List
  // contains all the Event of same type. The is useful to removeAll Event of same type and helper
  // en the query method.
  // The cost Big-O of operations is:
  // add: at the majority is O(1), but if the ArrayList of Event the type X need more space the cost
  // became O(n), because to increase its own space, the ArrayList needs to copy all the elements of
  // the new array;
  // removerAll: the cost is O(1), because removing ConcurrentHashMap is O(1)
  // when the list stops being pointed, the GarbageCollector will clear it from memory;
  // query: the cost is O(1 + k), k represents the number of Event the have same type;

  private ConcurrentHashMap<String, List<Event>> eventStoreMap = new ConcurrentHashMap<>();

  @Override
  public synchronized void insert(Event event) {
    if (event == null)
      throw new IllegalArgumentException();

    List<Event> trackList = eventStoreMap.get(event.type());
    if (trackList == null) {
      trackList = Collections.synchronizedList(new ArrayList<Event>());
      this.eventStoreMap.put(event.type(), trackList);
    }

    trackList.add(event);
  }

  @Override
  public void removeAll(String type) {
    if (type == null || type != null && type.trim().isEmpty())
      throw new IllegalArgumentException();

    eventStoreMap.remove(type);
  }

  @Override
  public synchronized EventIterator query(String type, long startTime, long endTime) {
    if (invalidArgumentsQuery(type, startTime, endTime))
      throw new IllegalArgumentException();

    if (!eventStoreMap.containsKey(type)) {
      return new EventIteratorMemory(Collections.synchronizedList(new ArrayList<Event>()));
    }

    Predicate<Event> condition =
        (event) -> event.timestamp() >= startTime && event.timestamp() <= endTime;

    EventIterator iteratorResult = new EventIteratorMemory(Collections.synchronizedList(
        eventStoreMap.get(type).stream().filter(condition).collect(Collectors.toList())));

    return iteratorResult;
  }

  private boolean invalidArgumentsQuery(String type, long startTime, long endTime) {
    return type == null || type != null && type.trim().isEmpty() || startTime < 0
        || endTime < startTime;
  }
}
