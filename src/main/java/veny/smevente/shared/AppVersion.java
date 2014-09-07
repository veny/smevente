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
        {"0.4.1", "07.09.2014", "Added feedback & suggestion service"},
        {"0.4.0", "06.09.2014", "Enh#31, first version of mailing functionality"},
        {"0.3.9", "26.07.2014", "Enh#32, adapted some log levels"},
        {"0.3.8", "09.07.2014", "Enh#32, persistence of Customer#email"},
        {"0.3.7", "04.07.2014", "BF#28, BF#30"},
        {"0.3.6", "04.07.2014", "BF#7, BF#23 again"},
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
