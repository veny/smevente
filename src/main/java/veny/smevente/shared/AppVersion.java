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
        {"0.6.3",  "25.06.2019", "changed domain of SMS endpoint"},
        {"0.6.2",  "20.11.2016", "removed libraries: guava, commons-lang, commons-beanutils"},
        {"0.6.1.1", "30.09.2016", "small adaptations for docker"},
        {"0.6.1",  "30.09.2016", "removed log4j"},
        {"0.6.0",  "16.08.2016", "GWT v2.7, OrientDB v2.2.2"},
        {"0.5.0",  "16.06.2015", "Enh#34"},
        {"0.4.10", "10.06.2015", "BF#42, OrientDB v2.0.10"},
        {"0.4.9",  "10.06.2015", "BF#42"},
        {"0.4.8",  "03.06.2015", "BF#42"},
        {"0.4.7",  "19.05.2015", "BF#23 again, BF #40, BF #41"},
        {"0.4.6",  "08.02.2015", "BF#23 again (Event bulk send)"},
        {"0.4.5",  "16.12.2014", "BF#23 again (Event bulk send)"},
        {"0.4.4",  "09.12.2014", "BF#23 again (delete Event)"},
        {"0.4.3",  "10.11.2014", "BF#39, BF#23 again"},
        {"0.4.2",  "07.10.2014", "Enh#38, OrientDB v1.7.9"},
        {"0.4.1",  "18.09.2014", "BF#23 again, BF#33"},
        {"0.4.0",  "06.09.2014", "Enh#31, first version of mailing functionality"},
        {"0.3.9",  "26.07.2014", "Enh#32, adapted some log levels"},
        {"0.3.8",  "09.07.2014", "Enh#32, persistence of Customer#email"},
        {"0.3.7",  "04.07.2014", "BF#28, BF#30"},
        {"0.3.6",  "04.07.2014", "BF#7, BF#23 again"},
        {"0.3.5",  "03.07.2014", "BF#29, BF#23 again"},
        {"0.3.4",  "02.07.2014", "Enh#26, Enh#27, BF#23 again"},
        {"0.3.3",  "01.07.2014", "BF#23"},
        {"0.3.2",  "30.06.2014", "BF#19, BF#20, Enh#21, BF#22"},
        {"0.3.1",  "29.06.2014", "BF#15, Added sendNow"},
        {"0.3.0",  "28.06.2014", "Initial revision on OrientDB"}
    };

    /** App version. */
    String VERSION = VERSIONS[0][0];

}

// CHECKSTYLE:ON
