package veny.smevente.client.mvp;

import com.google.gwt.user.client.ui.Widget;

/**
 * The <i>View</i> is an interface that displays data (the model)
 * and routes user commands (events) to the presenter to act upon that data.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public interface View {

    /**
     * Returns the view as a GWT {@link Widget}.
     *
     * @return The widget.
     */
    Widget asWidget();

}
