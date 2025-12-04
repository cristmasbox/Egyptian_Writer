package com.blueapps.egyptianwriter.editor;

import org.w3c.dom.Document;

public interface FileListener {

    void onGlyphXChanged(Document GlyphX);

    void onMdCChanged(String mdc);

}
