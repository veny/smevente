package veny.smevente.model;

import java.io.Serializable;


/**
 * DTO representing the Medical Help Category.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
public class MedicalHelpCategoryDto implements Serializable {

    /** Standard category. */
    public static final short TYPE_STANDARD = 0;
    /** Category used in special Sms. */
    public static final short TYPE_SPECIAL = 1;

    /** Generated (1110304) serial version UID. */
    private static final long serialVersionUID = -3133008636581757261L;

    /** Primary Key. */
    private Long id;

    /** Unit which is master of this. */
    private UnitDto unit;
    /** Category name. */
    private String name;
    /** Default SMS text. */
    private String smsText;
    /** Category color in calendar. */
    private String color;
    /** Typical time [minutes]. */
    private long time;
    /** Additional type of category. */
    private Short type;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public UnitDto getUnit() {
        return unit;
    }
    public void setUnit(UnitDto unit) {
        this.unit = unit;
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
    // CHECKSTYLE:ON

    // ---------------------------------------------------- Convenience Methods

    /**
     * Sets unit ID to a new unit object.
     * @param unitId unit ID
     */
    public void setUnitId(final Long unitId) {
        if (null == unitId || unitId.longValue() <= 0) {
            throw new IllegalArgumentException("invalid unit ID (null or less than 0)");
        }
        setUnit(new UnitDto());
        getUnit().setId(unitId);
    }

    // ----------------------------------------------------------- Object Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder rslt = new StringBuilder("MedicalHelpCategory(id=")
            .append(getId())
            .append(", name='")
            .append(name)
            .append(", type='")
            .append(type);

        if (null == unit) {
            rslt.append("', unit=null");
        } else {
            rslt.append("', unit='")
                .append(unit.getName())
                .append("'");
        }
        rslt.append("')");

        return rslt.toString();
    }

}
