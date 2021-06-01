package com.hrsh.doodledraw;

import android.graphics.Paint;
import android.graphics.Path;

public class DrawAction {
    public Path path;
    public Paint paint;

    public DrawAction(Path path, Paint paint){
        this.path = path;
        this.paint = paint;
    }
}
