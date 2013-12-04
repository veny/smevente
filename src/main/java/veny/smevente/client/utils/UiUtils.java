package veny.smevente.client.utils;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collection of utilities and convenience method for UI.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 3.2.2011
 */
public final class UiUtils {

    /** Suppresses default constructor, ensuring non-instantiability. */
    private UiUtils() { }

    /**
     * Adds cell into result set table.
     * @param table table where the cell will be inserted into
     * @param row row
     * @param col column
     * @param w widget to be inserted
     */
    public static void addCell(final FlexTable table, final int row, final int col, final Widget w) {
        final String style  = (0 != (row % 2) ? "resultTable-cell-odd" : "resultTable-cell-even");
        table.setWidget(row, col, w);
        table.getFlexCellFormatter().addStyleName(row, col, style);
    }

}
