package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import veny.smevente.misc.SoftDelete;

import com.fasterxml.jackson.annotation.JsonIgnore;



/**
 * Entity class representing the Procedure.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
@SoftDelete
public class Procedure extends AbstractEntity {

    /** Unit which is master of this. */
    @ManyToOne
    private Unit unit;
    /** Procedure name. */
    @Column
    private String name;
    /** Default message text. */
    @Column
    private String messageText;
    /** Color in calendar. */
    @Column
    private String color;
    /** Typical time [minutes]. */
    @Column
    private int time;
    /** Type of procedure. */
    @Column
    private String type;

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
    public String getMessageText() {
        return messageText;
    }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public int getTime() {
        return time;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    // CHECKSTYLE:ON

    // ---------------------------------------------------- Convenience Methods

    /**
     * Virtual attribute providing an enumeration entry to identify procedure type.
     *
     * @return event type or <i>Event.Type.IN_CALENDAR</i> if not defined
     */
    @Transient
    @JsonIgnore
    public Event.Type enumType() {
        final String t = getType();
        if (null == t || 0 == t.trim().length()) {
            return Event.Type.IN_CALENDAR;
        } else {
            return Event.Type.valueOf(t.trim());
        }
    }

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
        final StringBuilder rslt = new StringBuilder("Procedure(id=")
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
