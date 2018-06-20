package com.squareup.leakcanary;

/**
 * Implementations should have a public zero argument constructor.
 * TODO More javadoc
 */
public interface ReachabilityInspector {

  Reachability expectedReachability(LeakTraceElement element);

}
