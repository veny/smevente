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
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.shared.EntityTypeEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
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
 * Add Patient presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StoreMedicalHelpCategoryPresenter
    extends AbstractPresenter<StoreMedicalHelpCategoryPresenter.StoreMedicalHelpCategoryView>
    implements HeaderHandler {

    /**
     * View interface for the Add Medical help category.
     *
     * @author Vaclav Sykora
     * @since 22.8.2010
     */
    public interface StoreMedicalHelpCategoryView extends View {
        /**
         * Getter for the name text field.
         * @return the input field for the name
         */
        TextBox getName();
        /**
         * Getter for the Sms text field.
         * @return the input field for the Sms text
         */
        TextArea getSmsText();
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
         * @return the hidden field wit category ID
         */
        Hidden getMedicalHelpCategoryId();
    }

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    /**
     * Type of category to be created or updated.
     */
    private Event.Type type = Event.Type.IN_CALENDAR;

    /**
     *
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
                    DOM.setStyleAttribute(selectedColor.getElement(), "backgroundColor", "#" + selectedColor.getText());
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
     * @param type the type of category
     */
    public StoreMedicalHelpCategoryPresenter(final Event.Type type) {
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
                        // new category is being created, so the color
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
                storeMedicalHelpCategory();
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
            final Procedure mhc = (Procedure) parameter;
            view.getMedicalHelpCategoryId().setValue(mhc.getId().toString());
            view.getName().setText(mhc.getName());
            view.getSmsText().setText(mhc.getMessageText());
            if (type == Event.Type.IN_CALENDAR) {
                view.getTime().setText("" + mhc.getTime());
                view.getColor().setText(mhc.getColor());
                // color
                DOM.setStyleAttribute(view.getColor().getElement(), "backgroundColor", "#" + mhc.getColor());
            }
        } else {
            // Using a null as argument on IE7 will lead to the setting of
            // string "null" as value, therefore the empty string is used instead.
            view.getMedicalHelpCategoryId().setValue("");
            if (type == Event.Type.IN_CALENDAR) {
                // color
                DOM.setStyleAttribute(view.getColor().getElement(), "backgroundColor", "#FFFFFF");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // Using a null as argument on IE7 will lead to the setting of
        // string "null" as value, therefore the empty string is used instead.
        view.getMedicalHelpCategoryId().setValue("");
        view.getName().setText("");
        view.getSmsText().setText("");
        if (type == Event.Type.IN_CALENDAR) {
            view.getTime().setText("");
            view.getTime().removeStyleName("validationFailedBorder");
            view.getColor().setText("");
            DOM.setStyleAttribute(view.getColor().getElement(), "backgroundColor", "#FFFFFF");
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
                new EmptyValidator(view.getSmsText())
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
    private void storeMedicalHelpCategory() {
        final Procedure mhc = new Procedure();
        if (null == view.getMedicalHelpCategoryId().getValue()
            || view.getMedicalHelpCategoryId().getValue().trim().isEmpty()) {
            mhc.setId(null);
        } else {
            mhc.setId(Long.parseLong(view.getMedicalHelpCategoryId().getValue()));
        }

        mhc.setUnit(App.get().getSelectedUnit());
        mhc.setName(view.getName().getText());
        mhc.setMessageText(view.getSmsText().getText());
        mhc.setType(type.toString());
        if (type == Event.Type.IN_CALENDAR) {
            mhc.setTime(Long.parseLong(view.getTime().getText()));
            mhc.setColor(view.getColor().getText());
        }

        final Map<String, String> params = new HashMap<String, String>();
        params.put("unitId", mhc.getUnit().getId().toString());
        params.put("name", mhc.getName());
        params.put("smsText", mhc.getMessageText());
        params.put("type", "" + mhc.getType());
        if (type == Event.Type.IN_CALENDAR) {
            params.put("time", "" + mhc.getTime());
            params.put("color", mhc.getColor());
        }
        if (null != mhc.getId()) { params.put("id", mhc.getId().toString()); }

        final RestHandler rest = new RestHandler("/rest/unit/mhc/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                fireEvents(mhc, jsonText);

                App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                        ? PresenterEnum.MEDICAL_HELP_CATEGORY_TYPES
                        : PresenterEnum.SPECIAL_MESSAGES, null);
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });

        if (null == mhc.getId()) {
            rest.post(params);
        } else {
            rest.put(params);
        }
    }

    /**
     *
     * @param mhc the created/updated category
     * @param jsonText server response
     */
    private void fireEvents(final Procedure mhc, final String jsonText) {
        final int textType = App.get().getSelectedUnitTextVariant();
        if (null == mhc.getId()) {
            final Procedure medicalHelpCategory = App.get().getJsonDeserializer().deserialize(
                    Procedure.class, "medicalHelpCategory", jsonText);
            eventBus.fireEvent(new CrudEvent(EntityTypeEnum.MHC, OperationType.CREATE, medicalHelpCategory));
            Window.alert(type == Event.Type.IN_CALENDAR
                    ? CONSTANTS.medicalHelpAdded()[textType]
                    : CONSTANTS.specialSmsAdded());
        } else {
            eventBus.fireEvent(new CrudEvent(EntityTypeEnum.MHC, OperationType.UPDATE, mhc));
            Window.alert(type == Event.Type.IN_CALENDAR
                    ? CONSTANTS.medicalHelpUpdated()[textType]
                    : CONSTANTS.specialSmsUpdated());
        }
    }
}
