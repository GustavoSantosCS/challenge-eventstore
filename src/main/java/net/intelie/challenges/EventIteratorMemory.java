package net.intelie.challenges;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventIteratorMemory implements EventIterator {

  // The ArrayList is the satisfaction structure for the use case,
  // be case the operation of get, "next" have O(1) of complexity using the index to helper.
  // The remove operation have the O(n) cost the ArrayList is created using the
  // Collections.synchronizedList to get a Thread-safe structure

  private List<Event> listEvents;
  private AtomicInteger index;
  private AtomicBoolean moveNextToBeCall;
  private AtomicBoolean withoutElements;

  public EventIteratorMemory(List<Event> listEvents) {
    this.listEvents = listEvents;
    this.index = new AtomicInteger(-1);
    this.moveNextToBeCall = new AtomicBoolean(false);
    this.withoutElements = new AtomicBoolean(listEvents.isEmpty());
  }

  @Override
  public synchronized boolean moveNext() {
    this.moveNextToBeCall.set(true);
    if (hasNext()) {
      index.incrementAndGet();
      return true;
    }

    return false;
  }

  /**
   * Helper to verifies if is possible to move to the next element of iterator
   * 
   * @return true if is possible to move, false otherwise
   */
  private boolean hasNext() {
    // First make sure the list has element, if not, can not move the pointer
    if (listEvents.isEmpty()) {
      return false;
    }

    // then check if list have a next element, if not, can not move the pointer
    if ((index.get() + 1) >= listEvents.size())
      return false;


    // if it passes both can move the next element
    return true;
  }

  @Override
  public synchronized Event current() {
    // First check if moveNext was be call and if has next element
    if (!this.moveNextToBeCall.get() || this.withoutElements.get()) {
      throw new IllegalStateException();
    }

    return listEvents.get(index.get());
  }

  @Override
  public synchronized void remove() {
    // First check if moveNext was be call and if not has next element
    if (!this.moveNextToBeCall.get() || this.withoutElements.get())
      throw new IllegalStateException();

    listEvents.remove(index.get());

    // adjusting the pointer
    if (!this.listEvents.isEmpty()) {
      moveNext();
    } else {
      this.withoutElements.set(true);
      index.set(listEvents.size() - 1);
    }
  }

  @Override
  public void close() throws Exception {
    // This method has no implementation due to the
    // fact that there is no resource to be closed
  }
}
