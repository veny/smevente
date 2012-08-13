package veny.smevente.model.gae;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.BeanUtils;

import veny.smevente.misc.SoftDelete;
import veny.smevente.model.MedicalHelpCategoryDto;

/**
 * GAE entity representing the Medical Help Category.
 * <p/>
 * Schema versions:<ul>
 * <li>{@link http://blog.burnayev.com/2010/02/gae-developer-tip-updating-database.html}
 * <li>0 - id,unitId,name,smsText,color,time
 * <li>1 - type,status
 * <li>2 - deleted
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
@Entity
@SoftDelete
public class MedicalHelpCategory {

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unit which is master of this. */
    private Long unitId;
    /** Category name. */
    private String name;
    /** Default SMS text. */
    private String smsText;
    /** Category color in calendar. */
    private String color;
    /** Typical time [minutes]. */
    private long time;
    /** Additional type of category (MHC / Special SMS). */
    private Short type;
    /** Flag whether the object is deleted. */
    private Boolean deleted = Boolean.FALSE;
    /**
     * MHC's status.
     * TODO [tomas,A] remove it after deploy on production
     */
    @Deprecated
    private Integer status;
    /** Infrastructure schema version. */
    private Integer version = 2; // keep it always up to date (see class JavaDoc)!

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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSmsText() {
        return smsText;
    }
    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public Short getType() {
        return type;
    }
    public void setType(Short type) {
        this.type = type;
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
    // CHECKSTYLE:ON

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public MedicalHelpCategoryDto mapToDto() {
        final MedicalHelpCategoryDto rslt = new MedicalHelpCategoryDto();
        BeanUtils.copyProperties(this, rslt, new String[] { "unitId" });
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param dto corresponding DTO
     * @return instance of this
     */
    public static MedicalHelpCategory mapFromDto(final MedicalHelpCategoryDto dto) {
        final MedicalHelpCategory rslt = new MedicalHelpCategory();
        mapFromDto(dto, rslt);
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param source source DTO
     * @param destination destination GAE entity
     */
    public static void mapFromDto(final MedicalHelpCategoryDto source, final MedicalHelpCategory destination) {
        BeanUtils.copyProperties(source, destination, new String[] { "unit" });
    }

    // ------------------------------------------------------------- Java Stuff
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("MedicalHelpCategoryGae(id=")
            .append(getId())
            .append(", unitId=")
            .append(unitId)
            .append(", name='")
            .append(name)
            .append(", type='")
            .append(type)
            .append("')")
            .toString();
    }

}
