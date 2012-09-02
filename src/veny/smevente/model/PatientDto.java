package veny.smevente.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * DTO entity representing the Patient.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public class PatientDto implements Serializable {

    /** Generated (1110303) serial version UID. */
    private static final long serialVersionUID = 502793953437559774L;

    /** Primary Key. */
    private Long id;

    /** Unit which is master of this. */
    private Unit unit;
    /** Patient's first name. */
    private String firstname;
    /** Patient's surname. */
    private String surname;
    /** Patient's phone number. */
    private String phoneNumber;
    /** Patient's birth date. */
    private String birthNumber;
    /** Patient's degree. */
    private String degree;
    /** Patient's degree. */
    private String street;
    /** Patient's degree. */
    private String city;
    /** Patient's degree. */
    private String zipCode;
    /** Patient's degree. */
    private String employer;
    /** Patient's degree. */
    private String careers;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getBirthNumber() {
        return birthNumber;
    }
    public void setBirthNumber(String birthNumber) {
        this.birthNumber = birthNumber;
    }
    public String getDegree() {
        return degree;
    }
    public void setDegree(String degree) {
        this.degree = degree;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getEmployer() {
        return employer;
    }
    public void setEmployer(String employer) {
        this.employer = employer;
    }
    public String getCareers() {
        return careers;
    }
    public void setCareers(String careers) {
        this.careers = careers;
    }
    // CHECKSTYLE:ON

    /**
     * Gets patient's fullname.
     * @return fullname
     */
    @JsonIgnore
    public String getFullname() {
        return new StringBuilder(getFirstname())
            .append(' ')
            .append(getSurname())
            .toString();
    }

    /**
     * Gets formatted birth number.
     * @return formatted birth number
     */
    @JsonIgnore
    public String getFormattedBirthNumber() {
        if (null != birthNumber && birthNumber.length() > 6) {
            return birthNumber.substring(0, 6) + "/" + birthNumber.substring(6);
        }
        return birthNumber;
    }

    // ---------------------------------------------------- Convenience Methods

    /**
     * Sets unit ID to a new unit object.
     * @param unitId unit ID
     */
    public void setUnitId(final Long unitId) {
        if (null == unitId || unitId.longValue() <= 0) {
            throw new IllegalArgumentException("invalid unit ID (null or less than 0)");
        }
        setUnit(new Unit());
        getUnit().setId(unitId);
    }

    // ----------------------------------------------------------- Object Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("Patient(id=")
            .append(getId())
            .append(", unit=")
            .append(null == unit ? "null" : unit.getName())
            .append(", firstname='")
            .append(firstname)
            .append("', surname='")
            .append(surname)
            .append("', phoneNumber='")
            .append(phoneNumber)
            .append("', birthNumber='")
            .append(birthNumber)
            .append("')")
            .toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (!(other instanceof PatientDto)) { return false; }

        return id.equals(((PatientDto) other).getId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.intValue();
    }

}
