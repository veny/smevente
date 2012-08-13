package veny.smevente.model.gae;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.beans.BeanUtils;

import veny.smevente.client.utils.ClientTextUtils;
import veny.smevente.misc.SoftDelete;
import veny.smevente.model.PatientDto;

/**
 * GAE entity representing the Patient.
 *
 * <p/>
 * Schema versions:<ul>
 * <li>{@link http://blog.burnayev.com/2010/02/gae-developer-tip-updating-database.html}
 * <li>0 - id,unitId,firstname,surname,phoneNumber,birthNumber,degree,street,
 *         city,zipCode,employer,careers,status,upperFirstname,upperSurname
 * <li>1 - deleted
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
@SuppressWarnings("serial")
@Entity
@SoftDelete
public class Patient implements Serializable {

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unit which is master of this. */
    private Long unitId;
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
    /** Patient's street. */
    private String street;
    /** Patient's city. */
    private String city;
    /** Patient's zip code. */
    private String zipCode;
    /** Patient's degree. */
    private String employer;
    /** Patient's careers. */
    private String careers;
    /** Flag whether the object is deleted. */
    private Boolean deleted = Boolean.FALSE;
    /**
     * Patient's status.
     * TODO [veny,A] remove it after deploy on production
     */
    @Deprecated
    private Integer status;
    /** Infrastructure schema version. */
    private Integer version = 1; // keep it always up to date (see class JavaDoc)!

    /** Upper case first name. */
    @JsonIgnore
    private String upperFirstname;
    /** Upper case surname. */
    @JsonIgnore
    private String upperSurname;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUnitId() {
        return unitId;
    }
    public void setUnitId(Long unitId) {
        this.unitId = unitId;
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
    public Boolean getDeleted() {
        return null == deleted ? Boolean.FALSE : deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    @Deprecated
    public Integer getStatus() {
        return (null == status ? 0 : status);
    }
    @Deprecated
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getVersion() {
        return (null == version ? 0 : version);
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    @JsonIgnore
    public String getUpperFirstname() {
        return upperFirstname;
    }
    public void setUpperFirstname(String upperFirstname) {
        this.upperFirstname = upperFirstname;
    }
    @JsonIgnore
    public String getUpperSurname() {
        return upperSurname;
    }
    public void setUpperSurname(String upperSurname) {
        this.upperSurname = upperSurname;
    }
    // CHECKSTYLE:ON

    /**
     * GAE datastore doesn't support case-insensitive queries
     * so I implement support for case-insensitive queries in the application:
     * for each field that you want to query in a case-insensitive way,
     * create a duplicate field that stores the value of that field in either all upper or lowercase letters.
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (null != firstname) {
            setUpperFirstname(ClientTextUtils.convert2ascii(firstname.trim().toUpperCase()));
        } else {
            setUpperFirstname(null);
        }
        if (null != surname) {
            setUpperSurname(ClientTextUtils.convert2ascii(surname.trim().toUpperCase()));
        } else {
            setUpperSurname(null);
        }
    }

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public PatientDto mapToDto() {
        final PatientDto rslt = new PatientDto();
        BeanUtils.copyProperties(this, rslt, new String[] { "unitId" });
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param dto corresponding DTO
     * @return instance of this
     */
    public static Patient mapFromDto(final PatientDto dto) {
        final Patient rslt = new Patient();
        mapFromDto(dto, rslt);
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param source source DTO
     * @param destination destination GAE entity
     */
    public static void mapFromDto(final PatientDto source, final Patient destination) {
        BeanUtils.copyProperties(source, destination, new String[] { "unit" });
    }

    // ------------------------------------------------------------- Java Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("PatientGae(id=")
            .append(getId())
            .append(", unitId=")
            .append(unitId)
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

        return id.equals(((Patient) other).getId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.intValue();
    }

}
