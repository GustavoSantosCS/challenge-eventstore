package net.intelie.challenges;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class EventStoreMemoryThreadTest {

  @Test
  public void shouldBePossibleToInsertInConcurrent() {
    String type = "any_type";
    int numberOfThread = 50;
    EventStore sut = new EventStoreMemory();
    AtomicBoolean hasException = new AtomicBoolean(false);

    Runnable insertTask = () -> {
      try {
        sut.insert(new Event(type, System.currentTimeMillis()));
        sut.insert(new Event(type, System.currentTimeMillis()));
        sut.insert(new Event(type, System.currentTimeMillis()));
      } catch (Exception e) {
        hasException.set(true);
        e.printStackTrace();
      }
    };

    long startTime = System.currentTimeMillis();
    ExecutorService threadPool = Executors.newScheduledThreadPool(10);
    for (int i = 0; i < numberOfThread * 3; i++) {
      threadPool.execute(insertTask);
    }

    threadPool.shutdown();

    try {
      threadPool.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();

    EventIterator iterator = sut.query(type, startTime, endTime);

    assertEquals(false, hasException.get());
    for (int i = 1; i < numberOfThread * 3; i++) {
      assertEquals(true, iterator.moveNext());
    }
  }

  @Test
  public void shouldBePossibleToRemoveInConcurrent() {
    EventStore sut = new EventStoreMemory();
    AtomicBoolean hasException = new AtomicBoolean(false);
    for (int i = 0; i < 15; i++) {
      for (int y = 0; y < 11; y++) {
        sut.insert(new Event("Type_" + y, 55l));
      }
    }

    Runnable removeTask1 = () -> {
      try {
        sut.removeAll("Type_1");
      } catch (Exception e) {
        hasException.set(true);
        e.printStackTrace();
      }
    };
    Runnable removeTask2 = () -> {
      try {
        sut.removeAll("Type_2");
      } catch (Exception e) {
        hasException.set(true);
        e.printStackTrace();
      }
    };
    Runnable removeTask3_4 = () -> {
      try {
        sut.removeAll("Type_3");
        sut.removeAll("Type_4");
      } catch (Exception e) {
        hasException.set(true);
        e.printStackTrace();
      }
    };
    Runnable removeTask5 = () -> {
      try {
        sut.removeAll("Type_5");
      } catch (Exception e) {
        hasException.set(true);
        e.printStackTrace();
      }
    };

    ExecutorService threadPool = Executors.newScheduledThreadPool(5);
    threadPool.execute(removeTask1);
    threadPool.execute(removeTask2);
    threadPool.execute(removeTask3_4);
    threadPool.execute(removeTask5);

    threadPool.shutdown();

    try {
      threadPool.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(false, hasException.get());

    // Apagado
    EventIterator iterator = sut.query("Type_3", 54l, 56l);
    assertEquals(false, iterator.moveNext());

    // Nao Apagado
    iterator = sut.query("Type_10", 54l, 56l);
    assertEquals(true, iterator.moveNext());
  }

  @Test
  public void shouldBePossibleToMakeMultipleTackInConcurrentInToEventStore() {
    EventStore sut = new EventStoreMemory();
    AtomicBoolean hasExceptionInsert = new AtomicBoolean(false);
    AtomicBoolean hasExceptionRemove = new AtomicBoolean(false);
    AtomicBoolean hasExceptionMakeQuery = new AtomicBoolean(false);
    for (int i = 0; i < 15; i++) {
      sut.insert(new Event("Type_2", 55l));
    }
    for (int i = 0; i < 15; i++) {
      sut.insert(new Event("Type_1", 55l));
    }
    Runnable insertTaskTypeA = () -> {
      try {
        for (int i = 0; i < 25; i++) {
          sut.insert(new Event("type_A", System.currentTimeMillis()));
        }
      } catch (Exception e) {
        hasExceptionInsert.set(true);
        e.printStackTrace();
      }
    };
    Runnable insertTaskTypeB = () -> {
      try {
        for (int i = 0; i < 50; i++) {
          sut.insert(new Event("type_B", System.currentTimeMillis()));
        }
      } catch (Exception e) {
        hasExceptionInsert.set(true);
        e.printStackTrace();
      }
    };
    Runnable removerTaskType2 = () -> {
      try {
        sut.removeAll("type_2");
      } catch (Exception e) {
        hasExceptionRemove.set(true);
        e.printStackTrace();
      }
    };
    Runnable makeQueryType1 = () -> {
      try {
        sut.query("type_1", 54l, 56l);
      } catch (Exception e) {
        hasExceptionMakeQuery.set(true);
        e.printStackTrace();
      }
    };

    ExecutorService threadPool = Executors.newScheduledThreadPool(6);
    threadPool.execute(insertTaskTypeA);
    threadPool.execute(makeQueryType1);
    threadPool.execute(insertTaskTypeB);
    threadPool.execute(insertTaskTypeA);
    threadPool.execute(insertTaskTypeB);
    threadPool.execute(insertTaskTypeA);
    threadPool.execute(removerTaskType2);
    threadPool.execute(insertTaskTypeB);
    threadPool.shutdown();
    try {
      threadPool.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(false, hasExceptionInsert.get());
    assertEquals(false, hasExceptionMakeQuery.get());
    assertEquals(false, hasExceptionRemove.get());
  }

  @Test
  public void shouldBePossibleToMakeMultipleTackInConcurrentInToEventIterator() {
    AtomicBoolean hasExceptionToRemove = new AtomicBoolean(false);
    AtomicBoolean hasExceptionToCallMoveNext = new AtomicBoolean(false);
    ExecutorService threadPool = Executors.newScheduledThreadPool(3);
    List<Event> listEvents = Collections.synchronizedList(new ArrayList<>());
    Random random = new Random();
    for (int i = 0; i < 15; i++) {
      listEvents.add(new Event("Type_1", random.nextLong() + 1));
    }

    EventIterator sut = new EventIteratorMemory(listEvents);

    Runnable removeTask = () -> {
      try {
        sut.remove();
      } catch (Exception e) {
        hasExceptionToRemove.set(true);
        e.printStackTrace();
      }
    };

    Runnable callNexMoveNext = () -> {
      try {
        sut.moveNext();
        sut.current();
      } catch (Exception e) {
        hasExceptionToCallMoveNext.set(true);
        e.printStackTrace();
      }
    };

    sut.moveNext();

    threadPool.execute(removeTask);
    threadPool.execute(callNexMoveNext);
    threadPool.execute(callNexMoveNext);
    threadPool.execute(callNexMoveNext);
    threadPool.execute(callNexMoveNext);
    threadPool.execute(removeTask);
    threadPool.execute(callNexMoveNext);
    threadPool.execute(removeTask);
    threadPool.execute(callNexMoveNext);
    threadPool.shutdown();
    try {
      threadPool.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(false, hasExceptionToRemove.get());
    assertEquals(false, hasExceptionToCallMoveNext.get());
  }
}
