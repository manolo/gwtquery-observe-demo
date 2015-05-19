package com.google.gwt.query.client.plugins;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.$$;
import static com.google.gwt.query.client.GQuery.console;
import static com.google.gwt.query.client.GQuery.window;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.query.client.plugins.observe.Observe;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.ChangeRecord;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.MutationRecord;
import com.google.gwt.query.client.plugins.observe.Observe.MutationListener;
import com.google.gwt.query.client.plugins.observe.Observe.ObserveListener;
import com.google.gwt.user.client.Timer;

/**
 */
public class ObserveSample implements EntryPoint {
  final GQuery g =  $("div");
  final Properties o = $$();
  final JsArrayString a = JsArray.createArray().cast();

  public void onModuleLoad() {
    $(window).prop("e", g.get(0)).prop("o", o).prop("a", a);

    g.as(Observe.Observe).mutation(
        Observe.createMutationInit()
               .attributes(true)
               .characterData(true)
               .childList(true)
               .subtree(true),
     new MutationListener() {
      public void onMutation(List<MutationRecord> mutations) {
        for (MutationRecord r: mutations) {
          console.log("M - > ", r.getDataImpl());
        }
      }
    });

    Observe.observe(o, new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        for(ChangeRecord r : changes) {
          console.log("O -> ", r.getDataImpl());
        }
      }
    });

    Observe.observe(a, new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        for(ChangeRecord r : changes) {
          console.log("A -> ", r.getDataImpl());
        }
      }
    });

    testObserveMutation();
    testObserveObject();
    testObserveArray();
  }

  int mutationStatus = 0;
  public void testObserveMutation() {
    delayTestFinish(500);

    final MutationListener handler = new MutationListener() {
      public void onMutation(List<MutationRecord> mutations) {
        assertEquals("attributes", mutations.get(0).type());
        assertEquals("foo", mutations.get(0).attributeName());
        assertNull(mutations.get(0).oldValue());
        mutationStatus += mutations.size();
        g.as(Observe.Observe).disconnect();
        g.attr("Foo", "bar");
      }
    };

    g.as(Observe.Observe).mutation(Observe.createMutationInit().attributes(true), handler);

    g.attr("foo", "bar");
    new Timer() {
      public void run() {
        finishTest();
//        g.as(Observe.Observe).disconnect(handler);
      }
    }.schedule(200);
  }

  int lastObjectStatus, objectStatus;
  public void testObserveObject() {
    delayTestFinish(500);

    final ObserveListener handler = new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        objectStatus += changes.size();
      }
    };

    Observe.observe(o, Observe.createObserveInit().add(true).delete(true).update(true), handler);

    o.set("foo", "bar1");
    o.set("bar", "foo1");
    new Timer(){
      public void run() {
        o.set("foo", "bar2");
        o.remove("foo");
      }
    }.schedule(100);

    new Timer(){
      public void run() {
        assertTrue(objectStatus >= 3);
        lastObjectStatus = objectStatus;
        Observe.unobserve(o, handler);
        o.set("bar", "foo2");
      }
    }.schedule(200);

    new Timer() {
      public void run() {
        assertEquals(objectStatus, lastObjectStatus);
        finishTest();
      }
    }.schedule(400);
  }

  int lastArrayStatus, arrayStatus;
  public void testObserveArray() {
    delayTestFinish(500);

    final ObserveListener handler = new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        arrayStatus += changes.size();
      }
    };

    Observe.observe(a, handler);

    a.push("Hi");
    new Timer(){
      public void run() {
        assertTrue(arrayStatus == 1 || arrayStatus == 2);
        JsUtils.jsni(a,"splice", 0, 1);
      }
    }.schedule(100);

    new Timer(){
      public void run() {
        assertTrue(arrayStatus == 2 || arrayStatus == 4);
        lastArrayStatus = arrayStatus;
        Observe.unobserve(a, handler);
        a.push("Bye");
      }
    }.schedule(200);

    new Timer() {
      public void run() {
        assertEquals(lastArrayStatus, arrayStatus);
        finishTest();

      }
    }.schedule(400);
  }

  private void delayTestFinish(int i) {
  }
  private void assertEquals(Object a, Object b) {
    assert a.equals(b) : "Not equals: " + a + " : " + b;
  }
  private void assertEquals(int a, int b) {
    assert a == b  : "Not equals: " + a + " : " + b;;
  }
  private void assertTrue(boolean b) {
    assert b : "false instead of true";
  }

  private void finishTest() {
  }
  private void assertNull(Object oldValue) {
  }
 }
