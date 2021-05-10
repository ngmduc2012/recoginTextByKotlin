// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mlkitwithcamera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.example.mlkitwithcamera.GraphicOverlay.Graphic;
import com.google.mlkit.vision.text.Text;

import static java.lang.Math.*;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends Graphic {

    private static final String TAG = "TextGraphic";
    private static final int TEXT_COLOR = Color.GREEN;
    private static final float STROKE_WIDTH = 4.0f;
    float textSize = 54.0f;
    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Element element;

    private int width;
    private int height;

    TextGraphic(GraphicOverlay overlay, Text.Element element, int width, int height) {
        super(overlay);

        this.element = element;
        this.width = width;
        this.height = height;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(textSize);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(element.getBoundingBox());
//        Log.d("ok","===========" + rect.height());
        if(rect.bottom >  width/2)
        {
            canvas.drawRect(3/2*width - rect.top , rect.left , rect.bottom - 2*rect.bottom + width , rect.right, rectPaint);
        }
        else {
            canvas.drawRect(width - rect.top , rect.left , rect.bottom + width - 2*rect.bottom , rect.right, rectPaint);
        }
        textSize = (float) (rect.width()/(5.8*3));
        textPaint.setTextSize(textSize);

        canvas.rotate(90f,width - rect.top - rect.height() , rect.left);
        // Renders the text at the bottom of the box.
        canvas.drawText(element.getText(),width- rect.top - rect.height() , rect.left, textPaint);
        canvas.rotate(-90f,width - rect.top- rect.height(), rect.left);
    }
}

