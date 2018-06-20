/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import static android.graphics.Bitmap.Config.ARGB_8888;

public final class DisplayLeakConnectorView extends View {

  private static final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Paint rootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Paint reachablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Paint unreachablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Paint leakPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  static {
    iconPaint.setColor(LeakCanaryUi.LIGHT_GREY);
    rootPaint.setColor(LeakCanaryUi.ROOT_COLOR);
    reachablePaint.setColor(LeakCanaryUi.REACHABLE_COLOR);
    unreachablePaint.setColor(LeakCanaryUi.UNREACHEABLE_COLOR);
    leakPaint.setColor(LeakCanaryUi.LEAK_COLOR);
    clearPaint.setColor(Color.TRANSPARENT);
    clearPaint.setXfermode(LeakCanaryUi.CLEAR_XFER_MODE);
  }

  public enum Type {
    START,
    START_LAST_REACHABLE,
    NODE_UNKNOWN,
    NODE_FIRST_UNREACHABLE,
    NODE_UNREACHABLE,
    NODE_REACHABLE,
    NODE_LAST_REACHABLE,
    END,
    END_FIRST_UNREACHABLE,
  }

  private Type type;
  private Bitmap cache;

  public DisplayLeakConnectorView(Context context, AttributeSet attrs) {
    super(context, attrs);

    type = Type.NODE_UNKNOWN;
  }

  @SuppressWarnings("SuspiciousNameCombination") @Override protected void onDraw(Canvas canvas) {
    int width = getWidth();
    int height = getHeight();

    if (cache != null && (cache.getWidth() != width || cache.getHeight() != height)) {
      cache.recycle();
      cache = null;
    }

    if (cache == null) {
      cache = Bitmap.createBitmap(width, height, ARGB_8888);

      Canvas cacheCanvas = new Canvas(cache);

      float halfWidth = width / 2f;
      float halfHeight = height / 2f;
      float thirdWidth = width / 3f;

      float strokeSize = LeakCanaryUi.dpToPixel(4f, getResources());

      iconPaint.setStrokeWidth(strokeSize);
      reachablePaint.setStrokeWidth(strokeSize);
      unreachablePaint.setStrokeWidth(strokeSize);
      leakPaint.setStrokeWidth(strokeSize);
      rootPaint.setStrokeWidth(strokeSize);

      switch (type) {
        case NODE_UNKNOWN:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, height, leakPaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, iconPaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, thirdWidth, clearPaint);
          break;
        case NODE_UNREACHABLE:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, height, unreachablePaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, unreachablePaint);
          break;
        case NODE_FIRST_UNREACHABLE:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, leakPaint);
          cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, unreachablePaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, unreachablePaint);
          break;
        case NODE_REACHABLE:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, height, reachablePaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, reachablePaint);
          break;
        case NODE_LAST_REACHABLE:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, reachablePaint);
          cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, leakPaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, reachablePaint);
          break;
        case START: {
          float radiusClear = halfWidth - strokeSize / 2f;
          cacheCanvas.drawRect(0, 0, width, radiusClear, reachablePaint);
          cacheCanvas.drawCircle(0, radiusClear, radiusClear, clearPaint);
          cacheCanvas.drawCircle(width, radiusClear, radiusClear, clearPaint);
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, reachablePaint);
          cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, reachablePaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, reachablePaint);
          break;
        }
        case START_LAST_REACHABLE: {
          float radiusClear = halfWidth - strokeSize / 2f;
          cacheCanvas.drawRect(0, 0, width, radiusClear, reachablePaint);
          cacheCanvas.drawCircle(0, radiusClear, radiusClear, clearPaint);
          cacheCanvas.drawCircle(width, radiusClear, radiusClear, clearPaint);
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, reachablePaint);
          cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, leakPaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, reachablePaint);
          break;
        }
        case END:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, unreachablePaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, unreachablePaint);
          break;
        case END_FIRST_UNREACHABLE:
          cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, leakPaint);
          cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, unreachablePaint);
          break;
        default:
          throw new UnsupportedOperationException("Unknown type " + type);
      }
    }
    canvas.drawBitmap(cache, 0, 0, null);
  }

  public void setType(Type type) {
    if (type != this.type) {
      this.type = type;
      if (cache != null) {
        cache.recycle();
        cache = null;
      }
      invalidate();
    }
  }
}
