package com.squareup.leakcanary;

import android.view.View;

public class ViewReachabilityInspector implements ReachabilityInspector {
  @Override public Reachability expectedReachability(LeakTraceElement element) {
    if (!isView(element)) {
      return Reachability.UNKNOWN;
    }

    // TODO it'd be better if we had a map to access fields.
    for (LeakReference fieldReference : element.fieldReferences) {
      if (fieldReference.name.equals("mAttachInfo")) {
        boolean detached = fieldReference.value.equals("null");
        return detached ? Reachability.UNREACHABLE : Reachability.REACHABLE;
      }
    }

    return Reachability.UNKNOWN;
  }

  private boolean isView(LeakTraceElement element) {
    for (String className : element.classHierarchy) {
      if (className.equals(View.class.getName())) {
        return true;
      }
    }
    return false;
  }
}
