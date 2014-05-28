package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

import veny.smevente.client.utils.ClientTextUtils;
import veny.smevente.misc.SoftDelete;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orientechnologies.orient.core.annotation.OBeforeSerialization;

/**
 * Entity class representing the Patient.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
@SoftDelete
public class Patient extends AbstractEntity {

    /** Unit which is master of this. */
    @ManyToOne
    private Unit unit;
    /** Patient's first name. */
    @Column
    private String firstname;
    /** Patient's surname. */
    @Column
    private String surname;
    /** Patient's phone number. */
    @Column
    private String phoneNumber;
    /** Patient's birth number. */
    @Column
    private String birthNumber;
    /** Patient's degree. */
    @Column
    private String degree;
    /** Patient's degree. */
    @Column
    private String street;
    /** Patient's degree. */
    @Column
    private String city;
    /** Patient's degree. */
    @Column
    private String zipCode;
    /** Patient's degree. */
    @Column
    private String employer;
    /** Patient's degree. */
    @Column
    private String careers;
    /** Fullname as ASCII for search. */
    private String asciiFullname;

    // CHECKSTYLE:OFF
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
    public String getAsciiFullname() {
        return asciiFullname;
    }
    // CHECKSTYLE:ON

    /**
     * Gets patient's fullname.
     * @return fullname
     */
    @JsonIgnore
    public String fullname() {
        return new StringBuilder(getFirstname())
            .append(' ')
            .append(getSurname())
            .toString();
    }

    /**
     * Converts firstname+surname to ASCII to be searchable without national characters like 'á'.
     */
    @JsonIgnore
    @OBeforeSerialization
    public void asciiFullname() {
        this.asciiFullname = ClientTextUtils.convert2ascii(fullname()).toUpperCase();
    }

    /**
     * Gets formatted birth number.
     * @return formatted birth number
     */
    @JsonIgnore
    public String formattedBirthNumber() {
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
    public void setUnitId(final Object unitId) {
        if (null == unitId) { throw new NullPointerException("unit ID is null"); }
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
        if (!(other instanceof Patient)) { return false; }

        return getId().equals(((Patient) other).getId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}
