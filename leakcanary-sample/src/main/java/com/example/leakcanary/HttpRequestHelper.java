package com.example.leakcanary;

import android.view.View;

/**
 * Fake class for the purpose of demonstrating a leak.
 */
public class HttpRequestHelper {

  private final View button;

  HttpRequestHelper(View button) {
    this.button = button;
  }
}
