package no.java.swing;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;
import org.apache.commons.lang.Validate;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: override and optimize in the same way as the default cell renderer
// todo: implement ListCellRenderer, TreeCellRenderer

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class HighlightingCellRenderer extends JTextField implements TableCellRenderer {

    public static final String HIGHLIGHT_PATTERN_PROPERTY = HighlightingCellRenderer.class.getSimpleName() + ".pattern";
    private static final String DOTS = "...";
    private final Highlighter.HighlightPainter highlightPainter;
    private final DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();

    public HighlightingCellRenderer(Color color) {
        Validate.notNull(color);
        setBorder(
                Borders.createEmptyBorder(
                        Sizes.dluY(1),
                        Sizes.dluX(2),
                        Sizes.dluY(1),
                        Sizes.dluX(2)
                )
        );
        setEditable(false);
        highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(color);
        setIgnoreRepaint(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        cellRenderer.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, col);
        String text = value == null ? "" : value.toString();
        setIgnoreRepaint(true);
        setText(text);
        setFont(cellRenderer.getFont());
        setBackground(cellRenderer.getBackground());
        setForeground(cellRenderer.getForeground());
        getHighlighter().removeAllHighlights();
        Pattern pattern = (Pattern)table.getClientProperty(HIGHLIGHT_PATTERN_PROPERTY);
        if (value != null && !isSelected && pattern != null) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                try {
                    getHighlighter().addHighlight(m.start(), m.end(), highlightPainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    public int getPreferredRowHeight(JTable table) {
        return getTableCellRendererComponent(table, null, false, false, 0, 0).getPreferredSize().height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        Insets insets = getInsets();
        if (width < getPreferredSize().width) {
            // todo: fix magic number bug for insets.top + 5
            int pos = viewToModel(new Point(width - insets.right, insets.top + 5));
            if (pos > 0) {
                try {
                    Rectangle rectangle;
                    int dotsWidth = g.getFontMetrics().stringWidth(DOTS);
                    while ((rectangle = modelToView(pos)).x > width - dotsWidth - insets.right) {
                        if (--pos < 1) {
                            break;
                        }
                    }
                    g.setColor(getBackground());
                    g.fillRect(rectangle.x, rectangle.y, width - rectangle.x - insets.right, rectangle.height);
                    g.setColor(getForeground());
                    g.drawString(DOTS, rectangle.x, getBaseline(width, getHeight()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
        cellRenderer.getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
    }

}
