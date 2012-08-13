package veny.smevente.dao.jpa.gae;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.model.gae.MedicalHelpCategory;
import veny.smevente.server.JpaGaeUtils;
import veny.smevente.server.JpaGaeUtils.JpaCallback;

/**
 * GAE JPA DAO implementation for <code>MedicalHelpCategory</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public class MedicalHelpCategoryDaoGae extends AbstractDaoGae<MedicalHelpCategory> {
    /** */
    private static final String TYPE_PROPERTY_NAME = "e.type";
    /** */
    private static final String TYPE_PROPERTY_PARAMETER = "type";

    /**
     * Finds category according to given name.
     * @param unitId ID to search in
     * @param name category name to search
     * @param categoryType the type of category
     * @return instance of found category
     */
    public MedicalHelpCategory findByNameAndType(final Long unitId, final String name, final Short categoryType) {
        return JpaGaeUtils.execute(new JpaCallback<MedicalHelpCategory>() {
            @Override
            public MedicalHelpCategory doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM " + getPersistentClass().getName()
                        + " e WHERE e.unitId=:unitId AND e.name=:name" + getCategoryTypeWhere(categoryType));
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("name", name);
                if (categoryType != null) {
                    query.setParameter(TYPE_PROPERTY_PARAMETER, categoryType);
                }
                setSoftDeleteFilter(query);
                final MedicalHelpCategory rslt = (MedicalHelpCategory) query.getSingleResult();
                return rslt;
            }
        });
    }

    /**
     * Finds categories according to given type.
     * @param unitId ID to search in
     * @param categoryType the type of category
     * @param orderBy the property used for ordering
     * @return list of found categories
     */
    @SuppressWarnings("unchecked")
    public List<MedicalHelpCategory> findByType(final Long unitId, final Short categoryType, final String orderBy) {
        return JpaGaeUtils.execute(new JpaCallback<List<MedicalHelpCategory>>() {
            @Override
            public List<MedicalHelpCategory> doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM " + getPersistentClass().getName()
                    + " e WHERE e.unitId=:unitId" + getCategoryTypeWhere(categoryType));
                appendSoftDeleteFilter(sql);
                if (null != orderBy) {
                    sql.append(" ORDER BY " + orderBy);
                }
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                if (categoryType != null) {
                    query.setParameter(TYPE_PROPERTY_PARAMETER, categoryType);
                }
                setSoftDeleteFilter(query);
                final List<MedicalHelpCategory> rslt = (List<MedicalHelpCategory>) query.getResultList();
                rslt.size(); // load entities to eliminate 'Object Manager has been closed' exception
                return rslt;
            }
        });
    }

//    /**
//     * Marks given category as deleted (STATUS_DELETED).
//     * @param categoryId category ID
//     */
//    public void remove(final Long categoryId) {
//        JpaGaeUtils.execute(new JpaCallback<Object>() {
//            @Override
//            public Object doWithEntityManager(final EntityManager em) throws PersistenceException {
//                final MedicalHelpCategory mhc = em.find(getPersistentClass(), categoryId);
//                mhc.setStatus(mhc.getStatus() | MedicalHelpCategoryDto.STATUS_DELETED);
//                em.persist(mhc);
//                return null;
//            }
//        }, true);
//    }

    /**
     *
     * @param categoryType the type of category
     * @return the where clause for category type
     */
    private String getCategoryTypeWhere(final Short categoryType) {
        StringBuffer ret = new StringBuffer(TYPE_PROPERTY_NAME);

        if (categoryType == null) {
            ret.append(" IS NULL");
        } else {
            ret.append("=:");
            ret.append(TYPE_PROPERTY_PARAMETER);
            if (categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
                ret = new StringBuffer("(" + ret.toString());
                ret.append(" OR ");
                ret.append(TYPE_PROPERTY_NAME);
                ret.append(" IS NULL)");
            }
        }

        return " AND " + ret.toString();
    }
}
