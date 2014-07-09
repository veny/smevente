package veny.smevente.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.client.utils.DateUtils;
import veny.smevente.client.utils.Pair;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import eu.maydu.gwt.validation.client.InvalidValueSerializable;
import eu.maydu.gwt.validation.client.ValidationException;

/**
 * JSON deserializer to deserialize the JSON texts to DTOs.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public class JsonDeserializer {

    /**
     * Deserializes a JSON data representation to an object of given class.
     * @param <T> class type of returned object
     * @param classToCreate class of created object
     * @param variableName master key in JSON message
     * @param representation JSON string
     * @return created object of given type
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(final Class<T> classToCreate, final String variableName, final String representation) {
        JSONValue jsonValue = JSONParser.parseStrict(representation);
        JSONObject jsObj;

        jsObj = jsonValue.isObject();
        if (jsObj == null) {
            throw new IllegalArgumentException("not object variableName: " + variableName
                + ", value: " + representation);
        }

        JSONValue jsonObjValue = jsObj.get(variableName);
        if (jsonObjValue == null) {
            throw new IllegalArgumentException("invalid object variableName: " + variableName
                + ", value: " + representation);
        }

        jsObj = jsonObjValue.isObject();
        if (jsObj == null) {
            throw new IllegalArgumentException("invalid object variableName: " + variableName
                + ", value: " + representation);
        }

        T result;

        if (ExceptionJsonWrapper.class == classToCreate) {
            result = (T) fromJsonExceptionWrapper(jsObj);
        } else if (Unit.class == classToCreate) {
            result = (T) unitFromJson(jsObj);
        } else if (User.class == classToCreate) {
            result = (T) userFromJson(jsObj);
        } else if (Customer.class == classToCreate) {
            result = (T) customerFromJson(jsObj);
        } else if (Procedure.class == classToCreate) {
            result = (T) procedureFromJson(jsObj);
        } else if (Event.class == classToCreate) {
            result = (T) eventFromJson(jsObj);
        } else {
            throw new IllegalArgumentException("no deserialization, class=" + classToCreate);
        }

        return result;
    }

    /**
     * Deserializes a JSON data representation to a list of object of given class.
     * @param <T> class type of returned objects in list
     * @param classToCreate class of created objects in list
     * @param variableName master key in JSON message
     * @param representation JSON string
     * @return list of objects of given type
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> deserializeList(
        final Class<T> classToCreate, final String variableName, final String representation) {

        JSONValue jsonValue = JSONParser.parseStrict(representation);

        JSONObject jsObj = jsonValue.isObject();
        if (jsObj == null) {
            throw new IllegalArgumentException(
                "not object variableName: " + variableName + " value: " + representation);
        }

        JSONArray jsArr = jsObj.get(variableName).isArray();
        if (jsArr == null) {
            throw new IllegalArgumentException(
                "invalid array variableName: " + variableName + " value: " + representation);
        }

        List<T> rslt = null;
        if (Unit.class == classToCreate) {
            rslt = (List<T>) unitListFromJson(jsArr);
        } else if (User.class == classToCreate) {
            rslt = (List<T>) userListFromJson(jsArr);
        } else if (Membership.class == classToCreate) {
            rslt = (List<T>) membershipListFromJson(jsArr);
        } else if (Customer.class == classToCreate) {
            rslt = (List<T>) customerListFromJson(jsArr);
        } else if (Procedure.class == classToCreate) {
            rslt = (List<T>) procedureListFromJson(jsArr);
        } else if (Event.class == classToCreate) {
            rslt = (List<T>) eventListFromJson(jsArr);
        } else {
            throw new IllegalArgumentException("no list deserialization, class=" + classToCreate);
        }

        return rslt;
    }

    /**
     * Creates a string from given JSON.
     * @param variableName name of a variable representing a string
     * @param representation JSON string
     * @return parsed string
     */
    public String createString(final String variableName, final String representation) {
        final JSONValue jsonValue = JSONParser.parseStrict(representation);
        final JSONObject jsObj = jsonValue.isObject();

        if (null == jsObj) {
            throw new IllegalArgumentException("not object variableName: " + variableName
                + " value: " + representation);
        }

        final JSONValue jsonStringValue = jsObj.get(variableName);
        if (null == jsonStringValue) {
            throw new IllegalArgumentException("invalid object variableName: " + variableName
                + " value: " + representation);
        }

        final JSONString string = jsonStringValue.isString();
        if (null == string) {
            throw new IllegalArgumentException("invalid object value variableName: " + variableName
                + " value: " + representation);
        }

        return string.stringValue();
    }

    /**
     * Creates a number from given JSON.
     * @param variableName name of a variable representing a number
     * @param representation JSON string
     * @return parsed number
     */
    public double createNumber(final String variableName, final String representation) {
        JSONValue jsonValue = JSONParser.parseStrict(representation);

        JSONObject jsObj = jsonValue.isObject();
        if (jsObj == null) {
            throw new IllegalArgumentException("not object variableName: " + variableName
                + " value: " + representation);
        }

        JSONValue jsonNumberValue = jsObj.get(variableName);

        if (jsonNumberValue == null) {
            throw new IllegalArgumentException("invalid object variableName: " + variableName
                + " value: " + representation);
        }

        JSONNumber number = jsonNumberValue.isNumber();

        if (number == null) {
            throw new IllegalArgumentException("invalid object value variableName: " + variableName
                + " value: " + representation);
        }

        return number.doubleValue();
    }

    // ------------------------------------------------------ Assistant Methods


    /**
     * @param jsObj JSON object to deserialize
     * @return <code>Unit</code> object
     */
    private Unit unitFromJson(final JSONObject jsObj) {
        final Unit rslt = new Unit();
        rslt.setId(jsObj.get("id").isString().stringValue());
        rslt.setName(jsObj.get("name").isString().stringValue());
        rslt.setLimitedSmss(getLong(jsObj.get("limitedSmss")));
//XXX        rslt.addMetadata(Unit.UNIT_TYPE, getString(jsObj.get("type")));
        return rslt;
    }
    /**
     * @param jsArr JSON array to deserialize
     * @return list of <code>Unit</code> objects
     */
    private List<Unit> unitListFromJson(final JSONArray jsArr) {
        final List<Unit> rslt = new ArrayList<Unit>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(unitFromJson(jsObj));
        }
        return rslt;
    }

    /**
     * @param jsObj JSON object to deserialize
     * @return <code>MembershipDto</code> object
     */
    private Membership membershipFromJson(final JSONObject jsObj) {
        final Membership rslt = new Membership();
        rslt.setId(jsObj.get("id").isString().stringValue());
        rslt.setRole(jsObj.get("role").isString().stringValue());
        rslt.setSignificance((int) jsObj.get("significance").isNumber().doubleValue());
        // -> user
        final JSONObject jsUserObj = jsObj.get("user").isObject();
        if (null != jsUserObj) {
            rslt.setUser(userFromJson(jsUserObj));
        }
        // -> unit
        final JSONObject jsUnitObj = jsObj.get("unit").isObject();
        if (null != jsUnitObj) {
            rslt.setUnit(unitFromJson(jsUnitObj));
        }
        return rslt;
    }
    /**
     * @param jsArr JSON array to deserialize
     * @return list of <code>Unit</code> objects
     */
    private List<Membership> membershipListFromJson(final JSONArray jsArr) {
        final List<Membership> rslt = new ArrayList<Membership>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(membershipFromJson(jsObj));
        }
        return rslt;
    }

    /**
     * @param jsObj JSON object to deserialize
     * @return <code>User</code> object
     */
    private User userFromJson(final JSONObject jsObj) {
        final User rslt = new User();
        rslt.setId(jsObj.get("id").isString().stringValue());
        rslt.setUsername(jsObj.get("username").isString().stringValue());
        rslt.setFullname(jsObj.get("fullname").isString().stringValue());
        return rslt;
    }
    /**
     * @param jsArr JSON array to deserialize
     * @return list of <code>User</code> objects
     */
    private List<User> userListFromJson(final JSONArray jsArr) {
        final List<User> rslt = new ArrayList<User>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(userFromJson(jsObj));
        }
        return rslt;
    }

    /**
     * @param jsObj JSON object to deserialize
     * @return <code>Customer</code> object
     */
    private Customer customerFromJson(final JSONObject jsObj) {
        final Customer rslt = new Customer();
        rslt.setId(getString(jsObj.get("id")));
        final JSONObject jsUnitObj = jsObj.get("unit").isObject();
        if (null != jsUnitObj) {
            rslt.setUnit(unitFromJson(jsUnitObj));
        }
        rslt.setFirstname(getString(jsObj.get("firstname")));
        rslt.setSurname(getString(jsObj.get("surname")));
        rslt.setAsciiFullname(getString(jsObj.get("asciiFullname")));
        rslt.setPhoneNumber(getString(jsObj.get("phoneNumber")));
        rslt.setEmail(getString(jsObj.get("email")));
        rslt.setBirthNumber(getString(jsObj.get("birthNumber")));
        rslt.setDegree(getString(jsObj.get("degree")));
        rslt.setStreet(getString(jsObj.get("street")));
        rslt.setCity(getString(jsObj.get("city")));
        rslt.setZipCode(getString(jsObj.get("zipCode")));
        rslt.setEmployer(getString(jsObj.get("employer")));
        rslt.setCareers(getString(jsObj.get("careers")));
        return rslt;
    }
    /**
     * @param jsArr JSON array to deserialize
     * @return list of <code>Customer</code> objects
     */
    private List<Customer> customerListFromJson(final JSONArray jsArr) {
        final List<Customer> rslt = new ArrayList<Customer>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(customerFromJson(jsObj));
        }
        return rslt;
    }

    /**
     * Gets <code>Procedure</code> from JSON.
     * @param jsObj JSON object
     * @return instance of <code>Procedure</code>
     */
    private Procedure procedureFromJson(final JSONObject jsObj) {
        final Procedure rslt = new Procedure();
        rslt.setId(jsObj.get("id").isString().stringValue());
        rslt.setName(jsObj.get("name").isString().stringValue());
        rslt.setMessageText(jsObj.get("messageText").isString().stringValue());
        rslt.setType(jsObj.get("type").isString().stringValue());
        if (rslt.enumType() == Event.Type.IN_CALENDAR) {
            rslt.setColor(jsObj.get("color").isString().stringValue());
            rslt.setTime((int) jsObj.get("time").isNumber().doubleValue());
        }
        return rslt;
    }
    /**
     * Gets list of <code>MedicalHelpCategory</code> from JSON.
     * @param jsArr JSON array
     * @return list of <code>MedicalHelpCategory</code>
     */
    private List<Procedure> procedureListFromJson(final JSONArray jsArr) {
        final List<Procedure> rslt = new ArrayList<Procedure>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(procedureFromJson(jsObj));
        }
        return rslt;
    }


    /**
     * Gets <code>Event</code> from JSON.
     * @param jsObj JSON object
     * @return instance of <code>Event</code>
     */
    private Event eventFromJson(final JSONObject jsObj) {
        final Event rslt = new Event();
        rslt.setId(jsObj.get("id").isString().stringValue());
        final JSONObject jsUserObj = jsObj.get("author").isObject();
        if (null != jsUserObj) {
            rslt.setAuthor(userFromJson(jsUserObj));
        }
        final JSONObject jsCustomerObj = jsObj.get("customer").isObject();
        if (null != jsCustomerObj) {
            rslt.setCustomer(customerFromJson(jsCustomerObj));
        }
        final JSONObject jsStObj = jsObj.get("procedure").isObject();
        if (null != jsStObj) {
            rslt.setProcedure(procedureFromJson(jsStObj));
        }
        rslt.setStartTime(getDate(jsObj.get("startTime"), true));
        rslt.setLength((int) jsObj.get("length").isNumber().doubleValue());
        rslt.setSent(getDate(jsObj.get("sent"), false));
        rslt.setText(jsObj.get("text").isString().stringValue());
        rslt.setNotice(getString(jsObj.get("notice")));
        final Long sendAttemptCount = getLong(jsObj.get("sendAttemptCount"));
        rslt.setSendAttemptCount(null == sendAttemptCount ? 0 : sendAttemptCount.intValue());
        return rslt;
    }
    /**
     * Gets list of <code>Event</code> from JSON.
     * @param jsArr JSON array
     * @return list of <code>Event</code>
     */
    private List<Event> eventListFromJson(final JSONArray jsArr) {
        final List<Event> rslt = new ArrayList<Event>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(eventFromJson(jsObj));
        }
        return rslt;
    }


    // ---------------------------------------------------------- Special Cases

    /**
     * Gets list of <code>Sms</code> from JSON.
     * @param representation JSON string
     * @return list of <code>Sms</code>
     */
    public List<Pair<User, Map<String, Long>>> smsStatisticsFromJson(final String representation) {
        final JSONValue jsonValue = JSONParser.parseStrict(representation);
        final JSONObject jsObj = jsonValue.isObject();
        if (jsObj == null) { throw new IllegalArgumentException("not JSON object: " + representation); }
        final JSONArray jsArr = jsObj.get("smsStatistics").isArray();
        if (jsArr == null) { throw new IllegalArgumentException("not JSON array: " + representation); }

        final List<Pair<User, Map<String, Long>>> rslt = new ArrayList<Pair<User, Map<String, Long>>>();
        for (int i = 0; i < jsArr.size(); i++) {
            final JSONValue jsonUser = jsArr.get(i).isObject().get("a");
            final User user = userFromJson(jsonUser.isObject());

            final JSONValue jsonStats = jsArr.get(i).isObject().get("b");
            final Map<String, Long> stats = new HashMap<String, Long>();
            stats.put(Event.SUM, getLong(jsonStats.isObject().get(Event.SUM)));
            stats.put(Event.SENT, getLong(jsonStats.isObject().get(Event.SENT)));
            stats.put(Event.FAILED, getLong(jsonStats.isObject().get(Event.FAILED)));
            stats.put(Event.DELETED, getLong(jsonStats.isObject().get(Event.DELETED)));

            rslt.add(new Pair<User, Map<String, Long>>(user, stats));
        }
        return rslt;
    }

    /**
     * Gets customer's history from JSON.
     * @param representation JSON string
     * @return pair composed by customer and list of his <code>Event</code>
     */
    public Pair<Customer, List<Event>> customerHistoryFomJson(final String representation) {
        final JSONValue jsonValue = JSONParser.parseStrict(representation);
        final JSONObject jsonRoot = jsonValue.isObject();
        if (null == jsonRoot) { throw new IllegalArgumentException("not JSON object: " + representation); }

        final JSONObject jsonPair = jsonRoot.get("history").isObject();
        if (null == jsonPair) { throw new IllegalArgumentException("not JSON object: history"); }
        final JSONObject jsonCustomer = jsonPair.get("a").isObject();
        if (null == jsonCustomer) { throw new IllegalArgumentException("not JSON object: history/a"); }
        final JSONArray jsonSmss = jsonPair.get("b").isArray();
        if (null == jsonSmss) { throw new IllegalArgumentException("not JSON array: history/b"); }

        final Customer customer = customerFromJson(jsonCustomer);
        final List<Event> smss = eventListFromJson(jsonSmss);
        return new Pair<Customer, List<Event>>(customer, smss);
    }

    /**
     * Converts JSON representation of an Exception into an instance of ExceptionJsonWrapper.
     * If ValidationException is found then it is set as cause.
     *
     * @param jsObj JSON representation of Exception
     * @return instance of ExceptionJsonWrapper
     */
    private ExceptionJsonWrapper fromJsonExceptionWrapper(final JSONObject jsObj) {
        final ExceptionJsonWrapper exWrapper = new ExceptionJsonWrapper();

        exWrapper.setMessage(getString(jsObj.get("message")));
        exWrapper.setClassName(getString(jsObj.get("className")));
        exWrapper.setId(getString(jsObj.get("id")));

        // validation exception
        final ValidationException ve = new ValidationException();
        exWrapper.setCause(ve);
        if (exWrapper.isValidation()) {
            JSONArray array = jsObj.get("invalidValues").isArray();
            if (null != array) {
                for (int i = 0; i < array.size(); i++) {
                    JSONValue invalidValue = array.get(i);
                    String invalidMessage = null;
                    String invalidPropertyName = null;
                    JSONObject invalidValueObj = invalidValue.isObject();
                    if (invalidValueObj != null) {
                        JSONValue invalidMessageValue = invalidValueObj.get("message");
                        if (invalidMessageValue != null) {
                            JSONString invalidMessageJS = invalidMessageValue.isString();
                            if (invalidMessageJS != null) {
                                invalidMessage = invalidMessageJS.stringValue();
                            }
                        }
                        JSONValue invalidPropertyNameValue = invalidValueObj.get("propertyName");
                        if (invalidPropertyNameValue != null) {
                            JSONString invalidPropertyNameJS = invalidPropertyNameValue.isString();
                            if (invalidPropertyNameJS != null) {
                                invalidPropertyName = invalidPropertyNameJS.stringValue();
                            }
                        }
                    }

                    ve.getInvalidValues().add(new InvalidValueSerializable(invalidMessage, invalidPropertyName));
                }
            }
        }

        return exWrapper;
    }

    // --------------------------------------------------------- Helper Methods

    /**
     * Gets <code>String</code> from JSON object.
     * @param jsonValue JSON value
     * @return JSON value converted to <code>String</code> or <i>null</i> if value is null
     */
    private String getString(final JSONValue jsonValue) {
        if (null == jsonValue || JSONNull.getInstance().equals(jsonValue)) {
            return null;
        }
        JSONString jsonString = jsonValue.isString();
        return (null == jsonString ? null : jsonString.stringValue());
    }

    /**
     * Gets <code>Date</code> from JSON object.
     * @param jsonValue JSON value
     * @return JSON value converted to <code>Date</code> or <i>null</i> if value is null
     */
    private Long getLong(final JSONValue jsonValue) {
        if (null == jsonValue || JSONNull.getInstance().equals(jsonValue)) {
            return null;
        }
        return new Long((long) jsonValue.isNumber().doubleValue());
    }

    /**
     * Gets <code>Date</code> from JSON object.
     * @param jsonValue JSON value
     * @param mandatory whether the <code>jsonValue</code> must be presented
     * @return JSON value converted to <code>Date</code> or <i>null</i> if value is null
     */
    private Date getDate(final JSONValue jsonValue, final boolean mandatory) {
        if (null == jsonValue || JSONNull.getInstance().equals(jsonValue)) {
            if (mandatory) {
                throw new NullPointerException("mandatory date cannot be null");
            } else {
                return null;
            }
        }
        final Date d = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601).parse(
                jsonValue.isString().stringValue());
        return DateUtils.toUTC(d);
    }

}
