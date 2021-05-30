package net.intelie.challenges;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventIteratorMemoryTest {

  EventIterator sut;
  List<Event> listEvents;

  public void buildIteratorWithElements() {
    listEvents = Collections.synchronizedList(new ArrayList<>());
    for (int i = 0; i < 5; i++) {
      Event event = new Event("Tipe_1", (long) 5 + i);
      listEvents.add(event);
    }

    sut = new EventIteratorMemory(listEvents);
  }

  @Test
  public void moveNextShouldReturnFalseIfAnEmptyListIsProvider() {
    sut = new EventIteratorMemory(new ArrayList<>());

    boolean responseOfMoveNext = sut.moveNext();

    assertEquals(false, responseOfMoveNext);
  }

  @Test
  public void moveNextShouldReturnTrueIfListHaveUneElement() {
    sut = new EventIteratorMemory(Arrays.asList(new Event("any_type", 1l)));

    boolean responseOfMoveNext = sut.moveNext();

    assertEquals(true, responseOfMoveNext);
  }

  @Test
  public void moveNextShouldReturnFalseIfCallNumberExceedsNumberOfElements() {
    buildIteratorWithElements();

    // the list have 5 element
    assertEquals(true, sut.moveNext());
    assertEquals(true, sut.moveNext());
    assertEquals(true, sut.moveNext());
    assertEquals(true, sut.moveNext());
    assertEquals(true, sut.moveNext());

    // not have a next element
    assertEquals(false, sut.moveNext());
  }

  @Test
  public void currentShouldReturnTheElementThatWasProvided() {
    Event event = new Event("any_type", 1l);
    sut = new EventIteratorMemory(Arrays.asList(event));

    sut.moveNext();

    assertEquals(event, sut.current());
  }

  @Test
  public void currentShouldReturnTheSecondElementWhenMoveNextIsCallTwice() {
    buildIteratorWithElements();
    Event secondElement = listEvents.get(1);

    sut.moveNext();
    sut.moveNext();

    assertEquals(secondElement, sut.current());
  }

  @Test(expected = IllegalStateException.class)
  public void currentShouldThrowsIfMoveNextIsNotCall() {
    buildIteratorWithElements();

    sut.current();
  }

  @Test(expected = IllegalStateException.class)
  public void removerShouldThrowsIfMoveNextIsNotCall() {
    buildIteratorWithElements();

    sut.remove();
  }

  @Test
  public void removerShouldRemoveTheCurrentElement() {
    buildIteratorWithElements();
    sut.moveNext();
    Event event = sut.current();

    sut.remove();

    assertNotEquals(event, sut.current());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotBePossibleUseIteratorIfTheLastElementIsRemoved() {
    Event event = new Event("any_type", 1l);
    List<Event> list = Collections.synchronizedList(new ArrayList<>());
    list.add(event);
    sut = new EventIteratorMemory(list);

    sut.moveNext();
    sut.remove();

    boolean result = sut.moveNext();
    assertEquals(false, result);
    sut.current();
  }
}
