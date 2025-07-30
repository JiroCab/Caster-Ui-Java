package casterui.io.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class BarHelper extends Bar{
    private static Rect scissor = new Rect();

    private Floatp fraction;
    private CharSequence name = "";
    private float value, lastValue, blink, outlineRadius;
    private Color blinkColor = new Color(), outlineColor = new Color();

    public BarHelper(String name, Color color, Floatp fraction){
        this.fraction = fraction;
        this.name = Core.bundle.get(name, name);
        this.blinkColor.set(color);
        lastValue = value = fraction.get();
        setColor(color);
    }

    public BarHelper(Prov<CharSequence> name, Prov<Color> color, Floatp fraction){
        this.fraction = fraction;
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            this.name = name.get();
            this.blinkColor.set(color.get());
            setColor(color.get());
        });
    }

    public BarHelper(){

    }

    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get());


        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        if(outlineRadius > 0){
            Draw.color(outlineColor);
            bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius*2, height + outlineRadius*2);
        }

        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);
        Draw.alpha(parentAlpha);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").width){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").width, height);
                ScissorStack.pop();
            }
        }

        Draw.color();

        Font font = Fonts.def;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);

        float prevSX = font.getData().scaleX, prevSY = font.getData().scaleX;
        font.getData().setScale(0.8f);
        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();

        font.getCache().addText(name, x + width / 2.2f - lay.width / 2.2f, y + height / 2.2f + lay.height / 2.2f + 1);
        font.getCache().draw(parentAlpha);
        font.getData().setScale(prevSX, prevSY);

        Pools.free(lay);
    }
}
