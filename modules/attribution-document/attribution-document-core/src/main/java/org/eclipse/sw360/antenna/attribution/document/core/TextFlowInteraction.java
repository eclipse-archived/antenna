package org.eclipse.sw360.antenna.attribution.document.core;

import rst.pdfbox.layout.text.Position;
import rst.pdfbox.layout.text.TextFlow;

import java.io.IOException;

public interface TextFlowInteraction {
    Position addYourText(TextFlow text, float pageHeight, float pageWidth ) throws IOException;
}
