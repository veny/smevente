package veny.smevente.model;

import javax.persistence.ManyToOne;



/**
 * Entity class representing the Medical Help Category.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
public class MedicalHelpCategory extends AbstractEntity {

    /** Standard category. */
    public static final short TYPE_STANDARD = 0;
    /** Category used in special Sms. */
    public static final short TYPE_SPECIAL = 1;

    /** Unit which is master of this. */
    @ManyToOne
    private Unit unit;
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
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
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
