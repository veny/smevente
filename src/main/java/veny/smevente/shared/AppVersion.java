package veny.smevente.shared;

// CHECKSTYLE:OFF

/**
 * This interface represents only application's version history.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 3.7.2014
 */
public interface AppVersion {

    /** Versions history. */
    String[][] VERSIONS = new String[][] {
        {"0.3.5", "03.07.2014", "BF#29, BF#23 again"},
        {"0.3.4", "02.07.2014", "Enh#26, Enh#27, BF#23 again"},
        {"0.3.3", "01.07.2014", "BF#23"},
        {"0.3.2", "30.06.2014", "BF#19, BF#20, Enh#21, BF#22"},
        {"0.3.1", "29.06.2014", "BF#15, Added sendNow"},
        {"0.3.0", "28.06.2014", "Initial revision on OrientDB"}
    };

    /** App version. */
    String VERSION = VERSIONS[0][0];

}

// CHECKSTYLE:ON
