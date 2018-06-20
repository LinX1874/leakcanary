package com.squareup.leakcanary;

import android.app.Activity;

public class ActivityReachabilityInspector implements ReachabilityInspector {
  @Override public Reachability expectedReachability(LeakTraceElement element) {
    if (!isActivity(element)) {
      return Reachability.UNKNOWN;
    }

    // TODO it'd be better if we had a map to access fields.
    for (LeakReference fieldReference : element.fieldReferences) {
      if (fieldReference.name.equals("mDestroyed")) {
        boolean destroyed = fieldReference.value.equals(Boolean.toString(true));
        return destroyed ? Reachability.UNREACHABLE : Reachability.REACHABLE;
      }
    }

    return Reachability.UNKNOWN;
  }

  private boolean isActivity(LeakTraceElement element) {
    for (String className : element.classHierarchy) {
      if (className.equals(Activity.class.getName())) {
        return true;
      }
    }
    return false;
  }
}
