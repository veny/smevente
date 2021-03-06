package veny.smevente.client.uc;

import java.util.HashMap;
import java.util.Map;

import net.auroris.ColorPicker.client.ColorPicker;
import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithValidation;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;
import eu.maydu.gwt.validation.client.validators.numeric.LongValidator;

/**
 * Presenter for Store (Create or Update) a procedure.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StoreProcedurePresenter
    extends AbstractPresenter<StoreProcedurePresenter.StoreProcedureView>
    implements HeaderHandler {

    /**
     * View interface for storing a procedure.
     *
     * @author Vaclav Sykora
     * @since 22.8.2010
     */
    public interface StoreProcedureView extends View {
        /**
         * Getter for the name text field.
         * @return the input field for the name
         */
        TextBox getName();
        /**
         * Getter for the message text field.
         * @return the input field for the message text
         */
        TextArea getMessageText();
        /**
         * Getter for the time text field.
         * @return the input field for the time
         */
        TextBox getTime();
        /**
         * Getter for the time label field.
         * @return the label for the time
         */
        Label getTimeLabel();
        /**
        * Getter for the color text field.
        * @return the input field for the color
        */
        TextBox getColor();
        /**
         * Getter for the color label field.
         * @return the label for the color
         */
        Label getColorLabel();
        /**
         * Getter for the button to select color.
         * @return the select color element
         */
        Button getSelectColor();
        /**
         * Getter for the button to submit.
         * @return the submit element
         */
        HasClickHandlers getSubmit();
        /**
         * Getter for the button to cancel.
         * @return the submit element
         */
        HasClickHandlers getCancel();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
        /**
         * @return the hidden field wit procedure ID
         */
        Hidden getProcedureId();
    }

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    /**
     * Type of procedure to be created or updated.
     */
    private Event.Type type = Event.Type.IN_CALENDAR;

    /**
     * Dialog of color picker.
     */
    private ColorPickerDialog pickerDialog = null;

    private static class ColorPickerDialog extends DialogBox {
        /**
         *
         */
        private ColorPicker picker;

        /**
         *
         */
        private TextBox selectedColor;

        /**
         *
         * @param colorField The current color used as initial color value.
         */
        public ColorPickerDialog(final TextBox colorField) {
            selectedColor = colorField;
            setText(CONSTANTS.selectColor());

            setWidth("435px");
            setHeight("350px");

            // Define the panels
            VerticalPanel panel = new VerticalPanel();
            FlowPanel okcancel = new FlowPanel();
            picker = new ColorPicker();

            // Define the buttons
            Button ok = new Button(CONSTANTS.ok());
            ok.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    selectedColor.setText(picker.getHexColor());
                    selectedColor.getElement().getStyle().setProperty("backgroundColor", "#" + selectedColor.getText());
                    ColorPickerDialog.this.hide();
                }
            });

            Button cancel = new Button(CONSTANTS.cancel());
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    ColorPickerDialog.this.hide();
                }
            });

            okcancel.add(ok);
            okcancel.add(cancel);

            // Put it together
            panel.add(picker);
            panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(okcancel);

            setWidget(panel);
        }
    }

    /**
     * Constructor.
     * @param type the type of procedure
     */
    public StoreProcedurePresenter(final Event.Type type) {
        this.type = type;
    }

    // -------------------------------------------------- HeaderHandler Methods

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        clean();
    }
    /** {@inheritDoc} */
    @Override
    public void unitMemberChanged(final HeaderEvent event) { /* I don't care */ }
    /** {@inheritDoc} */
    @Override
    public void dateChanged(final HeaderEvent event) { /* I don't care */ }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // register this to display/hide the loading progress bar
        ebusUnitSelection = eventBus.addHandler(HeaderEvent.TYPE, this);

        if (type == Event.Type.IN_CALENDAR) {
            pickerDialog = new ColorPickerDialog(view.getColor());

            view.getSelectColor().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    try {
                        pickerDialog.picker.setHex(view.getColor().getText());
                    } catch (Exception e) {
                        // Do nothing if color format is bad - probably
                        // new procedure is being created, so the color
                        // code is empty.
                        e.equals(null); // Checkstyle - workaround line
                    }
                    pickerDialog.showRelativeTo(RootPanel.get());
                }
            });
        } else {
            view.getTime().setVisible(false);
            view.getTimeLabel().setVisible(false);
            view.getColor().setVisible(false);
            view.getColorLabel().setVisible(false);
            view.getSelectColor().setVisible(false);
        }

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // validation
                if (!validator.validate()) {
                    // One (or more) validations failed. The actions will have been
                    // already invoked by the ...validate() call.
                    return;
                }
                storeProcedure();
            }
        });

        view.getCancel().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                        ? PresenterEnum.MEDICAL_HELP_CATEGORY_TYPES
                        : PresenterEnum.SPECIAL_MESSAGES, null);
            }
        });

        // VALIDATION
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        ebusUnitSelection.removeHandler();
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        view.getName().setFocus(true);

        if (null != parameter && parameter instanceof Procedure) {
            final Procedure proc = (Procedure) parameter;
            view.getProcedureId().setValue(proc.getId().toString());
            view.getName().setText(proc.getName());
            view.getMessageText().setText(proc.getMessageText());
            if (type == Event.Type.IN_CALENDAR) {
                view.getTime().setText("" + proc.getTime());
                view.getColor().setText(proc.getColor());
                // color
                view.getColor().getElement().getStyle().setProperty("backgroundColor", "#" + proc.getColor());
            }
        } else {
            // Using a null as argument on IE7 will lead to the setting of
            // string "null" as value, therefore the empty string is used instead.
            view.getProcedureId().setValue("");
            if (type == Event.Type.IN_CALENDAR) {
                // color
                view.getColor().getElement().getStyle().setProperty("backgroundColor", "#FFFFFF");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // Using a null as argument on IE7 will lead to the setting of
        // string "null" as value, therefore the empty string is used instead.
        view.getProcedureId().setValue("");
        view.getName().setText("");
        view.getMessageText().setText("");
        if (type == Event.Type.IN_CALENDAR) {
            view.getTime().setText("");
            view.getTime().removeStyleName("validationFailedBorder");
            view.getColor().setText("");
            view.getColor().getElement().getStyle().setProperty("backgroundColor", "#FFFFFF");
        }
        // validation
        validator.reset((String[]) null);
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Sets up validation for the presenter.
     */
    private void setupValidation() {
        // validation messages
        ValidationMessages vmess = new ValidationMessages() {
            @Override
            public String getCustomMessage(final String key, final Object... parameters) {
                return getValidationMessage(key, parameters);
            }
            @Override
            public String getPropertyName(final String propertyName) {
                String result;
                if ("name".equals(propertyName)) {
                    result = CONSTANTS.name();
                } else if ("smtText".equals(propertyName)) {
                    result = CONSTANTS.smsText();
                } else if ("time".equals(propertyName)) {
                    result = CONSTANTS.time();
                } else if ("color".equals(propertyName)) {
                    result = CONSTANTS.color();
                } else { result = super.getPropertyName(propertyName); }
                return result;
            }
        };
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        final FocusAction focusAction = new FocusAction();

        validator.addValidators("name",
                new EmptyValidator(view.getName())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("smtText",
                new EmptyValidator(view.getMessageText())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        if (type == Event.Type.IN_CALENDAR) {
            validator.addValidators("time",
                    new LongValidator(view.getTime(), false, "notNumber")
                        .addActionForFailure(focusAction)
                        .addActionForFailure(new StyleAction("validationFailedBorder")));
            validator.addValidators("color",
                    new EmptyValidator(view.getColor())
                        .addActionForFailure(focusAction)
                        .addActionForFailure(new StyleAction("validationFailedBorder")));
        }
    }

    /**
     * Creates a new patient.
     */
    private void storeProcedure() {
        final Procedure proc = new Procedure();
        if (null == view.getProcedureId().getValue()
            || view.getProcedureId().getValue().trim().isEmpty()) {
            proc.setId(null);
        } else {
            proc.setId(view.getProcedureId().getValue());
        }

        proc.setUnit(App.get().getSelectedUnit());
        proc.setName(view.getName().getText());
        proc.setMessageText(view.getMessageText().getText());
        proc.setType(type.toString());
        if (type == Event.Type.IN_CALENDAR) {
            proc.setTime(Integer.parseInt(view.getTime().getText()));
            proc.setColor(view.getColor().getText());
        }

        final Map<String, String> params = new HashMap<String, String>();
        params.put("unitId", proc.getUnit().getId().toString());
        params.put("name", proc.getName());
        params.put("messageText", proc.getMessageText());
        params.put("type", "" + proc.getType());
        if (type == Event.Type.IN_CALENDAR) {
            params.put("time", "" + proc.getTime());
            params.put("color", proc.getColor());
        }
        if (null != proc.getId()) { params.put("id", proc.getId().toString()); }

        final RestHandler rest = new RestHandler("/rest/unit/procedure/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                fireEvents(proc, jsonText);

                App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                        ? PresenterEnum.MEDICAL_HELP_CATEGORY_TYPES
                        : PresenterEnum.SPECIAL_MESSAGES, null);
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });

        rest.post(params);
    }

    /**
     *
     * @param updated updated procedure
     * @param jsonText server response
     */
    private void fireEvents(final Procedure updated, final String jsonText) {
        if (null == updated.getId()) {
            final Procedure created =
                    App.get().getJsonDeserializer().deserialize(Procedure.class, "procedure", jsonText);
            eventBus.fireEvent(new CrudEvent(OperationType.CREATE, created));
            Window.alert(type == Event.Type.IN_CALENDAR
                    ? CONSTANTS.procedureAdded()
                    : CONSTANTS.specialSmsAdded());
        } else {
            eventBus.fireEvent(new CrudEvent(OperationType.UPDATE, updated));
            Window.alert(type == Event.Type.IN_CALENDAR
                    ? CONSTANTS.procedureUpdated()
                    : CONSTANTS.specialSmsUpdated());
        }
    }
}
