package com.notfallkompass.risikoanalyse;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PdfExporter {
    // A4 Querformat bei 300 dpi: 11,69 x 8,27 Zoll => ca. 3508 x 2480 px.
    private static final int PAGE_WIDTH = 3508;
    private static final int PAGE_HEIGHT = 2480;

    public static void writeA4LandscapePdf(Context context, Uri uri, List<Risk> risks) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(info);
        MatrixRenderer.draw(page.getCanvas(), PAGE_WIDTH, PAGE_HEIGHT, risks, null, 0, 0);
        document.finishPage(page);

        OutputStream stream = null;
        try {
            stream = context.getContentResolver().openOutputStream(uri);
            if (stream == null) throw new IOException("Ausgabedatei konnte nicht geöffnet werden.");
            document.writeTo(stream);
        } finally {
            document.close();
            if (stream != null) stream.close();
        }
    }
}
