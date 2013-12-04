package veny.smevente.client;

import veny.smevente.client.l10n.SmeventeConstants;
import veny.smevente.client.mvp.Presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog window that can be associated with a general presenter representing the dialog content.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 24.6.2010
 */
public class SmeventeDialog extends DialogBox {

    /** I18n messages. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    /** Presenter with dialog content. */
    private final Presenter< ? > presenter;
    /** Panel with buttons. */
    private final HorizontalPanel buttonPanel;
    /** OK button. */
    private final Button okButton;

    /**
     * Constructor.
     * @param caption window caption
     * @param presenter presenter with dialog content
     */
    public SmeventeDialog(final String caption, final Presenter< ? > presenter) {
        this(caption, presenter, true);
    }

    /**
     * Constructor.
     * @param caption window caption
     * @param presenter presenter with dialog content
     * @param ok whether to display OK button
     */
    public SmeventeDialog(final String caption, final Presenter< ? > presenter, final boolean ok) {
        this.presenter = presenter;

        setText(caption);
        setGlassEnabled(true);
        setAnimationEnabled(true);
        addStyleName("smevente-dlg");

        // create a table to layout the content
        final VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        // add the body
        final Widget body = this.presenter.getView().asWidget();
        dialogContents.add(body);
        dialogContents.setCellHorizontalAlignment(body, HasHorizontalAlignment.ALIGN_CENTER);

        // button panel
        buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        dialogContents.add(buttonPanel);
        dialogContents.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

        // add a close button at the bottom of the dialog
        final Button closeButton = new Button(CONSTANTS.close());
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(final ClickEvent event) {
                hide();
            }
        });
        buttonPanel.add(closeButton);
        buttonPanel.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);

        // add a close button at the bottom of the dialog
        okButton = new Button(CONSTANTS.ok());
        buttonPanel.setCellHorizontalAlignment(okButton, HasHorizontalAlignment.ALIGN_RIGHT);
        if (ok) {
            buttonPanel.add(okButton);
        }
    }

    /**
     * Gets panel with button.
     * @return panel with button
     */
    public HorizontalPanel getButtonPanel() {
        return buttonPanel;
    }

    /**
     * Gets OK button.
     * @return OK button
     */
    public Button getOkButton() {
        return okButton;
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        presenter.show(null);
        super.show();
    }

    /** {@inheritDoc} */
    @Override
    public void hide() {
        presenter.hide();
        super.hide();
    }

}
