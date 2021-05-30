package net.intelie.challenges;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import java.util.Random;
import org.junit.Before;

public class EventStoreMemoryTest {

  private EventStore sut = null;
  private Random random = new Random();

  @Before
  public void init() {
    sut = new EventStoreMemory();
  }

  public Event makeEvent() {
    return new Event("" + random.nextInt() + 1, 5l);
  }

  public Event addEventIntoSut() {
    Event event = makeEvent();
    sut.insert(event);
    return event;
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfEventIsNotProvide() {
    Event event = null;

    sut.insert(event);
  }

  @Test
  public void shouldAddEventIntoStore() {
    Event event = addEventIntoSut();

    EventIterator iteratorEvents = sut.query(event.type(), event.timestamp(), event.timestamp());

    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(event, iteratorEvents.current());
  }

  @Test
  public void shouldAdd2EventIntoStore() {
    Event event1 = addEventIntoSut();
    Event event2 = new Event(event1.type(), event1.timestamp());

    sut.insert(event2);

    EventIterator iteratorEvents = sut.query(event1.type(), event1.timestamp(), event1.timestamp());
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(event1, iteratorEvents.current());

    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(event2, iteratorEvents.current());
  }

  @Test
  public void shouldAddEachEventInYourGroup() {
    // should add each event in your group
    Event event1 = addEventIntoSut();
    Event event2 = addEventIntoSut();

    EventIterator iteratorEvents = sut.query(event1.type(), event1.timestamp(), event1.timestamp());
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(event1, iteratorEvents.current());

    assertEquals(false, iteratorEvents.moveNext());

    iteratorEvents = sut.query(event2.type(), event2.timestamp(), event2.timestamp());
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(event2, iteratorEvents.current());

    assertEquals(false, iteratorEvents.moveNext());
  }

  @Test
  public void shouldReturnEmptyIfIncorrectTypeIsProvided() {
    Event event1 = addEventIntoSut();

    EventIterator iteratorEvents =
        sut.query("Incorrect_type", event1.timestamp(), event1.timestamp());
    assertEquals(false, iteratorEvents.moveNext());
  }

  @Test
  public void shouldReturnEmptyIfIncorrectTimestampIsProvided() {
    Event event1 = addEventIntoSut();

    EventIterator iteratorEvents = sut.query(event1.type(), 10, 10);
    assertEquals(false, iteratorEvents.moveNext());
  }

  @Test
  public void shouldReturnAllEventsBelongInterval() {
    String eventType = "any_type";
    Long eventTimestamps = 5l;
    Event event1 = new Event(eventType, eventTimestamps);
    Event event2 = new Event(eventType, eventTimestamps);

    sut.insert(event1);
    sut.insert(event2);

    EventIterator iteratorEvents = sut.query(eventType, 4, 5);
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(true, iteratorEvents.moveNext());

    iteratorEvents = sut.query(eventType, 5, 5);
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(true, iteratorEvents.moveNext());

    iteratorEvents = sut.query(eventType, 5, 6);
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(true, iteratorEvents.moveNext());

    iteratorEvents = sut.query(eventType, 4, 6);
    assertEquals(true, iteratorEvents.moveNext());
    assertEquals(true, iteratorEvents.moveNext());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfTypeIsNotProviderIntoQuery() {
    Event event = addEventIntoSut();

    sut.query(null, event.timestamp(), event.timestamp());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfTypeIsEmptyProviderIntoQuery() {
    Event event = addEventIntoSut();

    sut.query("", event.timestamp(), event.timestamp());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfStartTimeIsNegative() {
    Event event = addEventIntoSut();

    sut.query(event.type(), Long.MIN_VALUE, event.timestamp());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfEndTimeIsNegative() {
    Event event = addEventIntoSut();

    sut.query(event.type(), event.timestamp(), Long.MIN_VALUE);
  }

  @Test
  public void shouldRemoveEventIfTypeIsProvider() {
    Event eventToRemove = addEventIntoSut();
    Event eventCanNotBeRemove = addEventIntoSut();

    EventIterator iterator =
        sut.query(eventToRemove.type(), eventToRemove.timestamp(), eventToRemove.timestamp());
    assertEquals(true, iterator.moveNext());
    assertEquals(eventToRemove, iterator.current());

    iterator = sut.query(eventCanNotBeRemove.type(), eventCanNotBeRemove.timestamp(),
        eventCanNotBeRemove.timestamp());
    assertEquals(true, iterator.moveNext());
    assertEquals(eventCanNotBeRemove, iterator.current());

    sut.removeAll(eventToRemove.type());
    iterator =
        sut.query(eventToRemove.type(), eventToRemove.timestamp(), eventToRemove.timestamp());
    assertEquals(false, iterator.moveNext());


    iterator = sut.query(eventCanNotBeRemove.type(), eventCanNotBeRemove.timestamp(),
        eventCanNotBeRemove.timestamp());
    assertEquals(true, iterator.moveNext());
    assertEquals(eventCanNotBeRemove, iterator.current());
  }


  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfTypeIsNotProviderInToRemove() {
    addEventIntoSut();

    sut.removeAll(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowsIfTypeIsEmptyInToRemove() {
    addEventIntoSut();

    sut.removeAll("");
  }


}
