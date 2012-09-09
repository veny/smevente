package veny.smevente.dao;

import java.util.List;

import veny.smevente.model.MedicalHelpCategory;

/**
 * Interface for persistence operation with <code>MedicalHelpCategory</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public interface MedicalHelpCategoryDao extends GenericDao<MedicalHelpCategory> {

    /**
     * Finds category according to given name.
     *
     * @param unitId ID to search in
     * @param name category name to search
     * @param categoryType the type of category
     * @return instance of found category
     */
    MedicalHelpCategory findByNameAndType(Object unitId, final String name, Short categoryType);

    /**
     * Finds categories according to given type.
     *
     * @param unitId ID to search in
     * @param categoryType the type of category
     * @param orderBy the property used for ordering
     * @return list of found categories
     */
    List<MedicalHelpCategory> findByType(Object unitId, Short categoryType, String orderBy);

}
