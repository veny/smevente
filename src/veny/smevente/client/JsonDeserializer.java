package veny.smevente.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.MedicalHelpCategory;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
import veny.smevente.model.SmsDto;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.shared.ExceptionJsonWrapper;

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
        } else if (Patient.class == classToCreate) {
            result = (T) patientFromJson(jsObj);
        } else if (MedicalHelpCategory.class == classToCreate) {
            result = (T) mhcFromJson(jsObj);
        } else if (SmsDto.class == classToCreate) {
            result = (T) smsFromJson(jsObj);
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
        } else if (Patient.class == classToCreate) {
            rslt = (List<T>) patientListFromJson(jsArr);
        } else if (MedicalHelpCategory.class == classToCreate) {
            rslt = (List<T>) mhcListFromJson(jsArr);
        } else if (SmsDto.class == classToCreate) {
            rslt = (List<T>) smsListFromJson(jsArr);
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
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
        rslt.setName(jsObj.get("name").isString().stringValue());
        rslt.setLimitedSmss(getLong(jsObj.get("limitedSmss")));
        rslt.addMetadata(Unit.UNIT_TYPE, getString(jsObj.get("type")));
        // members
        final JSONArray jsArr = jsObj.get("members").isArray();
        if (null != jsArr) {
            rslt.setMembers(membershipListFromJson(jsArr));
        }
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
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
        rslt.setType(Membership.Type.valueOf(jsObj.get("type").isString().stringValue()));
        rslt.setSignificance((int) jsObj.get("significance").isNumber().doubleValue());
        final JSONObject jsUserObj = jsObj.get("user").isObject();
        if (null != jsUserObj) {
            rslt.setUser(userFromJson(jsUserObj));
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
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
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
     * @return <code>Patient</code> object
     */
    private Patient patientFromJson(final JSONObject jsObj) {
        final Patient rslt = new Patient();
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
        final JSONObject jsUnitObj = jsObj.get("unit").isObject();
        if (null != jsUnitObj) {
            rslt.setUnit(unitFromJson(jsUnitObj));
        }
        rslt.setFirstname(getString(jsObj.get("firstname")));
        rslt.setSurname(getString(jsObj.get("surname")));
        rslt.setPhoneNumber(getString(jsObj.get("phoneNumber")));
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
     * @return list of <code>Patient</code> objects
     */
    private List<Patient> patientListFromJson(final JSONArray jsArr) {
        final List<Patient> rslt = new ArrayList<Patient>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(patientFromJson(jsObj));
        }
        return rslt;
    }

    /**
     * Gets <code>MedicalHelpCategory</code> from JSON.
     * @param jsObj JSON object
     * @return instance of <code>MedicalHelpCategory</code>
     */
    private MedicalHelpCategory mhcFromJson(final JSONObject jsObj) {
        final MedicalHelpCategory rslt = new MedicalHelpCategory();
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
        rslt.setName(jsObj.get("name").isString().stringValue());
        rslt.setSmsText(jsObj.get("smsText").isString().stringValue());
        JSONValue type = jsObj.get("type");
        if (type == null || type.isNumber() == null) {
            rslt.setType(MedicalHelpCategory.TYPE_STANDARD);
        } else {
            rslt.setType((short) type.isNumber().doubleValue());
        }
        if (rslt.getType() == MedicalHelpCategory.TYPE_STANDARD) {
            rslt.setColor(jsObj.get("color").isString().stringValue());
            rslt.setTime((long) jsObj.get("time").isNumber().doubleValue());
        }
        return rslt;
    }
    /**
     * Gets list of <code>MedicalHelpCategory</code> from JSON.
     * @param jsArr JSON array
     * @return list of <code>MedicalHelpCategory</code>
     */
    private List<MedicalHelpCategory> mhcListFromJson(final JSONArray jsArr) {
        final List<MedicalHelpCategory> rslt = new ArrayList<MedicalHelpCategory>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(mhcFromJson(jsObj));
        }
        return rslt;
    }


    /**
     * Gets <code>Sms</code> from JSON.
     * @param jsObj JSON object
     * @return instance of <code>Sms</code>
     */
    private SmsDto smsFromJson(final JSONObject jsObj) {
        final SmsDto rslt = new SmsDto();
        rslt.setId((long) jsObj.get("id").isNumber().doubleValue());
        final JSONObject jsUserObj = jsObj.get("author").isObject();
        if (null != jsUserObj) {
            rslt.setAuthor(userFromJson(jsUserObj));
        }
        final JSONObject jsPatientObj = jsObj.get("patient").isObject();
        if (null != jsPatientObj) {
            rslt.setPatient(patientFromJson(jsPatientObj));
        }
        final JSONObject jsStObj = jsObj.get("medicalHelpCategory").isObject();
        if (null != jsStObj) {
            rslt.setMedicalHelpCategory(mhcFromJson(jsStObj));
        }
        rslt.setMedicalHelpStartTime(new Date((long) jsObj.get("medicalHelpStartTime").isNumber().doubleValue()));
        rslt.setMedicalHelpLength((int) jsObj.get("medicalHelpLength").isNumber().doubleValue());
        rslt.setSent(getDate(jsObj.get("sent")));
        rslt.setText(jsObj.get("text").isString().stringValue());
        rslt.setNotice(getString(jsObj.get("notice")));
        final Long sendAttemptCount = getLong(jsObj.get("sendAttemptCount"));
        rslt.setSendAttemptCount(null == sendAttemptCount ? 0 : sendAttemptCount.intValue());
        return rslt;
    }
    /**
     * Gets list of <code>Sms</code> from JSON.
     * @param jsArr JSON array
     * @return list of <code>Sms</code>
     */
    private List<SmsDto> smsListFromJson(final JSONArray jsArr) {
        final List<SmsDto> rslt = new ArrayList<SmsDto>();

        for (int i = 0; i < jsArr.size(); i++) {
            JSONValue jsonValue = jsArr.get(i);
            JSONObject jsObj = jsonValue.isObject();
            if (jsObj == null) {
                throw new IllegalStateException("not an JSON object: " + jsonValue);
            }
            rslt.add(smsFromJson(jsObj));
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
            stats.put(SmsDto.SUM, getLong(jsonStats.isObject().get(SmsDto.SUM)));
            stats.put(SmsDto.SENT, getLong(jsonStats.isObject().get(SmsDto.SENT)));
            stats.put(SmsDto.FAILED, getLong(jsonStats.isObject().get(SmsDto.FAILED)));
            stats.put(SmsDto.DELETED, getLong(jsonStats.isObject().get(SmsDto.DELETED)));

            rslt.add(new Pair<User, Map<String, Long>>(user, stats));
        }
        return rslt;
    }

    /**
     * Gets patient's history from JSON.
     * @param representation JSON string
     * @return pair composed by patient and list of his <code>Sms</code>
     */
    public Pair<Patient, List<SmsDto>> patientHistoryFomJson(final String representation) {
        final JSONValue jsonValue = JSONParser.parseStrict(representation);
        final JSONObject jsonRoot = jsonValue.isObject();
        if (null == jsonRoot) { throw new IllegalArgumentException("not JSON object: " + representation); }

        final JSONObject jsonPair = jsonRoot.get("history").isObject();
        if (null == jsonPair) { throw new IllegalArgumentException("not JSON object: history"); }
        final JSONObject jsonPatient = jsonPair.get("a").isObject();
        if (null == jsonPatient) { throw new IllegalArgumentException("not JSON object: history/a"); }
        final JSONArray jsonSmss = jsonPair.get("b").isArray();
        if (null == jsonSmss) { throw new IllegalArgumentException("not JSON array: history/b"); }

        final Patient patient = patientFromJson(jsonPatient);
        final List<SmsDto> smss = smsListFromJson(jsonSmss);
        return new Pair<Patient, List<SmsDto>>(patient, smss);
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
     * @return JSON value converted to <code>Date</code> or <i>null</i> if value is null
     */
    private Date getDate(final JSONValue jsonValue) {
        if (null == jsonValue || JSONNull.getInstance().equals(jsonValue)) {
            return null;
        }
        return new Date((long) jsonValue.isNumber().doubleValue());
    }

}
